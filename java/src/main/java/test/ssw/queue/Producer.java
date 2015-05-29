package test.ssw.queue;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;

/**
 * The producer endpoint that writes to the queue.
 * 
 * @author rogersole
 *
 */
public class Producer extends Endpoint {

    public Producer(String host, int port, String username, String password, String endpointName)
                    throws QueueException {
        super(host, port, username, password, endpointName);
    }

    public void sendMessage(Serializable obj) throws QueueException {
        try {
            channel.basicPublish("", endpointName, null, SerializationUtils.serialize(obj));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            throw new QueueException("Captured " + ex.getClass().getName() + " exception while publishing to "
                            + endpointName);
        }
    }
}
