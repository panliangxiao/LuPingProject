package com.plx.android.capturescreen.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {
    public static File getSavedVideoDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "ScreenVideos");
    }
}
