package com.maxclub.android.cryptotracker;

import androidx.fragment.app.Fragment;

public class CryptoTrackerActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return CryptoTrackerFragment.newInstance();
    }
}