package com.plx.android.app.capturescreen.activity;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.plx.android.app.capturescreen.R;
import com.plx.android.app.capturescreen.constant.RecorderConstants;
import com.plx.android.app.capturescreen.service.FloatService;
import com.plx.android.app.capturescreen.utils.CheckFloatWindowUtil;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ScreenRecorderActivity extends AbsBaseActivity {

    private static final String TAG = ScreenRecorderActivity.class.getSimpleName();

    private String[] needPermissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};

    private static final int REQUEST_MEDIA_PROJECTION = 0x0001;
    private static final int REQUEST_PERMISSIONS = 0x0002;

    private static final int REQUEST_SYSTEM_ALERT_WINDOW_CODE = 0x0003;

    private MediaProjectionManager mMediaProjectionManager;

    private View mStartRecorder;
    private TextView tvRecorderSetting;

    private int mResultCode = -1;
    private Intent mResultData = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sr_recorder_main_act);
        mStartRecorder = findViewById(R.id.sr_recorder_btn);
        tvRecorderSetting = findViewById(R.id.sr_recorder_set_btn);
        mStartRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecorder();
            }
        });
        tvRecorderSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenRecorderActivity.this, RecorderSettingActivity.class));
            }
        });
//        requestCheckPermissions(needPermissions, REQUEST_PERMISSIONS);
        requestCheckPermissions(needPermissions, REQUEST_PERMISSIONS);


    }

    private void requestMediaProjection() {
        mMediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            mResultCode = resultCode;
            mResultData = data;
        }else if (requestCode == REQUEST_SYSTEM_ALERT_WINDOW_CODE){
            if (CheckFloatWindowUtil.checkPermission(this)) {
                Intent intent = new Intent(this, FloatService.class);
                startService(intent);
            }
        }
    }

    private void openFloatView(){
        if (!CheckFloatWindowUtil.checkPermission(this)) {
            CheckFloatWindowUtil.requestPermission(this, REQUEST_SYSTEM_ALERT_WINDOW_CODE);
        } else {
            Intent intent = new Intent(this, FloatService.class);
            startService(intent);
        }
    }

    private void startRecorder() {
//        openFloatView();
        if (mResultData != null) {
            //获得录屏权限，启动Service进行录制
            Intent intent = new Intent(this, FloatService.class);
            intent.putExtra(RecorderConstants.result_code, mResultCode);
            intent.putExtra(RecorderConstants.result_data, mResultData);
            //获取屏幕数据
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            //获取屏幕宽高，单位是像素
            int widthPixels = displayMetrics.widthPixels;
            int heightPixels = displayMetrics.heightPixels;
            //获取屏幕密度倍数
            int density = (int) displayMetrics.density;
            intent.putExtra(RecorderConstants.screen_width, widthPixels);
            intent.putExtra(RecorderConstants.screen_height, heightPixels);
            intent.putExtra(RecorderConstants.screen_density, density);
            startService(intent);
            Toast.makeText(this, "录屏开始", Toast.LENGTH_SHORT).show();
            moveTaskToBack(true);
        }else {
            requestMediaProjection();
        }
    }

    @Override
    protected void onPermissionsGranted(int requestCode) {
        super.onPermissionsGranted(requestCode);
        if (requestCode == REQUEST_PERMISSIONS){
            requestMediaProjection();
        }
    }

    @Override
    protected void onPermissionsDenied(int requestCode) {
        super.onPermissionsDenied(requestCode);
        Toast.makeText(this, getString(R.string.no_permission_to_write_sd_ard), Toast.LENGTH_LONG).show();
    }

}
