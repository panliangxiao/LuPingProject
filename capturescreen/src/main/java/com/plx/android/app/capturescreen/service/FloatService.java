package com.plx.android.app.capturescreen.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.plx.android.app.capturescreen.view.FloatPopView;

public class FloatService extends Service {

    private FloatPopView mFloatPopView;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("FloatService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("FloatService", "onStartCommand");
        if (mFloatPopView == null){
            mFloatPopView = new FloatPopView(getApplicationContext());
        }
        mFloatPopView.setIntent(intent);
        return super.onStartCommand(intent, flags, startId);
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
            mFloatPopView = null;
        }
    }
}
