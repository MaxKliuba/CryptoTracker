package com.maxclub.android.cryptotracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CryptoTrackerFragment extends Fragment {

    private ActionBar mActionBar;

    public static CryptoTrackerFragment newInstance() {
        return new CryptoTrackerFragment();
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
        mActionBar.setSubtitle(String.format(Locale.getDefault(), "%.2f $", 47128.145));

        return view;
    }
}
