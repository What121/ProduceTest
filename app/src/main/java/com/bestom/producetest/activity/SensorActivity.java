package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestom.producetest.R;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.utils.ProperTiesUtils;

import static com.bestom.producetest.base.App.configfilename;

public class SensorActivity extends AppCompatActivity  implements View.OnClickListener {
    private static final String TAG = "SensorActivity";
    private Activity mActivity;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mGSensor,mLSensor;

    private GSensorListener mGSensorListener;
    private LightSensorListener mLightSensorListener;

    LinearLayout GSensor_layout,LSensor_layout;
    TextView tv_x,tv_y,tv_z,tv_g;

    private class LightSensorListener implements SensorEventListener {

        private float lux; // 光线强度

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            // 获取光线强度
            lux = event.values[0];
            Log.d(TAG, "lux : " + lux);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_z.setText(""+lux);
                }
            });
            Log.i(TAG, "onSensorChanged: Lsensorevent type"+event.sensor.getType());
        }

    }

    private class GSensorListener implements SensorEventListener {

        //第四步：必须重写的两个方法：onAccuracyChanged，onSensorChanged
        /**
         * 传感器精度发生改变的回调接口
         */
        @Override
        public final void onAccuracyChanged(Sensor sensor, final int accuracy) {
            //TODO 在传感器精度发生改变时做些操作，accuracy为当前传感器精度

        }
        /**
         * 传感器事件值改变时的回调接口：执行此方法的频率与注册传感器时的频率有关
         */
        @Override
        public final void onSensorChanged(SensorEvent event) {
            // 大部分传感器会返回三个轴方向x,y,x的event值，值的意义因传感器而异
            final float x = event.values[0];
            final float y = event.values[1];
            final float z = event.values[2];
            //TODO 利用获得的三个float传感器值做些操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_x.setText(""+x);
                    tv_y.setText(""+y);
                    tv_z.setText(""+z);
                }
            });
//            Log.i(TAG, "onSensorChanged: Gsensorevent type"+event.sensor.getType());
        }

    }


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity=this;
        mContext=this;
        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }
        setContentView(R.layout.activity_sensor);

        initview();

        //第一步：通过getSystemService获得SensorManager实例对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //第二步：通过SensorManager实例对象获得想要的传感器对象:参数决定获取哪个传感器
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Success! There's a magnetometer.
            mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            // Failure! No magnetometer.
            Log.e(TAG,"Failure! No support Sensor.TYPE_ACCELEROMETER.");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            // Success! There's a magnetometer.
            mLSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        } else {
            // Failure! No magnetometer.
            Log.e(TAG,"Failure! No support Sensor.TYPE_LIGHT.");
        }

    }

    private void initview(){
        GSensor_layout= (LinearLayout) findViewById(R.id.Gsensor_test);
        tv_x= (TextView) findViewById(R.id.tv_X);
        tv_y= (TextView) findViewById(R.id.tv_Y);
        tv_z= (TextView) findViewById(R.id.tv_Z);
        LSensor_layout= (LinearLayout) findViewById(R.id.LightSeneor_test);
        tv_g= (TextView) findViewById(R.id.tv_g);
        GSensor_layout.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"GSensorTest"))==1?View.VISIBLE:View.GONE);
        LSensor_layout.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"LightSensorTest"))==1?View.VISIBLE:View.GONE);

        findViewById(R.id.close_btn).setOnClickListener(this);
    }


    /**
     * 第三步：在获得焦点时注册传感器并让本类实现SensorEventListener接口
     */
    @Override
    protected void onResume() {
        super.onResume();
        /*
         *第一个参数：SensorEventListener接口的实例对象
         *第二个参数：需要注册的传感器实例
         *第三个参数：传感器获取传感器事件event值频率：
         *              SensorManager.SENSOR_DELAY_FASTEST = 0：对应0微秒的更新间隔，最快，1微秒 = 1 % 1000000秒
         *              SensorManager.SENSOR_DELAY_GAME = 1：对应20000微秒的更新间隔，游戏中常用
         *              SensorManager.SENSOR_DELAY_UI = 2：对应60000微秒的更新间隔
         *              SensorManager.SENSOR_DELAY_NORMAL = 3：对应200000微秒的更新间隔
         *              键入自定义的int值x时：对应x微秒的更新间隔
         *
         */
        if (mGSensor!=null){
            mGSensorListener=new GSensorListener();
            mSensorManager.registerListener(mGSensorListener, mGSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (mLSensor!=null){
            mLightSensorListener=new LightSensorListener();
            mSensorManager.registerListener(mLightSensorListener, mLSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    /**
     * 第五步：在失去焦点时注销传感器
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mGSensorListener!=null)
            mSensorManager.unregisterListener(mGSensorListener);
        if (mLightSensorListener!=null)
            mSensorManager.unregisterListener(mLightSensorListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close_btn:
                Intent intent=new Intent();
                if (Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"GSensorTest"))==1){
                    intent.putExtra("request","GSensor");
                }else {
                    intent.putExtra("request","LightSensor");
                }
                setResult(001,intent);
                finish();
                break;
            default:
                break;
        }
    }
}
