package com.plx.android.app.capturescreen.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

public interface CodecDelegateCallback {
    void onInputBufferAvailable(BaseEncoder encoder, int index);

    void onOutputFormatChanged(BaseEncoder encoder, MediaFormat format);

    void onOutputBufferAvailable(BaseEncoder encoder, int index, MediaCodec.BufferInfo info);

    void onError(BaseEncoder encoder, Exception exception);

}