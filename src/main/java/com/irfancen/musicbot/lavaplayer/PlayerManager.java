package com.irfancen.musicbot.lavaplayer;

import com.irfancen.musicbot.command.CommandContext;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

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

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.emote = new HashMap<>();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        this.emote.put("1️⃣", 0);
        this.emote.put("2️⃣", 1);
        this.emote.put("3️⃣", 2);
        this.emote.put("4️⃣", 3);
        this.emote.put("5️⃣", 4);
        this.emote.put("❌", -1);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, CommandContext ctx, EventWaiter waiter, String trackUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                channel.sendMessage(new EmbedBuilder()
                        .setDescription(String.format("Queued [%s](%s)", track.getInfo().title, track.getInfo().uri))
                        .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                        .build())
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                if (tracks.size() == 1) {
                    musicManager.scheduler.queue(tracks.get(0));
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription(String.format("Queued [%s](%s)", tracks.get(0).getInfo().title, tracks.get(0).getInfo().uri))
                            .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
                } else if (playlist.isSearchResult()) {
                    List<String> pick = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        AudioTrack pickTrack = tracks.get(i);
                        pick.add(String.format("**%2d.**\t[%s](%s)", i+1, pickTrack.getInfo().title, pickTrack.getInfo().uri));
                    }
                    channel.sendMessage(new EmbedBuilder()
                            .setTitle("Choose a Track")
                            .setDescription(String.join("\n", pick))
                            .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .setColor(ctx.getMember().getColor())
                            .setTimestamp(Instant.now())
                            .build())
                            .queue((message -> {
                                List<String> emotes = new ArrayList<>(emote.keySet());
                                Collections.sort(emotes);
                                for (String emj : emotes) {
                                    message.addReaction(emj).queue();
                                }

                                waiter.waitForEvent(
                                        GuildMessageReactionAddEvent.class,
                                        (e) -> e.getMessageIdLong() == message.getIdLong() && e.getUser() == ctx.getAuthor(),
                                        (e) -> {
                                            int index = emote.get(e.getReactionEmote().getEmoji());
                                            if (index == -1) {
                                                channel.sendMessage(new EmbedBuilder()
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
                                            channel.sendMessage(new EmbedBuilder()
                                                    .setDescription(String.format("Queued [%s](%s)", track.getInfo().title, track.getInfo().uri))
                                                    .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
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
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription(String.format("Queued **%d** songs", tracks.size()))
                            .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage(new EmbedBuilder()
                        .setDescription(String.format("No songs found for **%s**", trackUrl.replaceFirst("ytsearch: ", "")))
                        .setColor(Color.RED)
                        .build())
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(new EmbedBuilder()
                        .setDescription(String.format("Error while loading the track **%s**", trackUrl.replaceFirst("ytsearch: ", "")))
                        .setColor(Color.RED)
                        .build())
                        .queue();
            }
        });
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }
}
