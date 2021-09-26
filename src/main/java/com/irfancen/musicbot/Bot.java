package com.irfancen.musicbot;

import com.irfancen.musicbot.database.SQLiteDataSource;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


public class Bot {

    public static void main(String[] arguments) throws Exception
    {
        SQLiteDataSource.getConnection();

        EventWaiter waiter = new EventWaiter();

        JDA api = JDABuilder.createDefault(
                Config.get("bot_token"),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        )
                .disableCache(
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE
                )
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new BotListener(waiter), waiter)
                .setActivity(Activity.playing("with her Developer."))
                .build().awaitReady();
    }
}
