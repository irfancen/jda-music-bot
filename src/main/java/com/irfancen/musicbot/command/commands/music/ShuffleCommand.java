package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        if (musicManager.audioPlayer.getPlayingTrack() == null && musicManager.scheduler.queue.isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("The queue is currently empty")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        Collections.shuffle(queue);
        musicManager.scheduler.queue.clear();

        for (AudioTrack track: queue) {
            musicManager.scheduler.queue(track);
        }

        channel.sendMessageEmbeds(new EmbedBuilder()
                .setDescription("The queue has been shuffled")
                .build())
                .queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Shuffles the queue**\n" +
                "Usage: `%1$sshuffle`\n", prefix);
    }
}
