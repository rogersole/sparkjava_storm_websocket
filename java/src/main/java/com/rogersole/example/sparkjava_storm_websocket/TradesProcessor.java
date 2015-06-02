package com.rogersole.example.sparkjava_storm_websocket;

import com.rogersole.example.sparkjava_storm_websocket.storm.StormController;

/**
 * Entry point to process the trades. The Storm topology defines an Spout as the RabbitMQ data
 * source. From that point, different Bolts are used to process the data to finish producing the
 * results to another RabbitMQ queue.
 * 
 * @author rogersole
 *
 */
public class TradesProcessor {

    public static void main(String[] args) throws Exception {

        StormController stormProcessor = new StormController();

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
