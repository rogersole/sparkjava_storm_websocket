package io.latent.storm.rabbitmq;

import io.latent.storm.rabbitmq.config.ConnectionConfig;
import io.latent.storm.rabbitmq.config.ProducerConfig;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.ReportedFailedException;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQProducer implements Serializable {
    private final Declarator           declarator;

    private transient Logger           logger;

    private transient ConnectionConfig connectionConfig;
    private transient Connection       connection;
    private transient Channel          channel;
    private transient Map              config;

    private boolean                    blocked = false;

    public RabbitMQProducer()
    {
        this(new Declarator.NoOp());
    }

    public RabbitMQProducer(Declarator declarator) {
        this.declarator = declarator;
    }

    public void send(Message message) {
        if (message == Message.NONE) return;
        sendMessageWhenNotBlocked((Message.MessageForSending) message);
    }

    private void sendMessageWhenNotBlocked(Message.MessageForSending message) {
        while (true) {
            if (blocked) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } else {
                sendMessageActual(message);
                return;
            }
        }
    }

    private void sendMessageActual(Message.MessageForSending message) {

        reinitIfNecessary();
        if (channel == null) throw new ReportedFailedException("No connection to RabbitMQ");
        try {
            // AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            // .contentType(message.getContentType())
            // .contentEncoding(message.getContentEncoding())
            // .deliveryMode((message.isPersistent()) ? 2 : 1)
            // .headers(message.getHeaders())
            // .build();
            channel.basicPublish(message.getExchangeName(), message.getRoutingKey(), /* properties */null,
                            message.getBody());
        }
        catch (AlreadyClosedException ace) {
            ace.printStackTrace();
            logger.error("already closed exception while attempting to send message", ace);
            reset();
            throw new ReportedFailedException(ace);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error("io exception while attempting to send message", ioe);
            reset();
            throw new ReportedFailedException(ioe);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.warn("Unexpected error while sending message. Backing off for a bit before trying again (to allow time for recovery)",
                            e);
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void open(final Map config) {
        logger = LoggerFactory.getLogger(RabbitMQProducer.class);
        connectionConfig = ProducerConfig.getFromStormConfig(config).getConnectionConfig();
        this.config = config;
        internalOpen();
    }

    private void internalOpen() {
        try {
            connection = createConnection();
            channel = connection.createChannel();
            String exchangeName = (String) config.get("rabbitmq.exchangeName");
            channel.exchangeDeclare(exchangeName, "direct");
            String routingKey = (String) config.get("rabbitmq.routingKey");
            channel.queueDeclare(routingKey, false, false, false, null);
            channel.queueBind(routingKey, exchangeName, "");
            // channel.exchangeBind(destination, source, routingKey)

            // run any declaration prior to message sending
            declarator.execute(channel);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("could not open connection on rabbitmq", e);
            reset();
        }
    }

    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.debug("error closing channel", e);
        }
        try {
            logger.info("closing connection to rabbitmq: " + connection);
            connection.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.debug("error closing connection", e);
        }
        channel = null;
        connection = null;
    }

    private void reset() {
        channel = null;
    }

    private void reinitIfNecessary() {
        if (channel == null) {
            close();
            internalOpen();
        }
    }

    private Connection createConnection() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = connectionConfig.asConnectionFactory();
        Connection connection =
                        connectionConfig.getHighAvailabilityHosts().isEmpty() ? connectionFactory.newConnection()
                                        : connectionFactory.newConnection(connectionConfig.getHighAvailabilityHosts()
                                                        .toAddresses());
        connection.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause) {
                logger.error("shutdown signal received", cause);
                reset();
            }
        });
        connection.addBlockedListener(new BlockedListener()
        {
            @Override
            public void handleBlocked(String reason) throws IOException
            {
                blocked = true;
                logger.warn(String.format("Got blocked by rabbitmq with reason = %s", reason));
            }

            @Override
            public void handleUnblocked() throws IOException
            {
                blocked = false;
                logger.warn(String.format("Got unblocked by rabbitmq"));
            }
        });
        logger.info("connected to rabbitmq: " + connection);
        return connection;
    }
}
