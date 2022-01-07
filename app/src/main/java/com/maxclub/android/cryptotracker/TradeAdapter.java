package com.maxclub.android.cryptotracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.TradeHolder> {

    public static final int UP = 1;
    public static final int DOWN = 2;

    private final Context mContext;
    private List<Trade> mItems;

    public TradeAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    public List<Trade> getItems() {
        return mItems;
    }

    public void setItems(List<Trade> items) {
        mItems = items;
    }

    @NonNull
    @NotNull
    @Override
    public TradeHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View view = layoutInflater.inflate(viewType == UP ? R.layout.list_item_trade_up : R.layout.list_item_trade_down,
                parent, false);

        return new TradeHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TradeAdapter.TradeHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        Trade trade = mItems.get(position);

        return trade.flags;
    }

    public class TradeHolder extends RecyclerView.ViewHolder {

        private final TextView mTimeTextView;
        private final TextView mMarketTextView;
        private final TextView mPriceTextView;
        private final TextView mAmountTextView;

        public TradeHolder(@NonNull @NotNull View itemView, int viewType) {
            super(itemView);

            mTimeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
            mMarketTextView = (TextView) itemView.findViewById(R.id.market_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.price_text_view);
            mAmountTextView = (TextView) itemView.findViewById(R.id.amount_text_view);
        }

        public void bind(Trade trade) {
            mTimeTextView.setText(DateTimeHelper.getFormattedTime(mContext, new Date((trade.timestamp + 2) * 1000L)));
            mMarketTextView.setText(trade.market);
            mPriceTextView.setText(String.format(Locale.getDefault(), "%.2f", trade.price));
            mAmountTextView.setText(String.format(Locale.getDefault(), "%.6f", trade.quantity));
        }
    }
}
