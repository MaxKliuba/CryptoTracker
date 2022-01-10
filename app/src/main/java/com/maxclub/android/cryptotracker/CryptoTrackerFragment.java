package com.maxclub.android.cryptotracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CryptoTrackerFragment extends Fragment {

    private static final String TAG = "WebSocket";

    private static final String KEY_TRADES = "CryptoTrackerFragment.mTrades";
    private static final String KEY_AGGREGATE_INDICES = "CryptoTrackerFragment.mAggregateIndices";
    private static final String KEY_TIME_INTERVAL = "CryptoTrackerFragment.mTimeInterval";

    private static final int REQUEST_EDIT_TIME_INTERVAL = 0;
    public static final String DIALOG_EDIT_TIME_INTERVAL = "CryptoTrackerFragment.DialogEditTimeInterval";

    private ActionBar mActionBar;
    private LineChart mLineChart;
    private RecyclerView mTradeRecyclerView;
    private TradeAdapter mTradeAdapter;
    private List<AggregateIndex> mAggregateIndices;
    private List<Trade> mTrades;
    private CryptoCompareClient mCryptoCompareClient;
    private int mTimeInterval;

    public static CryptoTrackerFragment newInstance() {
        return new CryptoTrackerFragment();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mAggregateIndices = savedInstanceState.getParcelableArrayList(KEY_AGGREGATE_INDICES);
            mTrades = savedInstanceState.getParcelableArrayList(KEY_TRADES);
            mTimeInterval = savedInstanceState.getInt(KEY_TIME_INTERVAL, 1);
        } else {
            mAggregateIndices = new ArrayList<>();
            mTrades = new ArrayList<>();
            mTimeInterval = 1;
        }

        setHasOptionsMenu(true);
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

        mTradeRecyclerView = (RecyclerView) view.findViewById(R.id.trade_recycler_view);
        mTradeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mTradeAdapter = new TradeAdapter(getActivity());
        mTradeRecyclerView.setAdapter(mTradeAdapter);

        mCryptoCompareClient = new CryptoCompareClient();
        mCryptoCompareClient.connect()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable disposable) {
                        Log.i(TAG, "onSubscribe()");
                    }

                    @Override
                    public void onNext(@NotNull String s) {
                        Log.i(TAG, "onNext() -> " + s);

                        Gson gson = (new GsonBuilder()).create();
                        CryptoCompareResponse cryptoCompareResponse = gson.fromJson(s, CryptoCompareResponse.class);

                        switch (cryptoCompareResponse.type) {
                            case AggregateIndex.TYPE:
                                AggregateIndex aggregateIndex = gson.fromJson(s, AggregateIndex.class);

                                if (aggregateIndex.price > 0) {
                                    mAggregateIndices.add(aggregateIndex);
                                    setData();

                                    mActionBar.setSubtitle(String.format(Locale.getDefault(), "%.2f $", aggregateIndex.price));
                                }
                                break;
                            case Trade.TYPE:
                                Trade trade = gson.fromJson(s, Trade.class);

                                mTrades.add(0, trade);
                                mTradeAdapter.setItems(mTrades);
                                mTradeAdapter.notifyDataSetChanged();
                                break;
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        Log.e(TAG, "onError()", throwable);
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete()");
                    }
                });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCryptoCompareClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_AGGREGATE_INDICES, (ArrayList<? extends Parcelable>) mAggregateIndices);
        outState.putParcelableArrayList(KEY_TRADES, (ArrayList<? extends Parcelable>) mTrades);
        outState.putInt(KEY_TIME_INTERVAL, mTimeInterval);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.crypto_tracker_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                FragmentManager manager = getActivity().getSupportFragmentManager();
                EditTimeIntervalDialogFragment dialog = EditTimeIntervalDialogFragment.newInstance(mTimeInterval);
                dialog.setTargetFragment(CryptoTrackerFragment.this, REQUEST_EDIT_TIME_INTERVAL);
                dialog.show(manager, DIALOG_EDIT_TIME_INTERVAL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        switch (requestCode) {
            case REQUEST_EDIT_TIME_INTERVAL:
                if (resultCode == Activity.RESULT_OK) {
                    mTimeInterval = data.getIntExtra(EditTimeIntervalDialogFragment.EXTRA_TIME_INTERVAL, mTimeInterval);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void initChart(View view) {
        mLineChart = view.findViewById(R.id.chart);
        mLineChart.setViewPortOffsets(0, 0, 0, 0);
        mLineChart.setBackgroundColor(getResources().getColor(R.color.color_surface));
        mLineChart.setNoDataTextColor(getResources().getColor(R.color.color_on_surface));
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setPinchZoom(true);
        mLineChart.setDrawGridBackground(false);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setLabelCount(5, false);
        xAxis.setTextSize(16.0f);
        xAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + ""; //DateTimeHelper.getFormattedTime(getActivity(), new Date((mAggregateIndices.get((int) value).lastUpdate + 2 * 3600) * 1000L));
            }
        });

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(16.0f);
        yAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        yAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.animateXY(1000, 1000);

        setData();
    }

    private void setData() {

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = Math.max(0, mAggregateIndices.size() - 10); i < mAggregateIndices.size(); i++) {
            values.add(new Entry(i, mAggregateIndices.get(i).price));
        }

        LineDataSet set1;

        if (mLineChart.getData() != null && mLineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        }

        // create a dataset and give it a type
        set1 = new LineDataSet(values, "Price");

        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setCubicIntensity(0.1f);
        set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(1.8f);
        set1.setCircleRadius(4f);
        set1.setCircleColor(getResources().getColor(R.color.color_on_surface));
        set1.setHighLightColor(getResources().getColor(R.color.color_on_surface));
        set1.setColor(getResources().getColor(R.color.color_primary));
        set1.setFillColor(getResources().getColor(R.color.color_primary));
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
        data.setValueTextColor(getResources().getColor(R.color.color_on_surface));
        data.setValueTextSize(9f);
        data.setDrawValues(true);

        // set data

        mLineChart.setData(data);
        mLineChart.invalidate();
    }
}
