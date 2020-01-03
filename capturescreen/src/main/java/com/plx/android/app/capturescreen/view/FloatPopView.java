package com.plx.android.app.capturescreen.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.plx.android.app.capturescreen.R;
import com.plx.android.app.capturescreen.activity.ScreenRecorderActivity;
import com.plx.android.app.capturescreen.constant.RecorderConstants;
import com.plx.android.app.capturescreen.service.FloatService;
import com.plx.android.app.capturescreen.service.ScreenRecorderService;

public class FloatPopView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = FloatPopView.class.getSimpleName();

    private WindowManager.LayoutParams wmParams;

    //创建浮动窗口设置布局参数的对象
    private WindowManager windowManager;

    private ImageView mFloatView;

    private View mStart;
    private View mStop;
    private View mHome;

    private boolean mBiggerState = false;

    private int mX;
    private int mY;

    public FloatPopView(Context context) {
        super(context);
        initView();
    }

    public FloatPopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FloatPopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        initWindowManager();
        if (!mBiggerState) {
            removeAllViews();
            LayoutInflater.from(getContext()).inflate(R.layout.sr_float_view_simple, this);
            mFloatView = findViewById(R.id.sr_float_pop);
            mFloatView.setOnTouchListener(onTouchListener);

            mFloatView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeFloatState();
                }
            });
        } else {
            removeAllViews();
            LayoutInflater.from(getContext()).inflate(R.layout.sr_float_view_bigger, this);
            mStart = findViewById(R.id.sr_recorder_start);
            mStop = findViewById(R.id.sr_recorder_stop);
            mHome = findViewById(R.id.sr_recorder_home);
            mStart.setOnClickListener(this);
            mStop.setOnClickListener(this);
            mHome.setOnClickListener(this);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();
                    Log.e(TAG, "X : " + x + ", Y : " + y);
                    if ((event.getAction() == MotionEvent.ACTION_DOWN)
                            && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
                        changeFloatState();
                        return true;
                    }else if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
                        changeFloatState();
                        return true;
                    }
                    return false;
                }
            });
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "dianji", Toast.LENGTH_LONG).show();
                }
            });

        }

        if (!isShown() && !isActivated()) {
            windowManager.addView(this, wmParams);
        } else {
            windowManager.updateViewLayout(this, wmParams);
        }
    }

    private void initWindowManager() {
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
        if (mBiggerState) {
            wmParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        } else {
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        //调整悬浮窗显示的停靠位置为左侧置顶
        if (!mBiggerState) {
            wmParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        } else {
            wmParams.gravity = Gravity.CENTER;
        }
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        if (!mBiggerState) {
            wmParams.x = mX;
            wmParams.y = mY;
        }else {
            wmParams.x = 0;
            wmParams.y = 0;
        }

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void remove() {
        //移除悬浮窗口
        windowManager.removeView(this);
    }

    private void changeFloatState() {
        mBiggerState = !mBiggerState;
        initView();
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
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
                    mX = wmParams.x -= movedX;
                    mY = wmParams.y += movedY;
                    Log.e(TAG, "MOVEX : " + movedX + ", MOVEY : " + movedY);

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(FloatPopView.this, wmParams);
                    break;
                default:
                    break;
            }
            return false;

        }
    };

    private void startRecorder(){
        if (mIntent != null) {
            //获得录屏权限，启动Service进行录制
            Intent intent = new Intent(getContext(), ScreenRecorderService.class);
            intent.putExtras(mIntent);
            getContext().startService(intent);
        }
    }

    private void stopRecorder(){
        Intent intent = new Intent(getContext(), ScreenRecorderService.class);
        getContext().stopService(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sr_recorder_start){
            startRecorder();
        }else if (id == R.id.sr_recorder_stop){
            stopRecorder();
        }else if (id == R.id.sr_recorder_home){
            Intent intent = new Intent(getContext(), ScreenRecorderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }

    private Intent mIntent = null;

    public void setIntent(Intent intent){
        mIntent = intent;
    }
}
