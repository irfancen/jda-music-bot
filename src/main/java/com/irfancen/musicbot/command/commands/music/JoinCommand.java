package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;

public class JoinCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState.inVoiceChannel()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("Already connected to a voice channel")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("You need to be in a voice channel for this command to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final AudioManager audioManager = ctx.getGuild().getAudioManager();
        final VoiceChannel memberChannel = memberVoiceState.getChannel();

        if (!PermissionUtil.checkPermission(memberChannel, self, Permission.VOICE_CONNECT)) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("I don't have permission to join the voice channel")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        audioManager.openAudioConnection(memberChannel);
        channel.sendMessageFormat("Connected to `\uD83D\uDD0A`  **%s**.", memberChannel.getName()).queue();
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Makes the bot join your voice channel**\n" +
                "Usage: `%1$sjoin`", prefix);
    }
}
