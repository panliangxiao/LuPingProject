package com.plx.android.app.capturescreen.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.plx.android.app.capturescreen.R;

public class RecorderSettingActivity extends AppCompatActivity {

    private View btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sr_recorder_setting_act);
        btnBack = findViewById(R.id.sr_setting_back_btn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecorderSettingActivity.this.onBackPressed();
            }
        });
    }
}
