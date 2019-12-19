package com.plx.android.app.capturescreen.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.plx.android.app.capturescreen.view.FloatControlView;

public class FloatControlService extends Service {

    private FloatControlView floatControlView;

    @Override
    public void onCreate() {
        super.onCreate();
        floatControlView = new FloatControlView(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatControlView != null) {
            //移除悬浮窗口
            floatControlView.remove();
        }
    }
}
