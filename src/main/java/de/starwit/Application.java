package de.starwit;

import org.apache.camel.quarkus.main.CamelMainApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Application {

    private final Logger log = LogManager.getLogger(this.getClass());

    public static void main(String... args) {
        LOGGER.info("Running on quarkus-artemis");
        Quarkus.run(args);
    }

}