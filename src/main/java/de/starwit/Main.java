package de.starwit;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private final Logger log = LogManager.getLogger(this.getClass());
    public Logger getLog() {
        return log;
    }

    private Properties config = new Properties();

    private DataBaseConnection dbCon;
    private Consumer queueConsumer;

    // true if db and artemis connection is online
    private boolean ready = false;

    public Main() {       
        log.info("Starting Queue Consumer");
        loadProperties();
        dbCon = new DataBaseConnection(config);
        dbCon.createConnection();

        queueConsumer = new Consumer(config);
        queueConsumer.configureQueue();
        ready = true;
    }

    private void loadProperties() {
        try {
            FileInputStream fis = new FileInputStream("client.properties");
            config.load(fis);
        } catch (IOException e) {
            log.error("Can't load config file. Exit. Reason " + e.getMessage());
            System.exit(1);
        }
        // TODO search path for props file
    }

    public boolean isReady() {
        return ready;
    }
    
    public void stop() {
        log.info("stopping client");
        ready = false;
        queueConsumer.stop();
        dbCon.stop();
    }

    public static void main( String[] args ) {

        Main m =  new Main();

        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    m.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });*/
        
        while(m.isReady()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                m.getLog().warn("Message consuming Thread got interrupted " + e.getMessage());
            }
        }        
    }
    
}
