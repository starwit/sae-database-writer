package de.starwit;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

public class App {

    //private static String url = "tcp://localhost:61616";
    private static String url = "tcp://brain01.starwit.home:32224";
    private static String user = "artemis";
    private static String pw = "artemis";

    private static ActiveMQConnectionFactory factory;
    public static void main( String[] args ) throws JMSException, InterruptedException {
        System.out.println( "Hello World!" );
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
                    System.out.println("Shutting down ...");
                    factory.close();
    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });

        while(true) {
            Thread.sleep(50);
        }
    }

    private static class MyListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            System.out.println("Message type " + message.getClass().getName());
            BytesMessage msg = (BytesMessage) message;
            byte[] bytes;
            try {
                bytes = new byte[(int) msg.getBodyLength()];
                msg.readBytes(bytes);
            } catch (JMSException e) {
                System.out.println("Can't get bytes");
                e.printStackTrace();
            }
            
            System.out.println("Consumer " + Thread.currentThread().getName() + " received message: " + msg.toString());
        }
        
    }    
}
