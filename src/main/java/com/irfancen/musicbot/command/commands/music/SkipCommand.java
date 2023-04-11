package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SkipCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        final MessageChannel channel = ctx.getChannel();

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("There is no songs currently playing")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final EmbedBuilder removeEmbed = new EmbedBuilder();

        try {
            if (ctx.getArgs().isEmpty()) {
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription("Skipped to the next song")
                        .build())
                        .queue();
                return;
            }
            int index = Integer.parseInt(ctx.getArgs().get(0));
            AudioTrack song = List.copyOf(queue).get(index - 1);
            for (int i = 0; i < index - 1; i++) {
                queue.poll();
            }
            musicManager.scheduler.nextTrack();

            channel.sendMessageEmbeds(
                    removeEmbed.setDescription(
                            String.format("Skipped to [%s](%s)", song.getInfo().title, song.getInfo().uri))
                            .build())
                    .queue();
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(
                    removeEmbed.setDescription("Argument needs to be the index of the queue")
                            .setColor(Color.RED)
                            .build())
                    .queue();
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessageEmbeds(
                    removeEmbed.setDescription(
                            String.format("There are no songs at index **%d** of the queue", Integer.parseInt(ctx.getArgs().get(0))))
                            .setColor(Color.RED)
                            .build())
                    .queue();
        }
    }



    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Skips the current song**\n" +
                "Usage: `%1$sskip` or `%1$sskip <index of song>`\n", prefix);
    }
}
