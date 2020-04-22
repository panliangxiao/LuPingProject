package com.plx.android.capturescreen.config;

import android.media.MediaFormat;

public interface EncoderConfig {

    String codecName();

    MediaFormat buildMediaFormat();

}
