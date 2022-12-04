package com.example.patient.app;

import android.app.Application;

import com.example.patient.BuildConfig;
import com.example.patient.R;
import com.example.patient.TagTree;

import live.videosdk.rtc.android.VideoSDK;
import timber.log.Timber;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoSDK.initialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            Timber.plant(new TagTree(getString(R.string.app_name), true));
        }
    }
}
