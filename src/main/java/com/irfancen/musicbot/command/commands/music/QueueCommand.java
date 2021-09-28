package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;

        if (musicManager.audioPlayer.getPlayingTrack() == null && queue.isEmpty()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("The queue is currently empty")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final int trackCount = Math.min(queue.size(), 10);
        final List<AudioTrack> trackList = new ArrayList<>(queue);
        final AudioTrack currentTrack = musicManager.audioPlayer.getPlayingTrack();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Queue")
                .setDescription(String.format("Showing up to next %s tracks", trackCount))
                .setColor(ctx.getMember().getColor())
                .setTimestamp(Instant.now())
                .addField(
                        new MessageEmbed
                                .Field("Currently playing",
                                String.format("[%s](%s)", currentTrack == null ?
                                                "No songs currently playing" :
                                                currentTrack.getInfo().title,
                                        currentTrack == null ? null : currentTrack.getInfo().uri),
                                false))
                .setFooter(String.format("Requested by %s", ctx.getAuthor().getName()), ctx.getAuthor().getAvatarUrl());

        List<String> lst = new ArrayList<>();
        for (int i = 0; i < trackCount; i++) {
            AudioTrack track = trackList.get(i);
            lst.add(String.format("`%2d.` [%s](%s)", i+1, track.getInfo().title.length() > 75 ? track.getInfo().title.substring(0, 75).stripTrailing() + "..." : track.getInfo().title, track.getInfo().uri));
        }
        if (trackCount > 0) {
            embedBuilder.addField(new MessageEmbed.Field("Next up", String.join("\n", lst),false));
        }

        if (trackList.size() > trackCount) {
            embedBuilder.addField("", String.format("and **%d** more...", trackList.size() - trackCount), false);
        }

        channel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Shows the queued songs**\n" +
                "Usage: `%1$squeue`\n" +
                "Aliases: `%1$sq`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("q");
    }
}
