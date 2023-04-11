package com.irfancen.musicbot.command.commands.music;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.lavaplayer.GuildMusicManager;
import com.irfancen.musicbot.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class RepeatCommand implements ICommand {
    @Override
    public void action(CommandContext ctx) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final boolean newRepeating = !musicManager.scheduler.repeating;

        musicManager.scheduler.repeating = newRepeating;

        final EmbedBuilder repeatEmbed = new EmbedBuilder().setColor(Color.RED);

        if (newRepeating) {
            repeatEmbed.setDescription("Repeating current song");
        } else {
            repeatEmbed.setDescription("No longer repeating current song");
        }
        ctx.getChannel().sendMessageEmbeds(repeatEmbed.build()).queue();
    }

    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format("**Loops the current song**\n" +
                "Usage: `%1$srepeat`\n" +
                "Aliases: `%1$sloop`", prefix);
    }

    @Override
    public List<String> getAliases() {
        return List.of("loop");
    }
}
