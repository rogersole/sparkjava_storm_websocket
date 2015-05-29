package test.ssw.message;

import static spark.SparkBase.awaitInitialization;
import static spark.SparkBase.threadPool;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Spark;
import test.ssw.model.Trade;
import test.ssw.queue.Producer;
import test.ssw.queue.QueueException;
import test.ssw.util.PropertiesLoader;


/**
 * This class is the main entry point for the consumer part. It develops a simple POST processor
 * which accepts JSON formatted input with the format:
 * 
 * Call example: - Headers: Authentication: 4a5048e271c2742aa0418cf3c13d9b67 - Body: { "userId":
 * "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10,
 * "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR" }
 * 
 * Once the message is validated and consumed, it is published to a queue.
 * 
 * @author rogersole
 *
 */
public class TradesConsumer {

    Logger                         log = LoggerFactory.getLogger(TradesConsumer.class);

    final private PropertiesLoader tradesProperties;
    final private PropertiesLoader queuesProperties;
    private Producer               producer;

    public TradesConsumer() throws IOException {
        tradesProperties = new PropertiesLoader("trades.consumer.properties");
        tradesProperties.loadProperties();
        queuesProperties = new PropertiesLoader("queues.properties");
        queuesProperties.loadProperties();
    }

    public void init() {
        log.debug("Initializing Trades Consumer (Sparkjava)...");

        // define sparkjava threads characteristics
        threadPool(tradesProperties.getInt("max_threads", 8), tradesProperties.getInt("min_threads", 2),
                        tradesProperties.getInt("timeout_millis", 30000));


        // Basic authentication defined in the environment variables
        String authenticationAccepted = loadEnvOrDefault("SPARK_AUTHENTICATION", "4a5048e271c2742aa0418cf3c13d9b67");
        // Basic authentication performed before routing
        // NOTE: more complex authentication (based on SSL) can be done using 'Spark.secure' method
        // before the routings.
        Spark.before((request, response) -> {
            String authentication = request.headers("Authentication");
            if (!authentication.equals(authenticationAccepted)) {
                Spark.halt(401, "Authentication failed");
            }
        });

        // defining routes: the POST one
        // secure(keystoreFile, keystorePassword, truststoreFile, truststorePassword);
        Spark.post("/trade", (request, response) -> {

            String content = request.body();

            // check whether content is empty or not
                        if (content.length() == 0) {
                            response.status(400);
                            response.body("Empty requests cannot be processed");
                            log.error("Received and empty request");
                            return "INVALID EMPTY JSON";
                        }

                        try {
                            Trade trade = Trade.fromJson(content);
                            producer.sendMessage(trade);
                            response.status(202); // request has been accepted
                        return "MESSAGE PROCESSED";
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        response.status(400);
                        log.error("Received an invalid JSON body request");
                        return "INVALID JSON";
                    }
                });

        // method to alter all the responses headers to add the author
        Spark.after((request, response) -> response.header("author", "roger(dot)sole(at)gmail(dot)com"));

        // initialize queue producer
        try {
            producer = new Producer(queuesProperties.get("trades_consumer_queue_host", "localhost"),
                            queuesProperties.getInt("trades_consumer_queue_port", 5672),
                            queuesProperties.get("trades_consumer_queue_user", "guest"),
                            queuesProperties.get("trades_consumer_queue_pswd", "guest"),
                            queuesProperties.get("trades_consumer_queue_name", "trades_consumer_queue"));
        }
        catch (QueueException ex) {
            log.error(ex.toString());
            System.exit(-1);
        }

        // lambda runnable to wait Jetty initialization, placed after routes definition
        Runnable healthCheck = () -> awaitInitialization();
        healthCheck.run();
    }

    public void close() {
        if (producer != null)
            try {
                producer.close();
            }
            catch (QueueException dummy) {}
    }


    public static void main(String[] args) throws Exception {

        TradesConsumer apiConsumer = new TradesConsumer();

        // Adding shutdown hook to finalize all the initialized stuff
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                apiConsumer.close();
            }
        });

        apiConsumer.init();
    }

    /**
     * Loads the environment variable requested, and if not found returns the default value.
     * 
     * @param envVar: Environment variable to be loaded
     * @param defaultReturn: String to be returned if env var not found
     * @return String
     */
    private static String loadEnvOrDefault(String envVar, String defaultReturn) {
        String value = System.getenv(envVar);
        if (value == null)
            value = defaultReturn;
        return value;
    }
}
