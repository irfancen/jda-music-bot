package com.irfancen.musicbot.command.commands;

import com.irfancen.musicbot.BotMapping;
import com.irfancen.musicbot.CommandManager;
import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }


    @Override
    public void action(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        MessageChannel channel = ctx.getChannel();

        String prefix = BotMapping.PREFIXES.get(ctx.getGuild().getIdLong());

        if (args.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            builder.append("List of commands:```");
            manager.getCommands().forEach(
                    it -> builder.append("\n")
                            .append(prefix)
                            .append(it)
            );
            channel.sendMessage(builder + "```").queue();
            return;
        }

        String search = args.get(0);
        ICommand command  = manager.getCommand(search);

        if (command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }

        channel.sendMessage(command.getHelp(prefix)).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format(
                "**Shows the list of bot commands available**\n" +
                "Usage: `%1$shelp [command]`\n" +
                "Aliases: `%1$sh`, `%1$scommands`, `%1$scmds`", prefix
        );
    }

    @Override
    public List<String> getAliases() {
        return List.of("h", "commands", "cmds");
    }
}
