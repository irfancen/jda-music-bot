package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlayingCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        final AudioTrack track = audioPlayer.getPlayingTrack();

        if (track == null) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("There is no songs currently playing")
                    .setColor(Color.RED)
                    .build())
                    .queue();
            return;
        }

        final AudioTrackInfo info = track.getInfo();

        long currentPos = track.getPosition();
        long duration = track.getDuration();

        int timePlayed = (int) (((float) currentPos / (float) duration) * 20);
        int timeRemain = 19 - timePlayed;

        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("Now Playing")
                .setDescription(String.format("[%s](%s)", info.title, info.uri))
                .setColor(ctx.getMember().getColor())
                .setFooter(String.format(
                                "%s\uD83D\uDD35%s %s/%s",
                                "\u25AC".repeat(timePlayed),
                                "\u25AC".repeat(timeRemain),
                                timeFormatter(currentPos),
                                timeFormatter(duration)))
                .build())
                .queue();
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Shows the song currently playing**\n" +
                "Usage: `%1$snowplaying`\n" +
                "Aliases: `%1$snp`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("np");
    }

    private String timeFormatter(long time) {
        long hour = TimeUnit.MILLISECONDS.toHours(time);
        long minute = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        if (hour > 0) {
            return String.format("%dh %dm %ds", hour, minute, second);
        } else if (minute > 0) {
            return String.format("%dm %ds", minute, second);
        } else {
            return String.format("%ds", second);
        }
    }
}
