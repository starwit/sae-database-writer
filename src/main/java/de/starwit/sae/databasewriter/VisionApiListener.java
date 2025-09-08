package de.starwit.sae.databasewriter;

import java.util.Base64;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

public class VisionApiListener implements StreamListener<String, MapRecord<String, String, String>> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Consumer<VisionApiRecord> messageCallback;

    public VisionApiListener(Consumer<VisionApiRecord> messageCallback) {
        this.messageCallback = messageCallback;
    }
    
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String b64Proto = message.getValue().get("proto_data_b64");
            if (b64Proto == null) {
                log.warn("Received message missing key \"proto_data_b64\"");
                return;
            }

            byte[] msgBytes = Base64.getDecoder().decode(b64Proto);

            VisionApiRecord record = VisionApiParser.parse(msgBytes, message.getStream());

            this.messageCallback.accept(record);
        } catch (VisionApiException e) {
            log.warn("Received invalid message", e);
            return;
        }
    }
    
}
