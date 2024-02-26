package de.starwit;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import de.starwit.visionapi.Messages.SaeMessage;
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
        this.streamOffsetById = config.redisStreamIds.stream()
            .collect(Collectors.toMap(id -> String.format("%s:%s", config.redisInputStreamPrefix, id), id -> new StreamEntryID(0)));
        this.jedis = null;
    }

    @Override
    public void run() {
        this.running = true;

        while (this.running) {
            if (!this.connect()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("Thread was interrupted while waiting after redis connection failure", e);
                }
                continue;
            }

            List<Entry<String,List<StreamEntry>>> result = null;

            try {
                result = jedis.xread(this.xReadParams, this.streamOffsetById);
            } catch (RuntimeException ex) {
                log.error("Redis stream read (XREAD) failed", ex);
                this.close();
                this.sleep(Duration.ofSeconds(1));
            }

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
                        SaeMessage msg = SaeMessage.parseFrom(Base64.getDecoder().decode(proto_b64));
                        dbConnection.insertNewDetection(msg);
                    } catch (InvalidProtocolBufferException e) {
                        log.warn("Error decoding proto from message. streamId={}", streamId, e);
                    }
                }
            }

            this.sleep(Duration.ofMillis(100));
        }

        this.close();
    }

    private boolean connect() {
        if (this.jedis == null) {
            try {
                this.jedis = new JedisPooled(config.redisHost, config.redisPort);
                log.info("Successfully connected to Redis instance at {}:{}", config.redisHost, config.redisPort);
                return true;
            } catch (RuntimeException ex) {
                log.error("Could not connect to Redis instance", ex);
                this.close();
                return false;
            }
        } else {
            return true;
        }
    }

    private void close() {
        if (this.jedis != null) {
            try {
                this.jedis.close();
            } catch (RuntimeException ex) {
                log.warn("Error closing Redis connection", ex);
            }
            this.jedis = null;
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            log.warn("Thread was interrupted while sleeping", ex);
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
