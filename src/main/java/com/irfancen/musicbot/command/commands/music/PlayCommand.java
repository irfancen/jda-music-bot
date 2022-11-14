package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class PlayCommand implements ICommand {
    private final EventWaiter waiter;

    public PlayCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
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
                channel.sendMessageEmbeds(new EmbedBuilder()
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
                channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription("You need to be in the same voice channel as me for this to work")
                        .setColor(Color.RED)
                        .build())
                        .queue();
                return;
            }
        }

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("You need to be in the voice channel for this to work")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        String args = String.join(" ", ctx.getArgs());

        if (args.startsWith("https://open.spotify.com/")) {
            String[] listArgs = null;
            if (args.contains("track")) {
                try {
                    Document doc = Jsoup.connect(args).userAgent("Mozilla").data("name", "jsoup").get();
                    args = doc.title()
                            .replace("Spotify - ", "")
                            .replace("- song and lyrics by ", "");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                listArgs = new String[]{"ytsearch: " + args};
            } else if (args.contains("playlist") || args.contains("album")) {
                try {
                    Document doc = Jsoup.connect(args).userAgent("Mozilla").data("name", "jsoup").get();
                    Elements tracks = doc.select("div[type=track]");

                    listArgs = new String[tracks.size()];

                    for (int i = 0; i < tracks.size(); i++) {
                        String song = tracks.get(i).select("a[href*=/track/]").text();
                        String artist = tracks.get(i).select("a[href*=/artist/]").text();
                        listArgs[i] = "ytsearch: " + song + " " + artist;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            PlayerManager.getInstance().loadAndPlay(channel, ctx, waiter, listArgs);
        } else {
            if (!isUrl(args)) {
                args = "ytsearch: " + args;
            }
            PlayerManager.getInstance().loadAndPlay(channel, ctx, waiter, args);
        }
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
