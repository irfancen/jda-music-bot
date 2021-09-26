package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class RemoveCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inVoiceChannel()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("I need to be in a voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("You need to be in the voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("You need to be in the same voice channel as me for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("Missing arguments")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        if (musicManager.audioPlayer.getPlayingTrack() == null && queue.isEmpty()) {
            channel.sendMessage(new EmbedBuilder()
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
            channel.sendMessage(
                    removeEmbed.setDescription(
                            String.format("Removed [%s](%s)", song.getInfo().title, song.getInfo().uri))
                            .build())
                    .queue();
        } catch (NumberFormatException e) {
            channel.sendMessage(
                    removeEmbed.setDescription("Argument needs to be the index of the queue")
                            .setColor(Color.RED)
                            .build())
                    .queue();
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessage(
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
