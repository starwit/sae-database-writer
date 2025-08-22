package de.starwit.sae.databasewriter;

import java.util.Base64;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

import com.google.protobuf.InvalidProtocolBufferException;

import de.starwit.visionapi.Common.TypeMessage;

public class VisionApiListener implements StreamListener<String, MapRecord<String, String, String>> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Consumer<TypeMessage> messageCallback;

    public VisionApiListener(Consumer<TypeMessage> messageCallback) {
        this.messageCallback = messageCallback;
    }
    
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String b64Proto = message.getValue().get("proto_data_b64");
        TypeMessage typeMsg;

        try {
            typeMsg = TypeMessage.parseFrom(Base64.getDecoder().decode(b64Proto));
        } catch (InvalidProtocolBufferException e) {
            log.warn("Received invalid proto", e);
            return;
        }

        log.info("Received message of type: " + typeMsg.getType().name());
        messageCallback.accept(typeMsg);
    }
    
}
