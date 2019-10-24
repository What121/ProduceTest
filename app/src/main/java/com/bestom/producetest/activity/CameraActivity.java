package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.bestom.producetest.R;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.utils.ProperTiesUtils;
import com.bestom.producetest.view.CameraView;

import java.io.IOException;

import static com.bestom.producetest.base.App.configfilename;

/**
 * 测试摄像头
 */
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private Activity mActivity;
    private Context mContext;

    Intent mIntent;

    TextView camView;
    TextView errHint;
    Button swBtn,sw2Button;
    Button closeBtn;
    CameraView cameraView;
    int cameraCount=0;
    private Camera camera;
    private static final int FRONT = 0; //前置摄像头标记
    private static final int BACK = 1; //后置摄像头标记
    private int currentCameraType = -1; //当前打开的摄像头标记

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }
        setContentView(R.layout.activity_camera);

        mActivity=this;
        mContext=this;

        mIntent=new Intent();

        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
//        switch (rotation) {
//            case Surface.ROTATION_0: return 0;
//            case Surface.ROTATION_90: return 90;
//            case Surface.ROTATION_180: return 180;
//            case Surface.ROTATION_270: return 270;
//        }
        Log.d(TAG, "onCreate: rotation"+rotation);

        initView();

        initCamera();
    }

    private void initView() {
        camView = (TextView) findViewById(R.id.cam_view);
        errHint = (TextView) findViewById(R.id.err_hint);
        swBtn = (Button) findViewById(R.id.switch_btn);
        swBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCamera();
            }
        });
        sw2Button=findViewById(R.id.switch2cameras_btn);
        sw2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open2cameras();
            }
        });
        closeBtn = (Button) findViewById(R.id.close_btn);
        cameraView = (CameraView) findViewById(R.id.camera_view);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("request","camera");

                setResult(001,intent);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initCamera();
        Log.d(TAG, "onResume: ");
    }

    
    @Override
    protected void onPause() {
//        camera.stopPreview();
//        camera.release();
//        camera=null;
//        cameraView.setSurfaceTextureListener(null);
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    private void initCamera() {
//        if (checkCamera()) {
//            Log.d(TAG,"NO Camera!");
//            errHint.setVisibility(View.VISIBLE);
//            return;
//        }

        cameraCount = Camera.getNumberOfCameras();
        if (cameraCount>=2){
            swBtn.setVisibility(View.VISIBLE);
            if (Integer.valueOf (ProperTiesUtils.getProperties(mActivity,configfilename,"RK3399server_2camera"))==1){
                sw2Button.setVisibility(View.VISIBLE);
            }
        }

        try {
            camera = openCamera(FRONT);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(1920,1080);
            camera.setParameters(parameters);
            camView.setText(R.string.front_camera);
            mIntent.putExtra("camera","frontcamera");
        } catch (Exception e) {
            e.printStackTrace();
            errHint.setVisibility(View.VISIBLE);
        }

        cameraView.init(camera);
    }

    /**
     * 检查设备是否有摄像头
     */
    private boolean checkCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 打开摄像头
     */
    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
//        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
//            Camera.getCameraInfo(cameraIndex, info);
//            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                frontIndex = cameraIndex;
//            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                backIndex = cameraIndex;
//            }
//        }

        currentCameraType = type;
        if (type == FRONT ) {
            return Camera.open(currentCameraType);
        } else if (type == BACK ) {
            return Camera.open(currentCameraType);
        }
        return null;
    }

    /**
     * 同时打开双摄像头
     */
    private void open2cameras(){
        Intent intent=new Intent();
        intent.putExtra("request","2camera");

        setResult(001,intent);
        finish();
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        try {
            camera.stopPreview();
            camera.release();
            if (currentCameraType == FRONT) {
                camera = openCamera(BACK);
                camView.setText(R.string.back_camera);
                mIntent.putExtra("request","frontcamera");
                errHint.setVisibility(View.GONE);
            } else if (currentCameraType == BACK) {
                camera = openCamera(FRONT);
                camView.setText(R.string.front_camera);
                mIntent.putExtra("request","backcamera");
                errHint.setVisibility(View.GONE);
            }
            camera.setPreviewTexture(cameraView.getSurfaceTexture());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            errHint.setVisibility(View.VISIBLE);
        }
    }
}
