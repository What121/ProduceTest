package com.bestom.producetest.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bestom.producetest.activity.TestStatus;

@SuppressLint("AppCompatCustomView")
public class TestView extends TextView {
    private final static String TAG = TestView.class.getSimpleName();
    public static final int SUCC_COLOR = Color.GREEN;
    public static final int FAIL_COLOR = Color.RED;
    public static final int TEST_COLOR = Color.YELLOW;
    public static final int WAIT_COLOR = Color.GRAY;

    private float center_x = 0;
    private float center_y = 0;
    private float ball_radius = 0;
    private final static float SCALE = 2.0f;

    private TestStatus current_status = TestStatus.WAITING;
    private Paint mPaint = new Paint();

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        center_x = this.getWidth() / 2.0f;
        center_y = this.getHeight() / 2.0f;
        ball_radius = this.getWidth() > this.getHeight() ? this.getHeight() / SCALE : this.getWidth() / SCALE;
    }

    @Override
    public void draw(Canvas canvas) {
        int color = WAIT_COLOR;
        switch (current_status) {
            case WAITING:
                color = WAIT_COLOR;
                break;
            case TESTING:
                color = TEST_COLOR;
                break;
            case FAILED:
                color = FAIL_COLOR;
                break;
            case SUCCEED:
                color = SUCC_COLOR;
                break;
        }
        mPaint.setColor(color);
        canvas.drawCircle(center_x, center_y, ball_radius / 2, mPaint);

        super.draw(canvas);
    }

    public void setStatus(TestStatus tmp) {
        this.current_status = tmp;
        this.invalidate();
    }

    public TestStatus getStatus() {
        return current_status;
    }
}
