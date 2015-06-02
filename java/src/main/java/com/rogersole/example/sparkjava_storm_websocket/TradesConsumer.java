package com.rogersole.example.sparkjava_storm_websocket;

import com.rogersole.example.sparkjava_storm_websocket.sparkjava.SparkController;

/**
 * Entry point to consume the initial trades published by the Python simulate_trades.py module. The
 * SparkController starts the listener to the post /trade endpoint and enqueues the messages
 * received (and validated) to RabbitMQ queue.
 * 
 * @author rogersole
 *
 */
public class TradesConsumer {

    public static void main(String[] args) throws Exception {

        SparkController apiConsumer = new SparkController();

        // Adding shutdown hook to finalize all the initialized stuff
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                apiConsumer.close();
            }
        });

        apiConsumer.init();
    }
}
