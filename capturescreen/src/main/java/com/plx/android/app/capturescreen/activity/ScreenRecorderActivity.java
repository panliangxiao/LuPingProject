package com.plx.android.app.capturescreen.activity;

import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.plx.android.app.base.AbsBaseActivity;
import com.plx.android.app.capturescreen.R;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sr_recorder_main_act);
        startRecorder = findViewById(R.id.sr_recorder_btn);
        startRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecorder();
            }
        });
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
            // NOTE: Should pass this result data into a Service to run ScreenRecorder.
            // The following codes are merely exemplary.

            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mMediaProjection == null) {
                Log.e(TAG, "media projection is null");
            }
        }
    }

    private void startRecorder(){
        requestCheckPermissions(needPermissions, REQUEST_PERMISSIONS);
    }

    @Override
    protected void onPermissionsGranted(int requestCode) {
        super.onPermissionsGranted(requestCode);
        if (mMediaProjection == null){
            requestMediaProjection();
        }
    }

    @Override
    protected void onPermissionsDenied(int requestCode) {
        super.onPermissionsDenied(requestCode);
        Toast.makeText(this, getString(R.string.no_permission_to_write_sd_ard), Toast.LENGTH_LONG).show();
    }

}
