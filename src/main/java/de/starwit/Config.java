package de.starwit;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static Config instance;

    public final String dbUrl;
    public final String dbSchema;
    public final String dbUsername;
    public final String dbPassword;
    public final String dbHypertable;

    public final String brokerUrl;
    public final String brokerUsername;
    public final String brokerPassword;
    public final String brokerQueue;

    private Config() {
        Dotenv dotenv = Dotenv.load();

        dbUrl = dotenv.get("DB_URL");
        dbSchema = dotenv.get("DB_SCHEMA");
        dbUsername = dotenv.get("DB_USERNAME");
        dbPassword = dotenv.get("DB_PASSWORD");
        dbHypertable = dotenv.get("DB_HYPERTABLE");

        brokerUrl = dotenv.get("BROKER_URL");
        brokerUsername = dotenv.get("BROKER_USERNAME");
        brokerPassword = dotenv.get("BROKER_PASSWORD");
        brokerQueue = dotenv.get("BROKER_QUEUE");
    }
        
    public static Config getInstance() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }
        return Config.instance;
    }
}
