package com.rogersole.example.sparkjava_storm_websocket.storm.declarator;

import io.latent.storm.rabbitmq.Declarator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;

public class QueueDeclarator implements Declarator {

    private final String exchange;
    private final String queue;
    private final String routingKey;

    public QueueDeclarator(String exchange, String queue) {
        this(exchange, queue, "");
    }

    public QueueDeclarator(String exchange, String queue, String routingKey) {
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
    }

    /**
     * At this point, we have the Channel, so we can wire up the exchange/queue bindings.
     * 
     */
    @Override
    public void execute(Channel channel) {
        try {
            Map<String, Object> args = new HashMap<>();
            channel.queueDeclare(queue, true, false, false, args);
            channel.exchangeDeclare(exchange, "topic", true);
            channel.queueBind(queue, exchange, routingKey);
        }
        catch (IOException e) {
            throw new RuntimeException("Error executing rabbitmq declarations.", e);
        }
    }
}
