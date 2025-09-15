package de.starwit.sae.databasewriter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import de.starwit.visionapi.Analytics.DetectionCountMessage;
import de.starwit.visionapi.Common.TypeMessage;
import de.starwit.visionapi.Sae.PositionMessage;
import de.starwit.visionapi.Sae.SaeMessage;

public class VisionApiParser {
    public static VisionApiRecord parse(byte[] messageBytes, String streamKey) throws VisionApiException {
        try {
            TypeMessage typeMsg;
            typeMsg = TypeMessage.parseFrom(messageBytes);

            Message parsedMessage;
            parsedMessage = switch (typeMsg.getType()) {
                case SAE -> SaeMessage.parseFrom(messageBytes);
                case POSITION -> PositionMessage.parseFrom(messageBytes);
                case DETECTION_COUNT -> DetectionCountMessage.parseFrom(messageBytes);

                default -> throw new VisionApiException("Unhandled message type: " + typeMsg.getType());
            };
            
            long timestampUtcMs = switch (parsedMessage) {
                case SaeMessage msg -> msg.getFrame().getTimestampUtcMs();
                case PositionMessage msg -> msg.getTimestampUtcMs();
                case DetectionCountMessage msg -> msg.getTimestampUtcMs();
                default -> 0;
            };

            ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampUtcMs), ZoneOffset.UTC);

            return new VisionApiRecord(timestamp, streamKey, typeMsg.getType(), JsonFormat.printer().print(parsedMessage));
        } catch (InvalidProtocolBufferException e) {
            throw new VisionApiException("Encountered an error while parsing protobuf", e);
        }
    }
}
