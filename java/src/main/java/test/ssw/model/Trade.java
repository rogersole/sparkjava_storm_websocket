package test.ssw.model;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;



/**
 * Class to map the input message.
 */
public class Trade implements Serializable {

    // JSON parsing performed by Google's Gson library
    private static Gson       gson             = new GsonBuilder().registerTypeAdapter(Trade.class,
                                                               new AnnotatedDeserializer<Trade>())
                                                               .create();

    private static final long serialVersionUID = 190702288786471614L;

    @JsonRequired
    private String            userId;
    @JsonRequired
    private String            currencyFrom;
    @JsonRequired
    private String            currencyTo;
    @JsonRequired
    private Float             amountSell;
    @JsonRequired
    private Float             amountBuy;
    @JsonRequired
    private Float             rate;
    @JsonRequired
    private Date              timePlaced;
    @JsonRequired
    private String            originatingCountry;

    public Trade() {}

    public static Trade fromJson(String content) throws Exception {
        return gson.fromJson(content, Trade.class);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Trade[");
        sb.append("userId=").append(userId);
        sb.append(", currencyFrom=").append(currencyFrom);
        sb.append(", currencyTo=").append(currencyTo);
        sb.append(", amountSell=").append(amountSell);
        sb.append(", amountBuy=").append(amountBuy);
        sb.append(", rate=").append(rate);
        sb.append(", timePlaced=").append(timePlaced);
        sb.append(", originatingCountry=").append(originatingCountry).append("]");

        return sb.toString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(String currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(String currencyTo) {
        this.currencyTo = currencyTo;
    }

    public Float getAmountSell() {
        return amountSell;
    }

    public void setAmountSell(Float amountSell) {
        this.amountSell = amountSell;
    }

    public Float getAmountBuy() {
        return amountBuy;
    }

    public void setAmountBuy(Float amountBuy) {
        this.amountBuy = amountBuy;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Date getTimePlaced() {
        return timePlaced;
    }

    public void setTimePlaced(Date timePlaced) {
        this.timePlaced = timePlaced;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
    }
}


/**
 * Class to validate that all the annotated as @JsonRequired fields are present in the request body.
 * 
 * @param <T>
 */
class AnnotatedDeserializer<T> implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        T pojo = new GsonBuilder().setDateFormat("dd-MMM-yy HH:mm:ss").create().fromJson(je, type);

        Field[] fields = pojo.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getAnnotation(JsonRequired.class) != null) {
                try {
                    f.setAccessible(true);
                    if (f.get(pojo) == null) { throw new JsonParseException("Missing field in JSON: " + f.getName()); }
                }
                catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    System.err.println(ex);
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    System.err.println(ex);
                }
            }
        }
        return pojo;

    }
}


/**
 * Annotation interface
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface JsonRequired {}
