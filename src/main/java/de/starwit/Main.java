package de.starwit;

import javax.xml.crypto.Data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    private DataBaseConnection dbCon;
    private QueueConsumer queueConsumer;
    private RedisConsumer redisConsumer;
    private Config config = Config.getInstance();

    private boolean running = false;

    public Main() {
        log.info("Starting vision api listener");

        if (config.dbOutputEnabled && config.brokerInputEnabled && config.redisInputEnabled) {
            //TODO DB connection is not thread-safe, fix if necessary
            log.warn("You have enabled both inputs. DB write requests will be synchronized, which may cause performance issues.");
        }

        if (config.dbOutputEnabled) {
            log.info("Setting up database connection to {}", config.dbJdbcUrl);
            dbCon = DataBaseConnection.getInstance();
            dbCon.createConnection();
        }

        if (config.brokerInputEnabled) {
            log.info("Starting JMS Consumer from {} - {}", config.brokerUrl, config.brokerQueue);
            queueConsumer = new QueueConsumer(dbCon);
            queueConsumer.start();
        }

        if (config.redisInputEnabled) {
            log.info("Starting Redis consumer from {}:{} - {}", config.redisHost, config.redisPort, config.redisStreamIds);
            redisConsumer = new RedisConsumer(dbCon);
            redisConsumer.start();
        }

        running = true;
    }

    public void run() {
        while(this.running) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.warn("Message consuming Thread got interrupted " + e.getMessage());
            }
        }    
    }
    
    public void stop() {
        log.info("stopping client");

        if (queueConsumer != null) {
            queueConsumer.stop();
        }
        if (redisConsumer != null) {
            redisConsumer.stop();
        }
        if (dbCon != null) {
            dbCon.stop();
        }

        running = false;
    }

    public static void main( String[] args ) {
        Main m =  new Main();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            m.stop();
        }));

        m.run();
    }
    
}
