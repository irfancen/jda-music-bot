package com.irfancen.musicbot.command.commands;

import com.irfancen.musicbot.CommandManager;
import com.irfancen.musicbot.Config;
import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }


    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            builder.append("List of commands\n");
            manager.getCommands().forEach(
                    (it) -> builder.append("`").append(Config.get("prefix")).append(it).append("`\n")
            );
            channel.sendMessage(builder.toString()).queue();
            return;
        }

        String search = args.get(0);
        ICommand command  = manager.getCommand(search);

        if (command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }

        channel.sendMessage(command.getHelp()).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "**Shows the list of bot commands available**\n" +
                "Usage: `-help [command]`\n" +
                "Aliases: `-h`, `-commands`, `-cmds`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("h", "commands", "cmds");
    }
}
