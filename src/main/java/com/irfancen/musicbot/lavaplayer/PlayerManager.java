package com.irfancen.musicbot.lavaplayer;

import com.irfancen.musicbot.command.CommandContext;
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

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, CommandContext ctx, String trackUrl) {
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

                if (playlist.isSearchResult() || tracks.size() == 1) {
                    musicManager.scheduler.queue(tracks.get(0));
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription(String.format("Queued [%s](%s)", tracks.get(0).getInfo().title, tracks.get(0).getInfo().uri))
                            .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl())
                            .build())
                            .queue();
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
