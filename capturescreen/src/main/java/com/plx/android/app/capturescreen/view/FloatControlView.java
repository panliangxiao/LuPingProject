package com.plx.android.app.capturescreen.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.plx.android.app.capturescreen.R;

public class FloatControlView extends FrameLayout {

    private static final String TAG = FloatControlView.class.getSimpleName();

    private WindowManager.LayoutParams wmParams;

    //创建浮动窗口设置布局参数的对象
    private WindowManager windowManager;

    private ImageView floatView;

    private boolean showBigger = false;

    public FloatControlView(Context context) {
        super(context);
        initView();
    }

    public FloatControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FloatControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        initWindowManager(showBigger);
        if (!showBigger) {
            removeAllViews();
            LayoutInflater.from(getContext()).inflate(R.layout.sr_float_view_simple, this);
            floatView = findViewById(R.id.sr_float_pop);
            floatView.setOnTouchListener(new OnTouchListener() {
                private int x;
                private int y;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = (int) event.getRawX();
                            y = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int nowX = (int) event.getRawX();
                            int nowY = (int) event.getRawY();
                            int movedX = nowX - x;
                            int movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            wmParams.x -= movedX;
                            wmParams.y += movedY;
                            Log.e(TAG, "MOVEX : " + movedX + ", MOVEY : " + movedY);

                            // 更新悬浮窗控件布局
                            windowManager.updateViewLayout(FloatControlView.this, wmParams);
                            break;
                        default:
                            break;
                    }
                    return false;

                }
            });

            floatView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBigger();
                }
            });
        } else {
            removeAllViews();
            LayoutInflater.from(getContext()).inflate(R.layout.sr_float_view_bigger, this);
            floatView = findViewById(R.id.sr_float_pop);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
                        showSmall();
                    }
                    return false;
                }
            });

        }

        if (!isShown() && !isActivated()) {
            windowManager.addView(this, wmParams);
        } else {
            windowManager.updateViewLayout(this, wmParams);
        }
    }

    private void initWindowManager(boolean showBigger) {
        wmParams = new WindowManager.LayoutParams();

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        //设置window type，TOAST不需要申请权限，7.0以上不再支持TOAST
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT > 24) {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        if (showBigger) {
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        } else {
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        //调整悬浮窗显示的停靠位置为左侧置顶
        if (!showBigger) {
            wmParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        } else {
            wmParams.gravity = Gravity.CENTER;
        }
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void remove() {
        //移除悬浮窗口
        windowManager.removeView(this);
    }

    private void showSmall() {
        showBigger = false;
        initView();
    }

    private void showBigger() {
        showBigger = true;
        initView();
    }
}
