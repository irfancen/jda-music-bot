package com.irfancen.musicbot;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import com.irfancen.musicbot.command.commands.HelpCommand;
import com.irfancen.musicbot.command.commands.PingCommand;
import com.irfancen.musicbot.command.commands.admin.SetPrefixCommand;
import com.irfancen.musicbot.command.commands.music.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class CommandManager {
    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(EventWaiter waiter) {
        // Basic Commands
        addCommand(new PingCommand());
        addCommand(new HelpCommand(this));
        addCommand(new SetPrefixCommand());

        // Music Commands
        addCommand(new JoinCommand());
        addCommand(new LeaveCommand());
        addCommand(new PlayCommand(waiter));
        addCommand(new NowPlayingCommand());
        addCommand(new QueueCommand());
        addCommand(new ClearCommand());
        addCommand(new RemoveCommand());
        addCommand(new RepeatCommand());
        addCommand(new ShuffleCommand());
        addCommand(new StopCommand());
        addCommand(new SkipCommand());
    }

    private void addCommand(ICommand cmd) {
        commands.put(cmd.getName().toLowerCase(), cmd);
    }

    public Set<String> getCommands() {
        return commands.keySet();
    }

    @Nullable
    public ICommand getCommand(String search) {
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

    void handle(GuildMessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
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
