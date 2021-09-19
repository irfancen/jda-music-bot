package com.irfancen.musicbot;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.command.commands.PingCommand;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CommandManager {
    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager() {
        addCommand(new PingCommand());
    }

    private void addCommand(ICommand cmd) {
        commands.put(cmd.getName().toLowerCase(), cmd);
    }

    @Nullable
    private ICommand getCommand(String search) {
        ICommand result = commands.get(search.toLowerCase());

        if (result == null) {
            for (ICommand cmd : commands.values()) {
                if (cmd.getAliases().contains(search.toLowerCase())) {
                    return cmd;
                }
            }
        }
        return result;
    }

    void handle(GuildMessageReceivedEvent event) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(Config.get("prefix")), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
        }
    }
}
