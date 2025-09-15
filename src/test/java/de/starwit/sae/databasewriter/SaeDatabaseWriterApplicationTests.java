package de.starwit.sae.databasewriter;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
	RedisAutoConfiguration.class,
	DataSourceAutoConfiguration.class
})
class SaeDatabaseWriterApplicationTests {

	@MockitoBean
	JdbcTemplate jdbcTemplate;

	@MockitoBean
	StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;

	// TODO Fix the test s.t. it starts the context without redis and db available
	// @Test
	// void contextLoads() {
	// }

}
