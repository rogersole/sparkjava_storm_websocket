package test.ssw.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * The endpoint that consumes messages off of the queue. Happens to be runnable.
 * 
 * @author syntx
 *
 */
public class QueueConsumer extends Endpoint implements Runnable, Consumer {

    public QueueConsumer(String host, int port, String username, String password, String endpointName)
                    throws QueueException {
        super(host, port, username, password, endpointName);
    }

    @Override
    public void run() {
        try {
            // start consuming messages. Auto acknowledge messages.
            channel.basicConsume(endpointName, true, this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when consumer is registered.
     */
    @Override
    public void handleConsumeOk(String consumerTag) {
        System.out.println("Consumer " + consumerTag + " registered");
    }

    /**
     * Called when new message is available.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void handleDelivery(String consumerTag, Envelope env, BasicProperties props, byte[] body)
                    throws IOException {
        Map map = (HashMap) SerializationUtils.deserialize(body);
        System.out.println("Message Number " + map.get("message number") + " received.");
    }

    @Override
    public void handleCancel(String consumerTag) {}

    @Override
    public void handleCancelOk(String consumerTag) {}

    @Override
    public void handleRecoverOk(String consumerTag) {}

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {}
}
