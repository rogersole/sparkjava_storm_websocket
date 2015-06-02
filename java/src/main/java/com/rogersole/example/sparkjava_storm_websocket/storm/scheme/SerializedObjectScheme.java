package com.rogersole.example.sparkjava_storm_websocket.storm.scheme;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import com.rogersole.example.sparkjava_storm_websocket.model.Trade;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;

public class SerializedObjectScheme implements Scheme {

    private static final long serialVersionUID = -7734176307841199017L;

    public SerializedObjectScheme() {}

    @Override
    public List<Object> deserialize(byte[] bytes) {
        final Trade trade = (Trade) SerializationUtils.deserialize(bytes);
        return Collections.singletonList(trade);
    }

    /**
     * Emits tuples containing only one field, named "object".
     */
    @Override
    public Fields getOutputFields() {
        return new Fields("object");
    }
}
