package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;


public class StopCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();

        channel.sendMessageEmbeds(new EmbedBuilder()
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
