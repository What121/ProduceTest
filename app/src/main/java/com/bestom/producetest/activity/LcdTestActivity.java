package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.bestom.producetest.R;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.view.LcdTestView;
import com.bestom.producetest.view.ResultDialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * T102平板 LCD 测试
 */
public class LcdTestActivity extends AppCompatActivity {
    private static final String TAG = "LcdTestActivity";
    
    private Context mContext;
    private Activity mActivity;

    LcdTestView mLcdView;
    Button mCloseLcdBtn;

    private boolean isTesting=false;
    private Timer mTimer;
    int mTestCount = 0;
    private static final int TEST_COLOR_COUNT = 5;
    private static final int TEST_COLORS[] = new int[TEST_COLOR_COUNT];


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }
        super.onCreate(savedInstanceState);

        mContext=this;
        mActivity=this;

        TEST_COLORS[0] = Color.WHITE;
        TEST_COLORS[1] = Color.BLACK;
        TEST_COLORS[2] = Color.RED;
        TEST_COLORS[3] = Color.GREEN;
        TEST_COLORS[4] = Color.BLUE;

        setContentView(R.layout.activity_lcd_test);
        mLcdView = (LcdTestView) findViewById(R.id.lcd_view);
        mLcdView.setVisibility(View.GONE);
        mCloseLcdBtn = (Button) findViewById(R.id.close_lcd_btn);
        mCloseLcdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("request","screen");
                setResult(001,intent);

                finish();
            }
        });
        mCloseLcdBtn.setVisibility(View.GONE);

        autotest();

    }

    private void autotest(){
        mTimer=new Timer();
        isTesting=true;
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTestCount <=4) {
                            mCloseLcdBtn.setVisibility(View.GONE);
                            mLcdView.setVisibility(View.VISIBLE);
                            mLcdView.setBackgroundColor(TEST_COLORS[mTestCount]);
                            mTestCount++;
                        }else {
                            mLcdView.paneBorder(false);
                            mLcdView.grayScale(false);
                            mTestCount = 0;
                            mLcdView.setVisibility(View.GONE);
//                    mCloseLcdBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        } ,500, 2000);

    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            isTesting=false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            mTestCount++;
//
//            if (mTestCount >= 1 && mTestCount <= TEST_COLOR_COUNT) {
//                mLcdView.setBackgroundColor(TEST_COLORS[mTestCount - 1]);
//            }
//
//            switch (mTestCount) {
//                case 1:
//                    mCloseLcdBtn.setVisibility(View.GONE);
//                    mLcdView.setVisibility(View.VISIBLE);
//                    break;
//                case TEST_COLOR_COUNT + 1:
//                    mLcdView.grayScale(true);
//                    mLcdView.paneBorder(false);
//                    mLcdView.postInvalidate();
//                    break;
//                case TEST_COLOR_COUNT + 2:
//                    mLcdView.paneBorder(true);
//                    mLcdView.grayScale(false);
//                    mLcdView.postInvalidate();
//                    break;
//                case TEST_COLOR_COUNT + 3:
//                    mLcdView.paneBorder(false);
//                    mLcdView.grayScale(false);
//                    mTestCount = 0;
//                    mLcdView.setVisibility(View.GONE);
//                    mCloseLcdBtn.setVisibility(View.VISIBLE);
//                    break;
//                default:
//                    break;
//            }
            if (isTesting){
                stopTimer();

                mLcdView.paneBorder(false);
                mLcdView.grayScale(false);
//                mTestCount = 0;
                mLcdView.setVisibility(View.GONE);
                mCloseLcdBtn.setVisibility(View.VISIBLE);
            }else {
                autotest();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            mTestCount++;
            if (mTestCount >= 1 && mTestCount <= TEST_COLOR_COUNT) {
                mLcdView.setBackgroundColor(TEST_COLORS[mTestCount - 1]);
            }
            switch (mTestCount) {
                case 1:
                    mCloseLcdBtn.setVisibility(View.GONE);
                    mLcdView.setVisibility(View.VISIBLE);
                    break;
                case TEST_COLOR_COUNT + 1:
                    mLcdView.grayScale(true);
                    mLcdView.paneBorder(false);
                    mLcdView.postInvalidate();
                    break;
                case TEST_COLOR_COUNT + 2:
                    mLcdView.paneBorder(true);
                    mLcdView.grayScale(false);
                    mLcdView.postInvalidate();
                    break;
                case TEST_COLOR_COUNT + 3:
                    mLcdView.paneBorder(false);
                    mLcdView.grayScale(false);
                    mTestCount = 0;
                    mLcdView.setVisibility(View.GONE);
                    mCloseLcdBtn.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
