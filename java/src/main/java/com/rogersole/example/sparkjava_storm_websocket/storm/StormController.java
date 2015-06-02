package com.rogersole.example.sparkjava_storm_websocket.storm;

import io.latent.storm.rabbitmq.RabbitMQBolt;
import io.latent.storm.rabbitmq.RabbitMQMessageScheme;
import io.latent.storm.rabbitmq.RabbitMQSpout;
import io.latent.storm.rabbitmq.config.ConnectionConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfigBuilder;
import io.latent.storm.rabbitmq.config.ProducerConfig;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.Scheme;
import backtype.storm.topology.TopologyBuilder;

import com.rabbitmq.client.ConnectionFactory;
import com.rogersole.example.sparkjava_storm_websocket.storm.bolt.TradeAnalyserBuyBolt;
import com.rogersole.example.sparkjava_storm_websocket.storm.bolt.TradeAnalyserSellBolt;
import com.rogersole.example.sparkjava_storm_websocket.storm.bolt.TradeRouterBolt;
import com.rogersole.example.sparkjava_storm_websocket.storm.scheme.SerializedObjectScheme;
import com.rogersole.example.sparkjava_storm_websocket.storm.scheme.TradeToMessage;
import com.rogersole.example.sparkjava_storm_websocket.util.PropertiesLoader;

/**
 * Storm configuration to handle the data aggregation
 * 
 * @author rogersole
 *
 */
public class StormController {

    Logger                         log = LoggerFactory.getLogger(StormController.class);

    final private PropertiesLoader queuesProperties;
    final private PropertiesLoader tradesProperties;
    private Scheme                 spoutScheme;
    private TradeToMessage         boltScheme;
    private ConsumerConfig         rabbitMQSpoutConfig;
    private ProducerConfig         rabbitMQBuyBoltConfig;
    private ProducerConfig         rabbitMQSellBoltConfig;
    private TopologyBuilder        builder;
    private LocalCluster           cluster;

    public StormController() throws IOException {
        queuesProperties = new PropertiesLoader("queues.properties");
        queuesProperties.loadProperties();
        tradesProperties = new PropertiesLoader("trades.processor.properties");
        tradesProperties.loadProperties();
    }

    public void init()
    {
        log.debug("Initializing Trades Processor (Apache Storm)...");

        // initializing schema definitions
        spoutScheme = new RabbitMQMessageScheme(new SerializedObjectScheme(), "msgEnvelope", "msgProperties");
        boltScheme = new TradeToMessage();

        // create the connection configuration
        ConnectionConfig connectionConfig = new ConnectionConfig(
                        queuesProperties.get("trades_consumer_queue_host", "localhost"),
                        queuesProperties.getInt("trades_consumer_queue_port", 5672),
                        queuesProperties.get("trades_consumer_queue_user", "guest"),
                        queuesProperties.get("trades_consumer_queue_pswd", "guest"),
                        ConnectionFactory.DEFAULT_VHOST,
                        queuesProperties.getInt("trades_consumer_queue_heartbeat", 10));

        // create the spout config object
        rabbitMQSpoutConfig = new ConsumerConfigBuilder().connection(connectionConfig)
                        .queue(queuesProperties.get("trades_consumer_queue_name", "trades_consumer_queue"))
                        .prefetch(200)
                        .requeueOnFail()
                        .build();

        // create the bolt config object to handle buy stuff
        ConnectionConfig buyConnConfig =
                        new ConnectionConfig(queuesProperties.get("trades_buy_processed_queue_host", "localhost"),
                                        queuesProperties.getInt("trades_buy_processed_queue_port", 5672),
                                        queuesProperties.get("trades_buy_processed_queue_user", "guest"),
                                        queuesProperties.get("trades_buy_processed_queue_pswd", "guest"),
                                        ConnectionFactory.DEFAULT_VHOST,
                                        queuesProperties.getInt("trades_buy_processed_queue_heartbeat", 10));
        rabbitMQBuyBoltConfig = new ProducerConfig(buyConnConfig,
                        queuesProperties.get("trades_buy_processed_exchange", "buy_exchange"),
                        queuesProperties.get("trades_buy_processed_routingkey", "trades_buy_processed_queue"),
                        "", "", false);

        // create the bolt config object to handle sell stuff
        ConnectionConfig sellConnConfig =
                        new ConnectionConfig(queuesProperties.get("trades_sell_processed_queue_host", "localhost"),
                                        queuesProperties.getInt("trades_sell_processed_queue_port", 5672),
                                        queuesProperties.get("trades_sell_processed_queue_user", "guest"),
                                        queuesProperties.get("trades_sell_processed_queue_pswd", "guest"),
                                        ConnectionFactory.DEFAULT_VHOST,
                                        queuesProperties.getInt("trades_sell_processed_queue_heartbeat", 10));
        rabbitMQSellBoltConfig = new ProducerConfig(buyConnConfig,
                        queuesProperties.get("trades_sell_processed_exchange", "sell_exchange"),
                        queuesProperties.get("trades_sell_processed_routingkey", "trades_sell_processed_queue"),
                        "", "", false);
    }

    public void buildTopology()
    {
        log.debug("Building topology...");

        builder = new TopologyBuilder();
        // consumes from RabbitMQ queue, deserializes the object and builds the Trade instances
        builder.setSpout("entry-point-consumer", new RabbitMQSpout(spoutScheme))
                        .addConfigurations(rabbitMQSpoutConfig.asMap())
                        .setMaxSpoutPending(tradesProperties.getInt("rabbitmq_spout_max_spout_pending", 200));

        // splits Trade content between sell and buy related attributes and routes them into
        // different streams
        builder.setBolt("router", new TradeRouterBolt()).shuffleGrouping("entry-point-consumer");
        // analyses the buy-related information
        builder.setBolt("buy-analyser", new TradeAnalyserBuyBolt()).shuffleGrouping("router", "buy");
        // analyses the sell-related information
        builder.setBolt("sell-analyser", new TradeAnalyserSellBolt()).shuffleGrouping("router", "sell");
        // publishes the buy analysis results to a rabbitMQ queue
        builder.setBolt("buy-rabbitmq", new RabbitMQBolt(boltScheme))
                        .addConfigurations(rabbitMQBuyBoltConfig.asMap())
                        .shuffleGrouping("buy-analyser", "buy-out");
        // publishes the sell analysis results to a rabbitMQ queue
        builder.setBolt("sell-rabbitmq", new RabbitMQBolt(boltScheme))
                        .addConfigurations(rabbitMQSellBoltConfig.asMap())
                        .shuffleGrouping("sell-analyser", "sell-out");
    }

    public void createLocalCluster()
    {
        Config conf = new Config();
        conf.setDebug(true);
        int parallelism = tradesProperties.getInt("parallelism", 5);
        conf.setMaxTaskParallelism(parallelism);
        log.debug("Creating local cluster (it takes time)...");
        cluster = new LocalCluster();
        log.debug("Created!");
        cluster.submitTopology("trades-processor", conf, builder.createTopology());
    }

    public void shutdownCluster()
    {
        if (cluster != null)
            cluster.shutdown();
    }
}
