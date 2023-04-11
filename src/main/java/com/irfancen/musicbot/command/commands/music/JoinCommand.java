package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.Objects;

public class JoinCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final AudioManager audioManager = ctx.getGuild().getAudioManager();
        final Member self = ctx.getSelfMember();
        final MessageChannel channel = ctx.getChannel();
        final VoiceChannel memberChannel = (VoiceChannel) Objects.requireNonNull(ctx.getMember().getVoiceState()).getChannel();

        if (memberChannel == null) return;
        if (!PermissionUtil.checkPermission(memberChannel, self, Permission.VOICE_CONNECT)) {
            channel.sendMessageEmbeds(new EmbedBuilder()
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
    public boolean check(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (selfVoiceState == null) return false;
        if (memberVoiceState == null) return false;
        if (memberVoiceState.getChannel() == null) return false;

        if (selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("Already connected to a voice channel")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("You need to be in a voice channel for this command to work")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        return true;
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
