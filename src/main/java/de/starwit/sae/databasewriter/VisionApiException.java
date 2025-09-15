package de.starwit.sae.databasewriter;

public class VisionApiException extends Exception {
    public VisionApiException(String message) {
        super(message);
    }

    public VisionApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
