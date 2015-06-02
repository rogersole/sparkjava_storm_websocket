package com.rogersole.example.sparkjava_storm_websocket.storm.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.rogersole.example.sparkjava_storm_websocket.model.Trade;

public class TradeRouterBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Trade trade = (Trade) input.getValueByField("object");
        collector.emit("sell", new Values(trade.getTimePlaced(), trade.getCurrencyFrom(), trade.getAmountSell(),
                        trade.getRate(), trade.getOriginatingCountry()));
        collector.emit("buy", new Values(trade.getTimePlaced(), trade.getCurrencyTo(), trade.getAmountBuy(),
                        trade.getRate(), trade.getOriginatingCountry()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("sell", new Fields("date", "currency_from", "amount_sell", "rate", "origin_country"));
        declarer.declareStream("buy", new Fields("date", "currency_to", "amount_buy", "rate", "origin_country"));
    }

}
