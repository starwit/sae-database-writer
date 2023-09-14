package de.starwit;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import de.starwit.visionapi.Messages.TrackingOutput;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

public class RedisConsumer implements Runnable {

    private static final Logger log = LogManager.getLogger(RedisConsumer.class);

    Config config = Config.getInstance();

    private Boolean running = false;
    private Thread thread;
    
    private DataBaseConnection dbConnection;
    
    private JedisPooled jedis;
    private XReadParams xReadParams;
    private Map<String,StreamEntryID> streamOffsetById;
    
    public RedisConsumer(DataBaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.xReadParams = new XReadParams().count(5).block(2000);
        this.jedis = new JedisPooled(config.redisHost, config.redisPort);
        this.streamOffsetById = config.redisStreamIds.stream()
            .collect(Collectors.toMap(id -> String.format("%s:%s", config.redisInputStreamPrefix, id), id -> StreamEntryID.LAST_ENTRY));
    }

    @Override
    public void run() {
        this.running = true;

        while (this.running) {
            List<Entry<String,List<StreamEntry>>> result = jedis.xread(this.xReadParams, this.streamOffsetById);

            if (result == null) {
                continue;
            }

            for (Entry<String,List<StreamEntry>> resultEntry : result) {
                String streamId = resultEntry.getKey();
                List<StreamEntry> messages = resultEntry.getValue();
                for (StreamEntry message : messages) {
                    // Set last retrieved id
                    this.streamOffsetById.put(streamId, message.getID());
                    String proto_b64 = message.getFields().get("proto_data_b64");
                    try {
                        TrackingOutput proto = TrackingOutput.parseFrom(Base64.getDecoder().decode(proto_b64));
                        dbConnection.insertNewDetection(proto);
                    } catch (InvalidProtocolBufferException e) {
                        log.warn("Error decoding proto from message. streamId={}", streamId, e);
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void start() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void stop() {
        this.running = false;
    }
    
}
