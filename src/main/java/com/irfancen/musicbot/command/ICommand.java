package com.irfancen.musicbot.command;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);

    String getName();

    String getHelp(String prefix);

    default List<String> getAliases() {
        return List.of();
    }
}
