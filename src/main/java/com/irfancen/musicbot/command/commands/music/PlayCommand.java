package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class PlayCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("Missing arguments")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!selfVoiceState.inVoiceChannel()) {
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
        } else {
            if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
                channel.sendMessage(new EmbedBuilder()
                        .setDescription("You need to be in the same voice channel as me for this to work")
                        .setColor(Color.RED)
                        .build())
                        .queue();
                return;
            }
        }

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage(new EmbedBuilder()
                    .setDescription("You need to be in the voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        String args = String.join(" ", ctx.getArgs());

        if (!isUrl(args)) {
            args = "ytsearch: " + args;
        }

        PlayerManager.getInstance().loadAndPlay(channel, ctx, args);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Plays a song**\n" +
                "Usage: `%1$splay <link>`\n" +
                "Aliases: `%1$sp`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("p");
    }

    public static boolean isUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
