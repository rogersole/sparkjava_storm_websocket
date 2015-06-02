package com.rogersole.example.sparkjava_storm_websocket.sparkjava.queue;

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;

/**
 * The producer endpoint that writes to the queue.
 * 
 * @author rogersole
 *
 */
public class Producer extends Endpoint {

    public Producer(String host, int port, String username, String password, String exchange, String routingKey)
                    throws QueueException {
        super(host, port, username, password, exchange, routingKey);
    }

    public void sendMessage(Serializable obj) throws QueueException {
        try {
            channel.basicPublish(exchange, routingKey, null, SerializationUtils.serialize(obj));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new QueueException("Captured " + ex.getClass().getName() + " exception while publishing to "
                            + routingKey);
        }
    }
}
