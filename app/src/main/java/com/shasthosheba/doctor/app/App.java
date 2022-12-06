package com.shasthosheba.doctor.app;

import android.app.Application;

import com.shasthosheba.doctor.BuildConfig;
import com.shasthosheba.doctor.R;
import com.shasthosheba.doctor.TagTree;

import timber.log.Timber;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new TagTree(getString(R.string.app_name), true));
        }
    }
}
