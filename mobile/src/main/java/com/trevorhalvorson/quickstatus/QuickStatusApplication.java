package com.trevorhalvorson.quickstatus;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by Trevor Halvorson on 1/5/2016.
 */
public class QuickStatusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
