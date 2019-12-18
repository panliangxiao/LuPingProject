package com.plx.android.app.capturescreen.encoder;

import java.io.IOException;

public interface Encoder {
    void prepare() throws IOException;

    void stop();

    void release();

}