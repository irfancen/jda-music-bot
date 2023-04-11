package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ClearCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final MessageChannel channel = ctx.getChannel();

        if (musicManager.audioPlayer.getPlayingTrack() == null && queue.isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("The queue is currently empty")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        queue.clear();
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setDescription("The queue has been cleared")
                .build())
                .queue();
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Clears the queue**\n" +
                "Usage: `%1$sclear`\n" +
                "Aliases: `%1$scl`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("cl");
    }
}
