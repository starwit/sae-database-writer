package de.starwit.sae.databasewriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VisionApiDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void insert(VisionApiRecord record) {
		String sql = "INSERT INTO messages (message_timestamp, stream_key, message_type, proto_json) VALUES (?::timestamptz, ?, ?, ?::jsonb)";
		jdbcTemplate.update(sql, record.timestamp().toString(), record.streamKey(), record.messageType().name(), record.protoJson());
	}
    
}