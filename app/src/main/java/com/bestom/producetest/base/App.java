package com.bestom.producetest.base;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.bestom.producetest.service.WSClient;
import com.bestom.producetest.utils.FileUtils;
import com.bestom.producetest.utils.ProperTiesUtils;

import java.io.File;

public class App extends Application {
    private static final String TAG = "App";
    public static productBean mProductBean;
    public static WSClient wsClient;

    // config file
    public static String configfilename="testMain_config";
    public static String ConfigPath;

    @Override
    public void onCreate() {
        //初始化测试项目配置
        Config();

        //初始化测试结果数据类
        mProductBean=new productBean();

        //初始化client，上传数据到服务器
        wsClient =  WSClient.getInstance(ProperTiesUtils.getProperties(this,configfilename,"localhost"));

        super.onCreate();

    }

    @TargetApi(Build.VERSION_CODES.N)
    private void Config(){
        ConfigPath=this.getFilesDir().getAbsolutePath()+ File.separator+configfilename;
        File configFile = new File(ConfigPath);
        if(!configFile.exists()){
            //初始化测试视频文件路径
            initConfig();
        }else {
            Log.d(TAG, "Config: file exit !!!");
        }
    }

    /**
     * 配置文件
     */
    private void initConfig(){
        boolean copyResult = FileUtils.copyFromAssetToData(this, configfilename, true);
        if(copyResult){
            // chmod 777 configfile
            FileUtils.chmodDataFilePath(this, ConfigPath);
//            initViewdefault();
        }else{
            Log.e(TAG, "initConfig: 初始化copyFromAsset()失败!! 完成默认初始化赋值" );
        }
    }

}
