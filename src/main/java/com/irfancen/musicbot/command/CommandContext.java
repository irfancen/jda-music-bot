package com.irfancen.musicbot.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandContext {
    private final MessageReceivedEvent event;
    private final List<String> args;

    public CommandContext(MessageReceivedEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    public Guild getGuild() {
        return this.getEvent().getGuild();
    }

    public MessageReceivedEvent getEvent() {
        return this.event;
    }

    public MessageChannel getChannel() {
        return this.getEvent().getChannel();
    }

    public User getAuthor() {
        return this.getEvent().getAuthor();
    }

    public Member getMember() {
        return this.getEvent().getMember();
    }

    public JDA getJDA() {
        return this.getEvent().getJDA();
    }

    public Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

    public List<String> getArgs() {
        return this.args;
    }
}
