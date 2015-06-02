package com.rogersole.example.sparkjava_storm_websocket;

import com.rogersole.example.sparkjava_storm_websocket.sparkjava.SparkController;
import com.rogersole.example.sparkjava_storm_websocket.storm.StormController;

public class Main {

    private static SparkController apiConsumer    = null;
    private static StormController stormProcessor = null;

    public static void main(String args[]) throws Exception {

        // Adding shutdown hook to finalize all the initialized stuff
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (apiConsumer != null)
                    apiConsumer.close();
                if (stormProcessor != null)
                    stormProcessor.shutdownCluster();
            }
        });

        initTradesConsumer();
        initTradesProcessor();
    }

    private static void initTradesConsumer() throws Exception {
        apiConsumer = new SparkController();
        apiConsumer.init();
    }

    private static void initTradesProcessor() throws Exception {
        stormProcessor = new StormController();
        stormProcessor.init();
        stormProcessor.buildTopology();
        stormProcessor.createLocalCluster();
    }
}
