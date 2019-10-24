package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bestom.producetest.R;

import java.io.IOException;

public class Camera2Activity extends AppCompatActivity {
    private Activity mActivity;
    private Context mContext;

    TextureView mTextureView0,mTextureView1;
    Button closeBtn;
    private Camera mCamera0,mCamera1;

    private static final String TAG = "CameraActivity";

    //region TextureView.SurfaceTextureListener
    private TextureView.SurfaceTextureListener mSurfaceTextureListener0=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            try {
                if (mCamera0==null)
                    return;
                mCamera0.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera0.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener1=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            try {
                if (mCamera1==null)
                    return;
                mCamera1.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera1.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera2);

        mActivity=this;
        mContext=this;

        mTextureView0=findViewById(R.id.capture0_preview);
        mTextureView1=findViewById(R.id.capture1_preview);

        closeBtn=findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("request","camera");

                setResult(001,intent);

                finish();
            }
        });

        openCamera();

        mTextureView0.setSurfaceTextureListener(mSurfaceTextureListener0);
        mTextureView1.setSurfaceTextureListener(mSurfaceTextureListener1);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera0!=null){
            mCamera0.release();
            mCamera0=null;
        }
        if (mCamera1!=null){
            mCamera1.release();
            mCamera1=null;
        }
    }

    private void openCamera(){
        if(Camera.getNumberOfCameras()>=2){
            mCamera0= Camera.open(0);
            mCamera1= Camera.open(1);
        }else if (Camera.getNumberOfCameras()==1){
            mCamera0= Camera.open(0);
        }else {
            Toast.makeText(mContext,"The number of cameras is"+ Camera.getNumberOfCameras(), Toast.LENGTH_SHORT).show();
        }

    }


}
