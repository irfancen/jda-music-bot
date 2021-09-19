package com.irfancen.musicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;


public class Bot {

    public static void main(String[] arguments) throws Exception
    {
        JDA api = JDABuilder.createDefault(Config.get("bot_token"))
                .addEventListeners(new BotListener())
                .setActivity(Activity.playing("with her Developer."))
                .build().awaitReady();
    }
}
