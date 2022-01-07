package com.maxclub.android.cryptotracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CryptoTrackerFragment extends Fragment {

    private static final String TAG = "WebSocket";

    private ActionBar mActionBar;
    private LineChart mLineChart;

    public static CryptoTrackerFragment newInstance() {
        return new CryptoTrackerFragment();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        new WebSocketManager(new EchoWebSocketListener());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crypto_tracker, container, false);

        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_bitcoin);
        mActionBar.setTitle(getString(R.string.btc_usd_title));

        initChart(view);

        return view;
    }

    private void initChart(View view) {
        mLineChart = view.findViewById(R.id.chart);
        mLineChart.setViewPortOffsets(0, 0, 0, 0);
        mLineChart.setBackgroundColor(getResources().getColor(R.color.color_surface));

        // no description text
        mLineChart.getDescription().setEnabled(false);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);

        mLineChart.setDrawGridBackground(false);
        mLineChart.setMaxHighlightDistance(300);

        XAxis xAxis = mLineChart.getXAxis();
        //x.setEnabled(false);
        xAxis.setLabelCount(6, false);
        xAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        xAxis.setTextSize(16.0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);

        YAxis yAxis = mLineChart.getAxisLeft();
        //y.setTypeface(tfLight);
        yAxis.setLabelCount(6, false);
        yAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        yAxis.setTextSize(16.0f);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        yAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));
        yAxis.setYOffset(-9f);

        mLineChart.getAxisRight().setEnabled(false);

        mLineChart.getLegend().setEnabled(false);

        //mLineChart.animateXY(2000, 2000);

        setData(20, 10);

        // don't forget to refresh the drawing
        mLineChart.invalidate();
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * (range + 1)) + 20;
            values.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (mLineChart.getData() != null &&
                mLineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.1f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(getResources().getColor(R.color.color_on_surface));
            set1.setHighLightColor(getResources().getColor(R.color.color_on_surface));
            set1.setColor(getResources().getColor(R.color.teal_700));
            set1.setFillColor(getResources().getColor(R.color.teal_700));
            set1.setFillAlpha(50);
            set1.setDrawHorizontalHighlightIndicator(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return mLineChart.getAxisLeft().getAxisMinimum();
                }
            });

            // create a data object with the data sets
            LineData data = new LineData(set1);
            //data.setValueTypeface(tfLight);
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            mLineChart.setData(data);
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i(TAG, "WebSocket.onOpen()");

            JSONArray jsonArray = new JSONArray();
            jsonArray.put("5~CCCAGG~BTC~USD");
            jsonArray.put("0~Coinbase~BTC~USD");
            jsonArray.put("0~Cexio~BTC~USD");

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("action", "SubAdd");
                jsonObject.put("subs", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            webSocket.send(jsonObject.toString());
        }

        @Override
        public void onMessage(WebSocket webSocket, String message) {
            Log.i(TAG, "WebSocket.onMessage() -> " + message);

            Gson gson = (new GsonBuilder()).create();
            CryptoCompareResponse cryptoCompareResponse = gson.fromJson(message, CryptoCompareResponse.class);

            switch (cryptoCompareResponse.type) {
                case AggregateIndex.TYPE:
                    AggregateIndex aggregateIndex = gson.fromJson(message, AggregateIndex.class);

                    Observable.just(aggregateIndex)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<AggregateIndex>() {
                                @Override
                                public void accept(AggregateIndex aggregateIndex) throws Exception {
                                    mActionBar.setSubtitle(String.format(Locale.getDefault(), "%.2f $", aggregateIndex.price));
                                }
                            });

                    break;
                case Trade.TYPE:
                    Trade trade = gson.fromJson(message, Trade.class);

//                    getActivity().runOnUiThread(() ->
//                            Toast.makeText(getActivity(), trade.toString(), Toast.LENGTH_SHORT).show()
//                    );
                    break;
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.i(TAG, "WebSocket.onClosing() -> " + reason);
            webSocket.close(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
            Log.i(TAG, "WebSocket.onFailure() -> " + throwable.getMessage());
        }
    }
}
