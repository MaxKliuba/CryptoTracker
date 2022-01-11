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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CryptoCompareClient mCryptoCompareClient;
    private LineChart mLineChart;
    private LineDataSet mPriceLineDataSet;
    private LineDataSet mAvgPriceLineDataSet;
    private RecyclerView mTradeRecyclerView;
    private TradeAdapter mTradeAdapter;
    private List<AggregateIndex> mAggregateIndices;
    private List<Trade> mTrades;
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
            mTimeInterval = savedInstanceState.getInt(KEY_TIME_INTERVAL, 0);
            mLineChart = savedInstanceState.getParcelable(KEY_TRADES);
        } else {
            mAggregateIndices = new ArrayList<>();
            mTrades = new ArrayList<>();
            mTimeInterval = 0;
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_layout_color);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.swipe_refresh_layout_background_color);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reconnectCryptoCompareClient();

                mAggregateIndices.clear();
                mActionBar.setSubtitle(null);
                mLineChart.clear();
                initLineData();

                mTrades.clear();
                mTradeAdapter.setItems(mTrades);
                mTradeAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mLineChart = (LineChart) view.findViewById(R.id.chart);
        initChart();

        mTradeRecyclerView = (RecyclerView) view.findViewById(R.id.trade_recycler_view);
        mTradeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mTradeAdapter = new TradeAdapter(getActivity());
        mTradeRecyclerView.setAdapter(mTradeAdapter);

        mCryptoCompareClient = new CryptoCompareClient();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        reconnectCryptoCompareClient();
    }

    @Override
    public void onStop() {
        super.onStop();

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

    public void reconnectCryptoCompareClient() {
        mCryptoCompareClient.disconnect();

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
                                    addEntry(aggregateIndex);

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

                        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete()");
                    }
                });
    }

    private void initChart() {
        mLineChart.setBackgroundColor(getResources().getColor(R.color.color_surface));
        mLineChart.setNoDataTextColor(getResources().getColor(R.color.color_on_surface));
        mLineChart.setDrawGridBackground(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setPinchZoom(true);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));
        xAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);

        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setTextColor(getResources().getColor(R.color.color_on_surface));
        yAxis.setAxisLineColor(getResources().getColor(R.color.color_surface));

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setEnabled(false);

        mLineChart.invalidate();

        initLineData();
    }

    private void initLineData() {
        LineData lineData = mLineChart.getData();

        if (lineData == null) {
            lineData = new LineData();
            mLineChart.setData(lineData);
        }

        mPriceLineDataSet = createPriceLineDataSet();
        lineData.addDataSet(mPriceLineDataSet);
        mAvgPriceLineDataSet = createAvgPriceLineDataSet();
        lineData.addDataSet(mAvgPriceLineDataSet);

        if (mAggregateIndices.size() > 0) {
            setEntries(mAggregateIndices);
        }
    }

    private void setEntries(List<AggregateIndex> aggregateIndices) {
        for (int i = 0; i < aggregateIndices.size(); i++) {
            mPriceLineDataSet.addEntry(new Entry(mPriceLineDataSet.getEntryCount(), aggregateIndices.get(i).price));
        }

        mLineChart.getData().notifyDataChanged();
        mLineChart.notifyDataSetChanged();

        mLineChart.setVisibleXRangeMaximum(6);
        mLineChart.moveViewTo(mLineChart.getData().getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);

    }

    private void addEntry(AggregateIndex aggregateIndex) {
        mPriceLineDataSet.addEntry(new Entry(mPriceLineDataSet.getEntryCount(), aggregateIndex.price));
        if (mTimeInterval > 0 && mPriceLineDataSet.getEntryCount() % mTimeInterval == 0) {
            int startIndex = Math.max(0, mPriceLineDataSet.getEntryCount() - mTimeInterval - 1);
            int endIndex = mPriceLineDataSet.getEntryCount() - 1;
            if (mAvgPriceLineDataSet.getEntryCount() != 0) {
                startIndex = (int) mAvgPriceLineDataSet.getEntryForIndex(mAvgPriceLineDataSet.getEntryCount() - 1).getX();
            }

            float sum = 0;
            for (int i = startIndex; i <= endIndex; i++) {
                sum += mPriceLineDataSet.getEntryForIndex(i).getY();
            }

            float avgPrice = sum / (endIndex - startIndex + 1);

            mAvgPriceLineDataSet.addEntry(new Entry(startIndex, avgPrice));
            mAvgPriceLineDataSet.addEntry(new Entry(endIndex, avgPrice));
        }
        mLineChart.getData().notifyDataChanged();
        mLineChart.notifyDataSetChanged();

        mLineChart.setVisibleXRangeMaximum(6);
        mLineChart.moveViewTo(mLineChart.getData().getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);

    }

    private LineDataSet createPriceLineDataSet() {
        LineDataSet set = new LineDataSet(null, "Price");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.01f);
        set.setDrawFilled(true);
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(getResources().getColor(R.color.color_primary));
        set.setCircleColor(getResources().getColor(R.color.color_primary));
        set.setCircleHoleColor(getResources().getColor(R.color.color_surface));
        set.setHighLightColor(getResources().getColor(R.color.color_on_surface));
        set.setFillColor(getResources().getColor(R.color.color_primary));
        set.setFillAlpha(50);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextColor(getResources().getColor(R.color.color_on_surface));
        set.setValueTextSize(8f);

        return set;
    }

    private LineDataSet createAvgPriceLineDataSet() {
        LineDataSet set = new LineDataSet(null, "Avg Price");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setLineWidth(2.0f);
        set.setDrawCircles(false);
        set.setColor(getResources().getColor(R.color.color_on_surface));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);

        return set;
    }
}
