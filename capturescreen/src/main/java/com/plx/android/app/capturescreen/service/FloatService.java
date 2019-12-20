package com.plx.android.app.capturescreen.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.plx.android.app.capturescreen.view.FloatPopView;

public class FloatService extends Service {

    private FloatPopView mFloatPopView;

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatPopView = new FloatPopView(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatPopView != null) {
            //移除悬浮窗口
            mFloatPopView.remove();
        }
    }
}
