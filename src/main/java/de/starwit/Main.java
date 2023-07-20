package de.starwit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    private DataBaseConnection dbCon;
    private QueueConsumer queueConsumer;

    private boolean running = false;

    public Main() {       
        log.info("Starting Queue Consumer");
        dbCon = new DataBaseConnection();
        dbCon.createConnection();

        queueConsumer = new QueueConsumer(dbCon);
        queueConsumer.start();
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
        queueConsumer.stop();
        dbCon.stop();
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
