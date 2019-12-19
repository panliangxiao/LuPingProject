package com.plx.android.app.capturescreen.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.plx.android.app.base.AbsBaseActivity;
import com.plx.android.app.capturescreen.R;
import com.plx.android.app.capturescreen.constant.RecorderConstants;
import com.plx.android.app.capturescreen.service.FloatControlService;
import com.plx.android.app.capturescreen.utils.CheckFloatWindowUtil;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ScreenRecorderActivity extends AbsBaseActivity {

    private static final String TAG = ScreenRecorderActivity.class.getSimpleName();

    private String[] needPermissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};

    private static final int REQUEST_MEDIA_PROJECTION = 0x0001;
    private static final int REQUEST_PERMISSIONS = 0x0002;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private ImageView startRecorder;
    private ImageView stopRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sr_recorder_main_act);
        startRecorder = findViewById(R.id.sr_recorder_btn);
        stopRecorder = findViewById(R.id.sr_stop_recorder_btn);
        startRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecorder();
            }
        });
        stopRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecorder();
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
            //获得录屏权限，启动Service进行录制
            Intent intent = new Intent(this, FloatControlService.class);
            intent.putExtra(RecorderConstants.result_code, resultCode);
            intent.putExtra(RecorderConstants.result_data, data);
            //获取资源对象
            Resources resources = getResources();
            //获取屏幕数据
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
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
        }
    }

    private void openFloatView(){
        if (Build.VERSION.SDK_INT > 24 && !CheckFloatWindowUtil.checkPermission(this)) {
            CheckFloatWindowUtil.applyPermission(this);
        } else {
            Intent intent = new Intent(this, FloatControlService.class);
            startService(intent);
        }
    }

    private void startRecorder() {
        openFloatView();
//        if (mMediaProjection == null) {
//            requestMediaProjection();
//        }
    }

    private void stopRecorder(){
        Intent intent = new Intent(this, FloatControlService.class);
        stopService(intent);
    }

    @Override
    protected void onPermissionsGranted(int requestCode) {
        super.onPermissionsGranted(requestCode);

    }

    @Override
    protected void onPermissionsDenied(int requestCode) {
        super.onPermissionsDenied(requestCode);
        Toast.makeText(this, getString(R.string.no_permission_to_write_sd_ard), Toast.LENGTH_LONG).show();
    }

}
