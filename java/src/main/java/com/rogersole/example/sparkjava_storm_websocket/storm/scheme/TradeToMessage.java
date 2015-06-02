package com.rogersole.example.sparkjava_storm_websocket.storm.scheme;

import io.latent.storm.rabbitmq.TupleToMessage;

import org.apache.commons.lang.SerializationUtils;

import backtype.storm.tuple.Tuple;

public class TradeToMessage extends TupleToMessage {

    private static final long serialVersionUID = 1L;

    @Override
    protected byte[] extractBody(Tuple input) {
        System.out.println("INPUT: " + input);
        int numMsg = input.getIntegerByField("num_messages");
        return SerializationUtils.serialize(numMsg);
    }

    @Override
    protected String determineExchangeName(Tuple input) {
        String streamId = input.getSourceStreamId();
        if ("buy-out".equals(streamId)) return "buy_exchange";
        if ("sell-out".equals(streamId)) return "sell_exchange";
        return "";
    }

    @Override
    protected String determineRoutingKey(Tuple input) {
        // String streamId = input.getSourceStreamId();
        // if ("buy-out".equals(streamId)) return "buy_exchange";
        // if ("sell-out".equals(streamId)) return "sell_exchange";
        return "";
    }
}
