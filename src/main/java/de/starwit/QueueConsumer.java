package de.starwit;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import de.starwit.visionapi.Messages.TrackingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class QueueConsumer {

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    QueueConsumerListener queueConsumerListener;

    private JMSContext context;

    private JMSConsumer consumer;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (context != null) {
            context.close();
        }
        if (consumer != null) {
            consumer.close();
        }
        scheduler.shutdown();
    }

    @Override
    public void run() {
        context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
        consumer = context.createConsumer(context.createQueue("broker.queue"));
        consumer.setMessageListener(queueConsumerListener);
    }
}
