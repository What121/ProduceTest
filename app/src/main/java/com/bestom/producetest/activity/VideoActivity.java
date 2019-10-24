package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bestom.producetest.R;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.utils.Util;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = VideoActivity.class.getSimpleName();
    private static final String videoUrl = "data/time.mp4";

    TextView mCountdownTv;
    Button closeBtn;
    VideoView videoView;

    private long leftTime = 8 * 60 * 60; // 8小时
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }
        setContentView(R.layout.activity_video);
        initView();
        //开始倒计时
        mHandler.postDelayed(countdownRunnable, 1000);
    }

    /**
     * 初始化
     */
    private void initView() {
        mCountdownTv = (TextView) findViewById(R.id.time_view);
        closeBtn = (Button) findViewById(R.id.close_btn);
        videoView = (VideoView) findViewById(R.id.video_view);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        videoView.setVideoPath(videoUrl);
        videoView.setMediaController(new MediaController(this));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });
    }

    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            leftTime--;
            if (leftTime > 0) {
                // 倒计时效果展示
                mCountdownTv.setText(Util.formatLongToTimeStr(leftTime));
                // 每一秒执行一次
                mHandler.postDelayed(this, 1000);
            } else { // 倒计时结束
                // 处理业务流程
                leftTime = 0;
                mCountdownTv.setText(Util.formatLongToTimeStr(leftTime));
                // 播放视频停止
                videoView.stopPlayback();
                // 发送消息，结束倒计时
                mHandler.removeCallbacks(countdownRunnable);
            }
        }
    };
}
