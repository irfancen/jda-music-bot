package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
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
    public void action(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("Missing arguments")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        String args = String.join(" ", ctx.getArgs());

        if (args.startsWith("https://open.spotify.com/")) {
            String[] listArgs = null;
            if (args.contains("track")) {
                args = spotifyTrack(args);
                listArgs = new String[]{"ytsearch: " + args};
            } else if (args.contains("playlist") || args.contains("album")) {
                listArgs = spotifyPlaylist(args, listArgs);
            }
            PlayerManager.getInstance().loadAndPlay(channel, ctx, waiter, listArgs);
        } else {
            if (!isUrl(args)) {
                args = "ytsearch: " + args;
            } else if (args.startsWith("https://www.youtube.com/watch")) {
                args = args.substring(0, !args.contains("&list") ? args.length() : args.indexOf("&list"));
            }
            PlayerManager.getInstance().loadAndPlay(channel, ctx, waiter, args);
        }
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

        if (!selfVoiceState.inAudioChannel()) {
            final AudioManager audioManager = ctx.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) memberVoiceState.getChannel();

            if (!memberVoiceState.inAudioChannel()) {
                channel.sendMessageEmbeds(new EmbedBuilder()
                                .setDescription("You need to be in a voice channel for this command to work")
                                .setColor(Color.RED)
                                .build())
                        .queue();
                return false;
            } else if (!PermissionUtil.checkPermission(memberChannel, self, Permission.VOICE_CONNECT)) {
                channel.sendMessageEmbeds(new EmbedBuilder()
                                .setDescription("I don't have permission to join the voice channel")
                                .setColor(Color.RED)
                                .build())
                        .queue();
                return false;
            }

            audioManager.openAudioConnection(memberChannel);
            channel.sendMessageFormat("Connected to `\uD83D\uDD0A`  **%s**.", memberChannel.getName()).queue();
        }

        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("You need to be in the voice channel for this to work")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        return true;
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

    private String spotifyTrack(String args) {
        try {
            Document doc = Jsoup.connect(args).userAgent("Mozilla").data("name", "jsoup").get();
            args = doc.title()
                    .replace("Spotify - ", "")
                    .replace("- song and lyrics by ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return args;
    }

    private String[] spotifyPlaylist(String args, String[] listArgs) {
        try {
            Document doc = Jsoup.connect(args).userAgent("Mozilla").data("name", "jsoup").get();
            Elements tracks = doc.select("div[data-testid=track-row]");

            listArgs = new String[tracks.size()];

            for (int i = 0; i < tracks.size(); i++) {
                String song = tracks.get(i).select("a[href*=/track/]").text();
                String artist = tracks.get(i).select("a[href*=/artist/]").text();
                listArgs[i] = "ytsearch: " + song + " " + artist;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listArgs;
    }
}
