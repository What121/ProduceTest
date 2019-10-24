package com.bestom.producetest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.bestom.producetest.R;
import com.bestom.producetest.utils.ProperTiesUtils;
import com.gpiotest.android_gpio_api.GPIO;

import static com.bestom.producetest.base.App.configfilename;

public class GpioActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GpioActivity";

    private Activity mActivity;
    private Context mContext;

    Button start_light,stop_light,net_test,close_bt;
    WebView mWebView;

    private boolean flag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gpio);
        mActivity=this;
        mContext=this;

        initview();

    }

    private void initview(){
        start_light= (Button) findViewById(R.id.start_light);
        start_light.setOnClickListener(this);
        stop_light= (Button) findViewById(R.id.start_dark);
        stop_light.setOnClickListener(this);
        net_test= (Button) findViewById(R.id.net_test);
        net_test.setOnClickListener(this);
        close_bt= (Button) findViewById(R.id.close_btn);
        close_bt.setOnClickListener(this);
        mWebView= (WebView) findViewById(R.id.net_webview);
        start_light.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"gpioTest"))==1?View.VISIBLE:View.GONE);
        stop_light.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"gpioTest"))==1?View.VISIBLE:View.GONE);
        net_test.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"NetTest"))==1?View.VISIBLE:View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private Thread startlight=new Thread(new Runnable() {
        @Override
        public void run() {
            while (flag){
                GPIO.writeGpioValue(1+(int)(Math.random()*44));
            }
        }
    });

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_light:
                for (int i=2;i<=52;i=i+2){
                    Log.i("light",i+"");
                    GPIO.writeGpioValue(i);
                    if (i==44){
                        Toast.makeText(this,"all light",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.start_dark:
                for (int i=1;i<=51;i=i+2){
                    Log.i("dark",i+"");
                    GPIO.writeGpioValue(i);
                    if (i==43){
                        Toast.makeText(this,"all dark",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.net_test:
                mWebView.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse("https://www.baidu.com");
                mWebView.loadUrl("https://www.baidu.com");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
                break;
            case R.id.close_btn:
                Intent resultintent=new Intent();
                if (Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"gpioTest"))==1){
                    resultintent.putExtra("request","GpioTest");
                }else {
                    resultintent.putExtra("request","NetWorkTest");
                }
                setResult(001,resultintent);
                finish();
                break;
                default:
                    break;
        }
    }
}
