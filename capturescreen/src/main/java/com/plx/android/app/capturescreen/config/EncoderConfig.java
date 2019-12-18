package com.plx.android.app.capturescreen.config;

import android.media.MediaFormat;

public interface EncoderConfig {

    String codecName();

    MediaFormat buildMediaFormat();

}
