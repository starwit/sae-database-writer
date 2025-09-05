package de.starwit.sae.databasewriter;

import java.util.Base64;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import de.starwit.visionapi.Analytics.DetectionCountMessage;
import de.starwit.visionapi.Common.TypeMessage;
import de.starwit.visionapi.Sae.PositionMessage;
import de.starwit.visionapi.Sae.SaeMessage;

public class VisionApiListener implements StreamListener<String, MapRecord<String, String, String>> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Consumer<Message> messageCallback;

    public VisionApiListener(Consumer<Message> messageCallback) {
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

            TypeMessage typeMsg = TypeMessage.parseFrom(msgBytes);
            log.info("Received message of type: " + typeMsg.getType().name());
            
            Message parsedMessage;
            parsedMessage = switch (typeMsg.getType()) {
                case SAE -> SaeMessage.parseFrom(msgBytes);
                case POSITION -> PositionMessage.parseFrom(msgBytes);
                case DETECTION_COUNT -> DetectionCountMessage.parseFrom(msgBytes);
                default -> null;
            };
            
            if (parsedMessage == null) {
                log.warn("Unhandled message type: " + typeMsg.getType());
                return;
            }

            this.messageCallback.accept(parsedMessage);
        } catch (InvalidProtocolBufferException e) {
            log.warn("Received invalid proto", e);
            return;
        }
    }
    
}
