package com.irfancen.musicbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.util.List;

public interface ICommand {
    String getName();

    void action(CommandContext ctx);

    String getHelp(String prefix);

    default List<String> getAliases() {
        return List.of();
    }

    default void handle(CommandContext ctx) {
        if (!check(ctx)) return;
        action(ctx);
    }

    default boolean check(CommandContext ctx) {
        final MessageChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (selfVoiceState == null) return false;
        if (memberVoiceState == null) return false;
        if (memberVoiceState.getChannel() == null) return false;

        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("I need to be in a voice channel for this to work")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("You need to be in the voice channel for this to work")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("You need to be in the same voice channel as me for this to work")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return false;
        }

        return true;
    }
}
