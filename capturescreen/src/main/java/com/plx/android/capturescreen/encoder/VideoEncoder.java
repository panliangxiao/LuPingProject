package com.plx.android.capturescreen.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.plx.android.capturescreen.config.EncoderConfig;

import java.util.Objects;

import static com.plx.android.capturescreen.constant.RecorderConstants.VERBOSE;

public class VideoEncoder extends BaseEncoder {
    private static final String TAG = VideoEncoder.class.getSimpleName();

    private EncoderConfig mConfig;
    private Surface mSurface;


    public VideoEncoder(EncoderConfig config) {
        super(config.codecName());
        this.mConfig = config;
    }

    @Override
    protected void onEncoderConfigured(MediaCodec encoder) {
        mSurface = encoder.createInputSurface();
        if (VERBOSE) Log.i(TAG, "VideoEncoder create input surface: " + mSurface);
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.buildMediaFormat();
    }

    /**
     * @throws NullPointerException if prepare() not call
     */
    public Surface getInputSurface() {
        return Objects.requireNonNull(mSurface, "doesn't prepare()");
    }

    @Override
    public void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        super.release();
    }
}
