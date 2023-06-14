package de.starwit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


import de.starwit.visionapi.Messages.TrackingOutput;

import org.jboss.logging.Logger;

@ApplicationScoped
public class QueueConsumerListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(InternalQueueConsumerListener.class.getName());

    @Inject
    DetectionService detectionService;

    @ActivateRequestContext
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
