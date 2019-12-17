package com.plx.android.app.capturescreen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;

import com.plx.android.app.capturescreen.constant.RecorderConstants;
import com.plx.android.app.capturescreen.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenRecorderService extends Service {

    private int resultCode;
    private Intent resultData = null;

    private MediaProjection mediaProjection = null;
    private MediaRecorder mediaRecorder = null;
    private VirtualDisplay virtualDisplay = null;

    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    private Context context = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            resultCode = intent.getIntExtra("resultCode", -1);
            resultData = intent.getParcelableExtra("resultData");
            screenWidth = intent.getIntExtra(RecorderConstants.screen_width, 0);
            screenHeight = intent.getIntExtra(RecorderConstants.screen_height, 0);
            screenDensity = intent.getIntExtra(RecorderConstants.screen_density, 0);
            mediaProjection = createMediaProjection();
            mediaRecorder = createMediaRecorder();
            virtualDisplay = createVirtualDisplay();
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
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

    private MediaRecorder createMediaRecorder() {
        File dir = FileUtils.getSavedVideoDir();
        if (!dir.exists() && !dir.mkdir()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        final File file = new File(dir, "Screenshots-" + format.format(new Date())
                + "-" + screenWidth + "x" + screenHeight + ".mp4");

        //Used to record audio and video. The recording control is based on a simple state machine.
        MediaRecorder mediaRecorder = new MediaRecorder();
        //Set the video source to be used for recording.
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //Set the format of the output produced during recording.
        //3GPP media file format
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //Sets the video encoding bit rate for recording.
        //param:the video encoding bit rate in bits per second.
        mediaRecorder.setVideoEncodingBitRate(5 * screenWidth * screenHeight);
        //Sets the video encoder to be used for recording.
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //Sets the width and height of the video to be captured.
        mediaRecorder.setVideoSize(screenWidth, screenHeight);
        //Sets the frame rate of the video to be captured.
        mediaRecorder.setVideoFrameRate(60);
        try {
            //Pass in the file object to be written.
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            //Prepares the recorder to begin capturing and encoding data.
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        /**
         * name    String: The name of the virtual display, must be non-empty.This value must never be null.
         width int: The width of the virtual display in pixels. Must be greater than 0.
         height    int: The height of the virtual display in pixels. Must be greater than 0.
         dpi   int: The density of the virtual display in dpi. Must be greater than 0.
         flags int: A combination of virtual display flags. See DisplayManager for the full list of flags.
         surface   Surface: The surface to which the content of the virtual display should be rendered, or null if there is none initially.
         callback  VirtualDisplay.Callback: Callback to call when the virtual display's state changes, or null if none.
         handler   Handler: The Handler on which the callback should be invoked, or null if the callback should be invoked on the calling thread's main Looper.
         */
        /**
         * DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
         * Virtual display flag: Allows content to be mirrored on private displays when no content is being shown.
         */
        return mediaProjection.createVirtualDisplay("mediaProjection", screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }
}
