package io.latent.storm.rabbitmq;

import java.io.Serializable;

import com.rabbitmq.client.Channel;

public interface Declarator extends Serializable {
    void execute(Channel channel);

    public static class NoOp implements Declarator {
        @Override
        public void execute(Channel channel) {}
    }
}
