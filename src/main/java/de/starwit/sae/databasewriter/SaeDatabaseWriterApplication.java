package de.starwit.sae.databasewriter;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SaeDatabaseWriterApplication {

	@Autowired
	private StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer;

	@Value("#{'${redis.stream.keys}'.split(',')}")
	private List<String> streamIds;

	private static final Logger LOG = LoggerFactory.getLogger(SaeDatabaseWriterApplication.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SaeDatabaseWriterApplication.class, args);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) throws SQLException {
		LOG.info("Application context refreshed");
		for (String streamId : this.streamIds) {
			StreamOffset<String> streamOffset = StreamOffset.create(streamId, ReadOffset.lastConsumed());
			streamListenerContainer.receive(streamOffset, new VisionApiListener(this::writeMessage));
			LOG.info("Added subscription for Redis stream \"" + streamId + "\"");
		}
		streamListenerContainer.start();
	}

	private void writeMessage(VisionApiRecord record) {
		LOG.debug("Received message of type " + record.messageType() + " on stream " + record.streamKey() + ": " + record.protoJson());
		String sql = "INSERT INTO messages (message_timestamp, stream_key, message_type, proto_json) VALUES (?::timestamptz, ?, ?, ?::jsonb)";
		jdbcTemplate.update(sql, record.timestamp().toString(), record.streamKey(), record.messageType().name(), record.protoJson());
	}

}
