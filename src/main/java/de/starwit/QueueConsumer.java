package de.starwit;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import de.starwit.visionapi.Messages.SaeMessage;

public class QueueConsumer {

    private final Logger log = LogManager.getLogger(this.getClass());
    private Config config;

    private String url;
    private String user;
    private String pw;

    private DataBaseConnection dbCon;

    private ActiveMQConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public QueueConsumer(DataBaseConnection dbCon) {
        this.config = Config.getInstance();
        this.dbCon = dbCon;
    }

    public void start() {

        url = config.brokerUrl;
        user = config.brokerUsername;
        pw = config.brokerPassword;

        factory = new ActiveMQConnectionFactory("tcp://" + url, user, pw);
        factory.setRetryInterval(1000);
        factory.setRetryIntervalMultiplier(1.0);
        factory.setReconnectAttempts(-1);
        factory.setConfirmationWindowSize(10);
        factory.setClientID(config.brokerClientId);

        log.info("set up queue connection to " 
            + "tcp://" + url + " "
            + config.brokerQueue);

        try {
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue(config.brokerQueue));
            consumer.setMessageListener(new TrackingMessageListener(dbCon));
            log.info("Connected to broker");
        } catch (JMSException e) {
            log.error("couldn't connect to broker " + config.brokerUrl, e);
        }
    }

    public void stop() {
        try {
            consumer.close();
            session.close();
            connection.stop();
        } catch (JMSException e) {
            log.warn("Closing JMS Connection didn't work", e);
        }

        factory.close();
    }

    private class TrackingMessageListener implements MessageListener {
        private final Logger log = LogManager.getLogger(this.getClass());

        private DataBaseConnection dbCon;

        public TrackingMessageListener(DataBaseConnection dbCon) {
            log.info("set up message listener");
            this.dbCon = dbCon;
        }

        @Override
        public void onMessage(Message message) {
            BytesMessage msg = (BytesMessage) message;
            byte[] bytes;
            try {
                bytes = new byte[(int) msg.getBodyLength()];
                msg.readBytes(bytes);
                SaeMessage saeMsg = parseReceivedMessage(bytes);
                if(saeMsg != null) {
                    dbCon.insertNewDetection(saeMsg);
                }

            } catch (JMSException e) {
                log.warn("Can't get bytes", e);
            }
        }

        private SaeMessage parseReceivedMessage(byte[] bytes) {
            SaeMessage msg;
            try {
                msg = SaeMessage.parseFrom(bytes);
                return msg;
            } catch (InvalidProtocolBufferException e) {
                log.warn("can't parse message, returning null", e);
            }

            return null;
        }
    }
}
