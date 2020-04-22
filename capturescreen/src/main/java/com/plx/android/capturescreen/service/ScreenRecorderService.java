package com.plx.android.capturescreen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.plx.android.capturescreen.R;
import com.plx.android.capturescreen.config.VideoEncodeConfig;
import com.plx.android.capturescreen.constant.RecorderConstants;
import com.plx.android.capturescreen.core.ScreenRecorder;
import com.plx.android.capturescreen.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.plx.android.capturescreen.constant.RecorderConstants.VERBOSE;
import static com.plx.android.capturescreen.core.ScreenRecorder.VIDEO_AVC;

public class ScreenRecorderService extends Service {

    private static final String TAG = ScreenRecorderService.class.getSimpleName();

    private int resultCode;
    private Intent resultData = null;

    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    private ScreenRecorder mRecorder;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            resultCode = intent.getIntExtra(RecorderConstants.result_code, -1);
            resultData = intent.getParcelableExtra(RecorderConstants.result_data);
            screenWidth = intent.getIntExtra(RecorderConstants.screen_width, 0);
            screenHeight = intent.getIntExtra(RecorderConstants.screen_height, 0);
            screenDensity = intent.getIntExtra(RecorderConstants.screen_density, 0);
            mMediaProjection = createMediaProjection();
            if (mMediaProjection == null) {
                if (VERBOSE)
                    Log.e(TAG, "media projection is null");
                return START_NOT_STICKY;
            }
            mMediaProjection.registerCallback(mProjectionCallback, new Handler());
            startCapturing(mMediaProjection);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    private void startCapturing(MediaProjection mediaProjection) {
        VideoEncodeConfig video = createVideoConfig();
        if (video == null) {
            return;
        }

        File dir = FileUtils.getSavedVideoDir();
        if (!dir.exists() && !dir.mkdirs()) {
            cancelRecorder();
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        final File file = new File(dir, "Screenshots-" + format.format(new Date())
                + "-" + video.width + "x" + video.height + ".mp4");
        if (VERBOSE)
            Log.d(TAG, "Create recorder with :" + video + " \n " + "\n " + file);
        mRecorder = newRecorder(mediaProjection, video, file);
        startRecorder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecorder();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public MediaProjection createMediaProjection() {
        /**
         * Use with getSystemService(Class) to retrieve a MediaProjectionManager instance for
         * managing media projection sessions.
         */
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(resultCode, resultData);
        /**
         * Retrieve the MediaProjection obtained from a succesful screen capture request.
         * Will be null if the result from the startActivityForResult() is anything other than RESULT_OK.
         */
    }

    private VideoEncodeConfig createVideoConfig() {
        final String codec = "OMX.qcom.video.encoder.avc";
        if (codec == null) {
            // no selected codec ??
            return null;
        }
        // video size
        int width = screenWidth;
        int height = screenHeight;
        int framerate = 15;
        int iframe = 1; // 关键帧时间1s
        int bitrate = 1200 * 1000;
        MediaCodecInfo.CodecProfileLevel profileLevel = null;
        return new VideoEncodeConfig(width, height, bitrate,
                framerate, iframe, codec, VIDEO_AVC, profileLevel);
    }

    private boolean isLandscape(){
        return false;
    }

    private void startRecorder() {
        if (mRecorder == null) return;
        mRecorder.start();
    }

    private void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
        try {
        } catch (Exception e) {
            //ignored
        }
    }

    private void cancelRecorder() {
        if (mRecorder == null) return;
        Toast.makeText(this, getString(R.string.permission_denied_screen_recorder_cancel), Toast.LENGTH_SHORT).show();
        stopRecorder();
    }

    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if (mRecorder != null) {
                stopRecorder();
            }
        }
    };

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       final File output) {
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        ScreenRecorder r = new ScreenRecorder(video, null, display, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            long startTime = 0;

            @Override
            public void onStop(Throwable error) {
//                runOnUiThread(() -> stopRecorder());
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "Recorder error ! See logcat for more details", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                    output.delete();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(output));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs;
                }
                long time = (presentationTimeUs - startTime) / 1000;
                Log.e(TAG, "onRecording : " + time);
            }
        });
        return r;
    }

    private VirtualDisplay getOrCreateVirtualDisplay(MediaProjection mediaProjection, VideoEncodeConfig config) {
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenRecorder-display0",
                    config.width, config.height, screenDensity /*dpi*/,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null /*surface*/, null, null);
        } else {
            // resize if size not matched
            Point size = new Point();
            mVirtualDisplay.getDisplay().getSize(size);
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay.resize(config.width, config.height, screenDensity);
            }
        }
        return mVirtualDisplay;
    }
}
