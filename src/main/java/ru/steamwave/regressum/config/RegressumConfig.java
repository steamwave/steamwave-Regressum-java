package ru.steamwave.regressum.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RegressumConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> DB_HOST;
    public static final ModConfigSpec.IntValue DB_PORT;
    public static final ModConfigSpec.ConfigValue<String> DB_NAME;
    public static final ModConfigSpec.ConfigValue<String> DB_USER;
    public static final ModConfigSpec.ConfigValue<String> DB_PASS;

    static {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        BUILDER.push("Database Settings");

        DB_HOST = BUILDER.comment("Хост базы данных").define("host", "localhost");
        DB_PORT = BUILDER.comment("Порт базы данных").defineInRange("port", 3306, 1, 65535);
        DB_NAME = BUILDER.comment("Имя базы данных").define("database", "regressum");
        DB_USER = BUILDER.comment("Пользователь").define("user", "root");
        DB_PASS = BUILDER.comment("Пароль").define("password", "password");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}