package com.irfancen.musicbot.command.commands;

import com.irfancen.musicbot.command.CommandContext;
import com.irfancen.musicbot.command.ICommand;
import net.dv8tion.jda.api.JDA;

public class PingCommand implements ICommand {

    @Override
    public void action(CommandContext ctx) {
        JDA jda = ctx.getJDA();

        jda.getRestPing().queue(
                ping -> ctx.getChannel()
                        .sendMessageFormat(":ping_pong: REST: %sms | WS: %sms", ping, jda.getGatewayPing()).queue()
        );
    }

    @Override
    public boolean check(CommandContext ctx) {
        return true;
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getHelp(String prefix) {
        return String.format(
                "**Shows the current ping**\n" +
                        "Usage: `%1$sping`", prefix
        );
    }
}
