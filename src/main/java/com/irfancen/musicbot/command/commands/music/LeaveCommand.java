package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;


public class LeaveCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        final MessageChannel channel = ctx.getChannel();

        musicManager.scheduler.repeating = false;
        musicManager.scheduler.queue.clear();
        musicManager.audioPlayer.stopTrack();

        final AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setDescription("I have left the voice channel")
                .build())
                .queue();
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Leaves the current voice channel**\n" +
                "Usage: `%1$sleave`\n", prefix);
    }
}
