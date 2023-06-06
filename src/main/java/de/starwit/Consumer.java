package de.starwit;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import de.starwit.visionapi.Messages.TrackingOutput;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

public class Consumer {

    //private static String url = "tcp://localhost:61616";
    private static String url = "tcp://brain01.starwit.home:30062";
    private static String user = "artemis";
    private static String pw = "artemis";

    private static DataBaseConnection dbCon;

    private static ActiveMQConnectionFactory factory;
    public static void main( String[] args ) throws JMSException, InterruptedException {

        dbCon = new DataBaseConnection();
        dbCon.createConnection();

        factory = new ActiveMQConnectionFactory(url,user,pw);
        factory.setRetryInterval(1000);
        factory.setRetryIntervalMultiplier(1.0);
        factory.setReconnectAttempts(-1);
        factory.setConfirmationWindowSize(10);

        Connection connection = factory.createConnection();
        connection.start();
        
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue("detector"));
        consumer.setMessageListener(new MyListener());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    factory.close();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });

        while(true) {
            Thread.sleep(1);
        }
    }

    private static class MyListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            BytesMessage msg = (BytesMessage) message;
            byte[] bytes;
            try {
                bytes = new byte[(int) msg.getBodyLength()];
                msg.readBytes(bytes);                
                TrackingOutput to = parseReceivedMessage(bytes);
                if(to != null) {
                    dbCon.insertNewDetection(to);
                }

            } catch (JMSException e) {
                System.out.println("Can't get bytes " + e.getMessage());
            }
        }

        private TrackingOutput parseReceivedMessage(byte[] bytes) {
            TrackingOutput to;
            try {
                to = TrackingOutput.parseFrom(bytes);
                return to;
            } catch (InvalidProtocolBufferException e) {
                System.out.println("can't parse message, returning null");
            }

            return null;
        }
        
    }    
}
