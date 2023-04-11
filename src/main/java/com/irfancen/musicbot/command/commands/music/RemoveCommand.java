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

public class RemoveCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final MessageChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("Missing arguments")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        if (musicManager.audioPlayer.getPlayingTrack() == null && queue.isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("The queue is currently empty")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final EmbedBuilder removeEmbed = new EmbedBuilder();

        try {
            int index = Integer.parseInt(ctx.getArgs().get(0));
            AudioTrack song = List.copyOf(queue).get(index - 1);
            queue.remove(song);
            channel.sendMessageEmbeds(
                    removeEmbed.setDescription(
                            String.format("Removed [%s](%s)", song.getInfo().title, song.getInfo().uri))
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
        return "remove";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Removes a song from the queue**\n" +
                "Usage: `%1$sremove`\n" +
                "Aliases: `%1$sr`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("r");
    }
}
