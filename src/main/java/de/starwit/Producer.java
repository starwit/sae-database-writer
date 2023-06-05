package de.starwit;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.google.protobuf.ByteString;

import de.starwit.visionapi.Messages.BoundingBox;
import de.starwit.visionapi.Messages.Detection;
import de.starwit.visionapi.Messages.TrackedDetection;
import de.starwit.visionapi.Messages.TrackingOutput;

public class Producer {
    private static String url = "tcp://brain01.starwit.home:30062";
    private static String user = "artemis";
    private static String pw = "artemis";

    private static ActiveMQConnectionFactory factory;
    public static void main( String[] args ) throws JMSException, InterruptedException {
        factory = new ActiveMQConnectionFactory(url,user,pw);
        factory.setRetryInterval(1000);
        factory.setRetryIntervalMultiplier(1.0);
        factory.setReconnectAttempts(-1);
        factory.setConfirmationWindowSize(10);

        Connection connection = factory.createConnection();
        connection.start();
        
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(session.createQueue("detector"));
        for (int i = 0; i<100; i++) {
            BytesMessage msg = session.createBytesMessage();
            msg.writeBytes(createSerializedMessage(i));
            producer.send(msg);
            System.out.println("Message send " + i + " " + msg);
            Thread.sleep(100);
        }
        producer.close();
        session.close();
        connection.close();
    }

    private static byte[] createSerializedMessage(int counter) {

        BoundingBox bb =  BoundingBox.newBuilder()
            .setMinX(1)
            .setMinY(1)
            .setMaxX(10)
            .setMaxY(10)
            .build();
        Detection d = Detection.newBuilder()
            .setBoundingBox(bb)
            .setConfidence((float)0.5)
            .setClassId(counter)
            .build();

        byte[] sampleTrackingID = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        TrackedDetection td = TrackedDetection.newBuilder()
            .setDetection(d)
            .setObjectId(ByteString.copyFrom(sampleTrackingID))
            .build();

        TrackingOutput to = TrackingOutput.newBuilder()
            .addTrackedDetections(td)
            .build();

        return to.toByteArray();
    }
}
