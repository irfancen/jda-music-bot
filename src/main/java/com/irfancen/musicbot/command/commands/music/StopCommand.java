package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class StopCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
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

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();

        channel.sendMessage(new EmbedBuilder()
                .setDescription("The player has been stopped and the queue has been cleared")
                .build())
                .queue();
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Stops the current song and clears the queue**\n" +
                "Usage: `%1$sstop`\n", prefix);
    }
}
