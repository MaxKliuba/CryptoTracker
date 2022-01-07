package com.maxclub.android.cryptotracker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Trade extends CryptoCompareResponse implements Parcelable {

    public static final String TYPE = "0";

    @SerializedName("M")
    public String market;

    @SerializedName("FSYM")
    public String fromSymbol;

    @SerializedName("TSYM")
    public String toSymbol;

    @SerializedName("F")
    public int flags;

    @SerializedName("ID")
    public String id;

    @SerializedName("TS")
    public long timestamp;

    @SerializedName("Q")
    public float quantity;

    @SerializedName("P")
    public float price;

    protected Trade(Parcel in) {
        type = in.readString();
        market = in.readString();
        fromSymbol = in.readString();
        toSymbol = in.readString();
        flags = in.readInt();
        id = in.readString();
        timestamp = in.readLong();
        quantity = in.readFloat();
        price = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(market);
        dest.writeString(fromSymbol);
        dest.writeString(toSymbol);
        dest.writeInt(flags);
        dest.writeString(id);
        dest.writeLong(timestamp);
        dest.writeFloat(quantity);
        dest.writeFloat(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Trade> CREATOR = new Creator<Trade>() {
        @Override
        public Trade createFromParcel(Parcel in) {
            return new Trade(in);
        }

        @Override
        public Trade[] newArray(int size) {
            return new Trade[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        if (flags != trade.flags) return false;
        if (timestamp != trade.timestamp) return false;
        if (Float.compare(trade.quantity, quantity) != 0) return false;
        if (Float.compare(trade.price, price) != 0) return false;
        if (type != null ? !type.equals(trade.type) : trade.type != null) return false;
        if (market != null ? !market.equals(trade.market) : trade.market != null) return false;
        if (fromSymbol != null ? !fromSymbol.equals(trade.fromSymbol) : trade.fromSymbol != null)
            return false;
        if (toSymbol != null ? !toSymbol.equals(trade.toSymbol) : trade.toSymbol != null)
            return false;
        return id != null ? id.equals(trade.id) : trade.id == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (market != null ? market.hashCode() : 0);
        result = 31 * result + (fromSymbol != null ? fromSymbol.hashCode() : 0);
        result = 31 * result + (toSymbol != null ? toSymbol.hashCode() : 0);
        result = 31 * result + flags;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (quantity != +0.0f ? Float.floatToIntBits(quantity) : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "market='" + market + '\'' +
                ", fromSymbol='" + fromSymbol + '\'' +
                ", toSymbol='" + toSymbol + '\'' +
                ", flags=" + flags +
                ", id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
