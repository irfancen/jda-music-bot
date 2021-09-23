package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class RemoveCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage("Missing arguments.").queue();
            return;
        }

        if (musicManager.audioPlayer.getPlayingTrack() == null && queue.isEmpty()) {
            channel.sendMessage("The queue is currently empty.").queue();
            return;
        }

        try {
            int index = Integer.parseInt(ctx.getArgs().get(0));
            AudioTrack song = List.copyOf(queue).get(index - 1);
            queue.remove(song);
            channel.sendMessageFormat("The song **%s** has been removed", song.getInfo().title).queue();
        } catch (NumberFormatException e) {
            channel.sendMessage("Argument needs to be the index of the queue.").queue();
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessageFormat("There are no songs at index **%d** of the queue.", Integer.parseInt(ctx.getArgs().get(0))).queue();
        }
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Removes a song from the queue**\n" +
                "Usage: `%1$sremove`\n" +
                "Aliases: `%1$sr`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("r");
    }
}
