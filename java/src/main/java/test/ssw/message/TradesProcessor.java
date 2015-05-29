package test.ssw.message;

import io.latent.storm.rabbitmq.RabbitMQMessageScheme;
import io.latent.storm.rabbitmq.RabbitMQSpout;
import io.latent.storm.rabbitmq.TupleToMessage;
import io.latent.storm.rabbitmq.config.ConnectionConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfigBuilder;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.ssw.storm.SerializedObjectScheme;
import test.ssw.storm.TradeAnalyserBuyBolt;
import test.ssw.storm.TradeAnalyserSellBolt;
import test.ssw.storm.TradeRouterBolt;
import test.ssw.storm.TradeToMessage;
import test.ssw.util.PropertiesLoader;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.Scheme;
import backtype.storm.topology.TopologyBuilder;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Storm configuration to handle the data aggregation
 * 
 * @author rogersole
 *
 */
public class TradesProcessor {

    Logger                         log = LoggerFactory.getLogger(TradesProcessor.class);

    final private PropertiesLoader queuesProperties;
    final private PropertiesLoader tradesProperties;
    final Scheme                   scheme;
    final TupleToMessage           boltScheme;
    private ConsumerConfig         rabbitMQSpoutConfig;
    private TopologyBuilder        builder;
    private LocalCluster           cluster;

    public TradesProcessor() throws IOException {
        scheme = new RabbitMQMessageScheme(new SerializedObjectScheme(), "messageEnvelope", "messageProperties");
        boltScheme = new TradeToMessage();
        queuesProperties = new PropertiesLoader("queues.properties");
        queuesProperties.loadProperties();
        tradesProperties = new PropertiesLoader("trades.processor.properties");
        tradesProperties.loadProperties();
    }

    public void init()
    {
        log.debug("Initializing Trades Processor (Apache Storm)...");

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
    }

    public void buildTopology()
    {
        log.debug("Building topology...");

        builder = new TopologyBuilder();
        // consumes from RabbitMQ queue, deserializes the object and builds the Trade instances
        builder.setSpout("entry-point-consumer", new RabbitMQSpout(scheme))
                        .addConfigurations(rabbitMQSpoutConfig.asMap())
                        .setMaxSpoutPending(tradesProperties.getInt("rabbitmq_spout_max_spout_pending", 200));
        // splits Trade content between sell and buy related attributes and routes them into
        // different streams
        builder.setBolt("router", new TradeRouterBolt(), 5).shuffleGrouping("entry-point-consumer");
        // analyses the buy-related information
        builder.setBolt("buy-analyser", new TradeAnalyserBuyBolt(), 5).shuffleGrouping("router", "buy");
        // analyses the sell-related information
        builder.setBolt("sell-analyser", new TradeAnalyserSellBolt(), 5).shuffleGrouping("router", "sell");
        // builder.setBolt("to-rabbit", new RabbitMQBolt(boltScheme),
        // 1).shuffleGrouping("aggregator");
    }

    public void createLocalCluster()
    {
        Config conf = new Config();
        conf.setDebug(true);
        conf.setMaxTaskParallelism(3);
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


    public static void main(String[] args) throws Exception {

        TradesProcessor stormProcessor = new TradesProcessor();

        // Adding shutdown hook to finalize all the initialized stuff
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stormProcessor.shutdownCluster();
            }
        });

        stormProcessor.init();
        stormProcessor.buildTopology();
        stormProcessor.createLocalCluster();
    }
}
