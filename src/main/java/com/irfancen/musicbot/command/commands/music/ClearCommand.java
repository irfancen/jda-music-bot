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

public class ClearCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inVoiceChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("I need to be in a voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("You need to be in the voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("You need to be in the same voice channel as me for this to work")
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
