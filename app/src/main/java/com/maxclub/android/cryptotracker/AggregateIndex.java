package com.maxclub.android.cryptotracker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AggregateIndex extends CryptoCompareResponse implements Parcelable {

    public static final String TYPE = "5";

    @SerializedName("MARKET")
    public String market;

    @SerializedName("FROMSYMBOL")
    public String fromSymbol;

    @SerializedName("TOSYMBOL")
    public String toSymbol;

    @SerializedName("FLAGS")
    public int flags;

    @SerializedName("PRICE")
    public float price;

    @SerializedName("LASTUPDATE")
    public long lastUpdate;

    @SerializedName("MEDIAN")
    public float median;

    @SerializedName("LASTVOLUME")
    public float lastVolume;


    protected AggregateIndex(Parcel in) {
        type = in.readString();
        market = in.readString();
        fromSymbol = in.readString();
        toSymbol = in.readString();
        flags = in.readInt();
        price = in.readFloat();
        lastUpdate = in.readLong();
        median = in.readFloat();
        lastVolume = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(market);
        dest.writeString(fromSymbol);
        dest.writeString(toSymbol);
        dest.writeInt(flags);
        dest.writeFloat(price);
        dest.writeLong(lastUpdate);
        dest.writeFloat(median);
        dest.writeFloat(lastVolume);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AggregateIndex> CREATOR = new Creator<AggregateIndex>() {
        @Override
        public AggregateIndex createFromParcel(Parcel in) {
            return new AggregateIndex(in);
        }

        @Override
        public AggregateIndex[] newArray(int size) {
            return new AggregateIndex[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AggregateIndex that = (AggregateIndex) o;

        if (flags != that.flags) return false;
        if (Float.compare(that.price, price) != 0) return false;
        if (lastUpdate != that.lastUpdate) return false;
        if (Float.compare(that.median, median) != 0) return false;
        if (Float.compare(that.lastVolume, lastVolume) != 0) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (market != null ? !market.equals(that.market) : that.market != null) return false;
        if (fromSymbol != null ? !fromSymbol.equals(that.fromSymbol) : that.fromSymbol != null)
            return false;
        return toSymbol != null ? toSymbol.equals(that.toSymbol) : that.toSymbol == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (market != null ? market.hashCode() : 0);
        result = 31 * result + (fromSymbol != null ? fromSymbol.hashCode() : 0);
        result = 31 * result + (toSymbol != null ? toSymbol.hashCode() : 0);
        result = 31 * result + flags;
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
        result = 31 * result + (median != +0.0f ? Float.floatToIntBits(median) : 0);
        result = 31 * result + (lastVolume != +0.0f ? Float.floatToIntBits(lastVolume) : 0);
        return result;
    }
}
