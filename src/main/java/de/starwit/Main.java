package de.starwit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private final Logger log = LogManager.getLogger(this.getClass());
    public Logger getLog() {
        return log;
    }

    private DataBaseConnection dbCon;
    private QueueConsumer queueConsumer;

    // true if db and artemis connection is online
    private boolean ready = false;

    public Main() {       
        log.info("Starting Queue Consumer");
        dbCon = new DataBaseConnection();
        dbCon.createConnection();

        queueConsumer = new QueueConsumer(dbCon);
        queueConsumer.start();
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void run() {
        while(this.isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                this.getLog().warn("Message consuming Thread got interrupted " + e.getMessage());
            }
        }    
    }
    
    public void stop() {
        log.info("stopping client");
        ready = false;
        queueConsumer.stop();
        dbCon.stop();
    }

    public static void main( String[] args ) {
        Main m =  new Main();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            m.stop();
        }));

        m.run();
    }
    
}
