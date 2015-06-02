package com.rogersole.example.sparkjava_storm_websocket.storm.bolt;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class TradeAnalyserSellBolt extends TradeAnalyser {

    private static final long serialVersionUID = 1L;

    Logger                    log              = LoggerFactory.getLogger(TradeAnalyserSellBolt.class);

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {

        Date date = (Date) tuple.getValueByField("date");
        String currency = tuple.getStringByField("currency_from");
        Float amount = tuple.getFloatByField("amount_sell");
        Float rate = tuple.getFloatByField("rate");
        String origin = tuple.getStringByField("origin_country");

        // log.debug("Date: " + date + ", currency: " + currency + ", amount: " + amount +
        // ", rate: " + rate +
        // ", origin: " + origin);

        numMessagesRead++;
        collector.emit("sell-out", new Values(numMessagesRead));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("sell-out", new Fields("num_messages"));
    }
}
