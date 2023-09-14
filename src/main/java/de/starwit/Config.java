package de.starwit;

import java.util.Arrays;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static Config instance;

    public final Boolean dbOutputEnabled;
    public final String dbJdbcUrl;
    public final String dbSchema;
    public final String dbUsername;
    public final String dbPassword;
    public final String dbHypertable;

    public final Boolean brokerInputEnabled;
    public final String brokerUrl;
    public final String brokerUsername;
    public final String brokerPassword;
    public final String brokerQueue;
    public final String brokerClientId;

    public final Boolean redisInputEnabled;
    public final String redisHost;
    public final Integer redisPort;
    public final List<String> redisStreamIds;
    public final String redisInputStreamPrefix;

    private Config() {
        Dotenv dotenv = Dotenv
                            .configure()
                            .ignoreIfMissing()
                            .load();

        dbOutputEnabled = Boolean.parseBoolean(dotenv.get("DB_OUTPUT_ENABLED", "false"));
        dbJdbcUrl = dotenv.get("DB_JDBC_URL");
        dbSchema = dotenv.get("DB_SCHEMA");
        dbUsername = dotenv.get("DB_USERNAME");
        dbPassword = dotenv.get("DB_PASSWORD");
        dbHypertable = dotenv.get("DB_HYPERTABLE");

        brokerInputEnabled = Boolean.parseBoolean(dotenv.get("BROKER_INPUT_ENABLED", "false"));
        brokerUrl = dotenv.get("BROKER_URL");
        brokerUsername = dotenv.get("BROKER_USERNAME");
        brokerPassword = dotenv.get("BROKER_PASSWORD");
        brokerQueue = dotenv.get("BROKER_QUEUE");
        brokerClientId = dotenv.get("BROKER_CLIENT_ID");

        redisInputEnabled = Boolean.parseBoolean(dotenv.get("REDIS_INPUT_ENABLED", "false"));
        redisHost = dotenv.get("REDIS_HOST");
        redisPort = Integer.parseInt(dotenv.get("REDIS_PORT"));
        redisStreamIds = Arrays.asList(dotenv.get("REDIS_STREAM_IDS").split(","));
        redisInputStreamPrefix = dotenv.get("REDIS_INPUT_STREAM_PREFIX", "objecttracker");
    }
        
    public static Config getInstance() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }
        return Config.instance;
    }
}
