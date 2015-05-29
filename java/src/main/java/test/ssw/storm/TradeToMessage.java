package test.ssw.storm;

import io.latent.storm.rabbitmq.TupleToMessage;
import backtype.storm.tuple.Tuple;

public class TradeToMessage extends TupleToMessage {

    @Override
    protected byte[] extractBody(Tuple input) {

        System.out.println("INPUT: " + input.getValueByField("object"));
        return null;
    }

    @Override
    protected String determineExchangeName(Tuple input) {

        System.out.println("INPUT: " + input.getValueByField("object"));
        return null;
    }

}
