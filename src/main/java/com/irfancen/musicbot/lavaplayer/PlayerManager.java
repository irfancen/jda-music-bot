package com.irfancen.musicbot.lavaplayer;

import com.irfancen.musicbot.Config;
import com.irfancen.musicbot.command.CommandContext;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    final Map<String, Integer> emote;
    static final String YT_SEARCH = "ytsearch: ";
    static final String QUEUED = "Queued [%s](%s)";
    static final String REQUESTED = "Requested by %s";
    static final String SONG_NOT_FOUND = "No songs found for **%s**";

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.emote = new HashMap<>();

        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true, new Client[] { new WebEmbedded(), new TvHtml5Embedded() });

        if (!Config.get("REFRESH_TOKEN").isEmpty()) {
            youtube.useOauth2(Config.get("REFRESH_TOKEN"), true);
        } else {
            youtube.useOauth2(null, false);
        }
        //        Web.setPoTokenAndVisitorData(Config.get("PO_TOKEN"),Config.get("VISITOR_DATA"));

        this.audioPlayerManager.registerSourceManager(youtube);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        this.emote.put("1️⃣", 0);
        this.emote.put("2️⃣", 1);
        this.emote.put("3️⃣", 2);
        this.emote.put("4️⃣", 3);
        this.emote.put("5️⃣", 4);
        this.emote.put("❌", -1);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(MessageChannel channel, CommandContext ctx, EventWaiter waiter, String trackUrl) {

        final GuildMusicManager musicManager = this.getMusicManager(ctx.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription(String.format(QUEUED, track.getInfo().title, track.getInfo().uri))
                        .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                        .build())
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                if (tracks.size() == 1) {
                    musicManager.scheduler.queue(tracks.get(0));
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(String.format(QUEUED, tracks.get(0).getInfo().title, tracks.get(0).getInfo().uri))
                            .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
                } else if (playlist.isSearchResult()) {
                    List<String> pick = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        AudioTrack pickTrack = tracks.get(i);
                        pick.add(String.format("**%2d.**\t[%s](%s)\t[%s]", i+1, pickTrack.getInfo().title, pickTrack.getInfo().uri, timeFormatter(pickTrack.getDuration())));
                    }
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Choose a Track")
                            .setDescription(String.join("\n", pick))
                            .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .setColor(ctx.getMember().getColor())
                            .setTimestamp(Instant.now())
                            .build())
                            .queue((message -> {
                                List<String> emotes = new ArrayList<>(emote.keySet());
                                Collections.sort(emotes);
                                for (String emj : emotes) {
                                    message.addReaction(Emoji.fromFormatted(emj)).queue();
                                }

                                waiter.waitForEvent(
                                        MessageReactionAddEvent.class,
                                        e -> e.getMessageIdLong() == message.getIdLong() && e.getUser() == ctx.getAuthor(),
                                        e -> {
                                            int index = emote.get(e.getReaction().getEmoji().getFormatted());
                                            if (index == -1) {
                                                channel.sendMessageEmbeds(new EmbedBuilder()
                                                        .setDescription("Operation cancelled")
                                                        .setColor(Color.RED)
                                                        .build())
                                                        .queue();
                                                message.delete().queue();
                                                return;
                                            }
                                            AudioTrack track = tracks.get(index);
                                            musicManager.scheduler.queue(track);
                                            message.delete().queue();
                                            channel.sendMessageEmbeds(new EmbedBuilder()
                                                    .setDescription(String.format(QUEUED, track.getInfo().title, track.getInfo().uri))
                                                    .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                                                    .build())
                                                    .queue();
                                        },
                                        60L, TimeUnit.SECONDS,
                                        () -> message.delete().queue()
                                );
                            }));
                } else {
                    for (final AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(String.format("Queued **%d** songs", tracks.size()))
                            .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription(String.format(SONG_NOT_FOUND, trackUrl.replaceFirst(YT_SEARCH, "")))
                        .setColor(Color.RED)
                        .build())
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription(String.format("Error while loading the %s **%s**", trackUrl.contains("https://www.youtube.com/playlist") ? "playlist" : "track", trackUrl.replaceFirst(YT_SEARCH, "")))
                        .setColor(Color.RED)
                        .build())
                        .queue();
            }
        });
    }

    public void loadAndPlay(MessageChannel channel, CommandContext ctx, EventWaiter waiter, String[] tracks) {
        final GuildMusicManager musicManager = this.getMusicManager(ctx.getGuild());

        if (tracks.length == 1) {
            String trackUrl = tracks[0];
            this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    // All requests from Spotify counts as a Playlist
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack track = playlist.getTracks().get(0);
                    musicManager.scheduler.queue(track);
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(String.format(QUEUED, track.getInfo().title, track.getInfo().uri))
                            .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
                }

                @Override
                public void noMatches() {
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(String.format(SONG_NOT_FOUND, trackUrl.replaceFirst(YT_SEARCH, "")))
                            .setColor(Color.RED)
                            .build())
                            .queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(String.format("Error while loading the track **%s**", trackUrl.replaceFirst(YT_SEARCH, "")))
                            .setColor(Color.RED)
                            .build())
                            .queue();
                }
            });
        } else {
            for (String trackUrl: tracks) {
                this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        // All requests from Spotify counts as a Playlist
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        AudioTrack track = playlist.getTracks().get(0);
                        musicManager.scheduler.queue(track);
                    }

                    @Override
                    public void noMatches() {
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setDescription(String.format(SONG_NOT_FOUND, trackUrl.replaceFirst(YT_SEARCH, "")))
                                .setColor(Color.RED)
                                .build())
                                .queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        channel.sendMessageEmbeds(new EmbedBuilder()
                                .setDescription(String.format("Error while loading the track **%s**", trackUrl.replaceFirst(YT_SEARCH, "")))
                                .setColor(Color.RED)
                                .build())
                                .queue();
                    }
                });
            }
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription(String.format("Queued **%d** songs", tracks.length))
                    .setFooter(String.format(REQUESTED, ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                    .build())
                    .queue();
        }
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    private String timeFormatter(long time) {
        long hour = TimeUnit.MILLISECONDS.toHours(time);
        long minute = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }
    }

    private ClientOptions setupClientOptions(boolean playback, boolean playlistLoading, boolean videoLoading, boolean searching) {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setPlayback(playback);
        clientOptions.setPlaylistLoading(playlistLoading);
        clientOptions.setVideoLoading(videoLoading);
        clientOptions.setSearching(searching);
        return clientOptions;
    }
}
