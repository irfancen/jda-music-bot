package com.irfancen.musicbot;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private Config(){}

    public static String get(String key) {
        return dotenv.get(key.toUpperCase());
    }
}
