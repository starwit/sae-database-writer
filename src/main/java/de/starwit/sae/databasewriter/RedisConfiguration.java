package de.starwit.sae.databasewriter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;

@Configuration
public class RedisConfiguration {
    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;
    
    @Bean
    StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        return StreamMessageListenerContainer.create(lettuceConnectionFactory());
    }

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        ClientOptions options = ClientOptions.builder().autoReconnect(true)
                .disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS).build();
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder().clientOptions(options)
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig,
                clientConfig);
        factory.setShareNativeConnection(false);
        return factory;
    }
}
