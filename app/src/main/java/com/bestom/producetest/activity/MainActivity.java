package com.bestom.producetest.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bestom.producetest.R;
import com.bestom.producetest.base.App;
import com.bestom.producetest.helper.CommandExec;
import com.bestom.producetest.helper.Recorder;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.utils.FileUtils;
import com.bestom.producetest.utils.PermissionsUtils;
import com.bestom.producetest.utils.ProperTiesUtils;
import com.bestom.producetest.utils.Util;
import com.bestom.producetest.view.ResultDialog;
import com.bestom.producetest.view.TestView;
import com.bestom.producetest.view.VUMeter;


import java.io.File;
import java.util.List;

import static com.bestom.producetest.base.App.ConfigPath;
import static com.bestom.producetest.base.App.configfilename;

/**
 * T102/T103平板测试
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int VOLUME_LESS_WHAT = 10;
    private static final int VOLUME_ADD_WHAT = 20;
    private static final int Dialog_LESS_WHAT = 30;
    private static final int VIEW_UPDATE_WHAT = 40;
    private static final int Dialog_Dismiss_WHAT=50;

    private Context mContext;
    private Activity mActivity;

    TextView chipVersion;
    TextView systemVersion;
    TextView snNum;
    TextView firmwareVersion;

    TextView memTotal;
    TextView memFree;
    TextView extStorage;

    TestView wifiTest;
    TestView bluetoothTest;
    TestView sdCardTest;
    TestView usbHostTest;

    Button speakerPlayBtn;
    Button leftChannelBtn;
    Button rightChannelBtn;
    TextView recordText;
    VUMeter uvMeter;
    Button recordBtn;

    TextView keyVolumeLess;
    TextView keyVolumeAdd;

    Button lcdTestBtn;
    Button cameraTestBtn;
    Button sensorTestBtn;
    Button nfcTestBtn;
    Button gpioTestBtn;
    Button uninstallBtn;

    // wifi
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private boolean mReadyToTest = false;
    private final static int WIFI_MSG_SCAN = 0;

    //GPS
    AlertDialog.Builder dialog = null;
    private LocationManager locationManager;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private final static int FOUND_BLUETOOTH_WHAT = 100;

    // 外置SD卡
    private StorageManager storageManager;
    public static String mInterSD; // 内置sd卡
    public static String mExternalSD; // 外置sd卡 1
    public static String mUSB; // U盘，外置sd卡2

    // speaker recorder
    private MediaPlayer mPlayer;
    private Recorder mRecorder;
    private final static int RECORD_TIME = 3;
    private RecordHandler mRecordHandler = new RecordHandler();
    int i=0;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WIFI_MSG_SCAN:
                    if (i==0){
                        i++;
                        removeMessages(WIFI_MSG_SCAN);
                        openGPS(mActivity);
                        if (mWifiManager!=null)
                            mWifiManager.startScan();
                        i=0;
                    }
                    break;
                case FOUND_BLUETOOTH_WHAT: //发现蓝牙设备
                    removeMessages(FOUND_BLUETOOTH_WHAT);
                    bluetoothTest.setStatus(TestStatus.SUCCEED);
                    break;
                case VOLUME_LESS_WHAT: //音量减延迟恢复
                    keyVolumeLess.setBackgroundResource(R.color.colorGreen);
                    break;
                case VOLUME_ADD_WHAT: //音量加延迟恢复
                    keyVolumeAdd.setBackgroundResource(R.color.colorGreen);
                    break;
                case Dialog_LESS_WHAT: //dialog延迟消失 + 退出程序
                    ResultDialog.getInstance(mContext,MainActivity.this).dismiss();
                    if (Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"T103_usbgpio"))==1){
                        //由于T103usb口是gpio控制的，默认为低，测试中拉高gpio，测试完毕 还原默认低
                        CommandExec.CommandResult commandResult = CommandExec.execCommand("echo \"0\" >sys/micro_usb_sd_ctl/usb_power_enable",false);
                        Log.i("//cs", "echo \"0\" >sys/micro_usb_sd_ctl/usb_power_enable"+commandResult.result+"errorMsg:"+commandResult.errorMsg+"successMsg:"+commandResult.successMsg);
                    }
                    mActivity.finish();
                    System.exit(0);
                    break;
                case Dialog_Dismiss_WHAT: //dialog延迟消失
                    ResultDialog.getInstance(mContext,MainActivity.this).dismiss();

                    break;
                case VIEW_UPDATE_WHAT:
                    checkUpview();
                        break;
            }
        }
    };

    public Handler getHandler(){
        return mHandler;
    }

    private void openGPS(final Activity activity){
        // 判断GPS模块是否开启，如果没有则开启
        Log.d(TAG, "openGPS: local status is "+Settings.System.getString(getContentResolver(), Settings.System.LOCATION_PROVIDERS_ALLOWED));
        if (Settings.System.getString(getContentResolver(), Settings.System.LOCATION_PROVIDERS_ALLOWED).contains("gps")
            ||Settings.System.getString(getContentResolver(), Settings.System.LOCATION_PROVIDERS_ALLOWED).contains("network")) {

            Log.d(TAG, "---> GPS模块已开启");
//            Toast.makeText(this, "GPS模块已开启", Toast.LENGTH_SHORT).show();
        } else {
//            Log.d(TAG, "---> 判断GPS模块是否开启，如果没有则开启");
//            Toast.makeText(this, "判断GPS模块是否开启，如果没有则开启", Toast.LENGTH_SHORT).show();
            if (dialog==null){
                dialog = new AlertDialog.Builder(activity);
            }
//            dialog.setTitle("要使用定位功能，请打开GPS连接");
            dialog.setMessage("wifi 测试，要使用定位功能，请打开GPS连接");

            dialog.setPositiveButton("设置", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    Log.d(TAG, "---> 设置");
                    // 转到手机设置界面，用户设置GPS
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                    dialogInterface.dismiss();

                }
            });

            dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    Log.d(TAG, "---> 取消");
                    Toast.makeText(mContext,"wifi 模块测试 需要gps",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();

                    Message message=new Message();
                    message.what=WIFI_MSG_SCAN;
                    mHandler.sendMessageDelayed(message,3000);
                }
            });

            AlertDialog alertDialog = dialog.create();

            //点击外面不消失
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            if (!alertDialog.isShowing()){
                alertDialog.show();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: ");
        mContext=this;
        mActivity=this;

        //申请动态权限
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        PermissionsUtils.getInstance().chekPermissions(this, permissions, permissionsResult);
        //连接服务器
        App.wsClient.connect();
        initView();
        initData();

        //检查配置文件
        checkConfigFile();

        if (Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"T103_usbgpio"))==1) {
            //由于usb口是gpio控制的，先拉高gpio，再循环检测外挂设备
            CommandExec.CommandResult commandResult = CommandExec.execCommand("echo \"1\" >sys/micro_usb_sd_ctl/usb_power_enable", false);
            Log.i("//cs", "echo \"1\" >sys/micro_usb_sd_ctl/usb_power_enable" + commandResult.result + "errorMsg:" + commandResult.errorMsg + "successMsg:" + commandResult.successMsg);
        }

        new Thread(new CheckStorageRunnable()).start();
        initSpeaker();
        initRecord();
    }

    private void checkConfigFile(){
        File configFile = new File(ConfigPath);
        if(!configFile.exists()){
            //初始化测试视频文件路径
            initConfig();
        }else {
            initViewdefault();
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
            initViewdefault();
        }else{
            Log.e(TAG, "initConfig: 初始化copyFromAsset()失败!! 完成默认初始化赋值" );
        }
    }


    /**
     * 初始化控件
     */
    private void initView() {
        chipVersion = (TextView) findViewById(R.id.chip_version);
        systemVersion = (TextView) findViewById(R.id.system_version);
        snNum = (TextView) findViewById(R.id.sn_num);
        firmwareVersion = (TextView) findViewById(R.id.firmware_version);

        memTotal = (TextView) findViewById(R.id.mem_total);
        memFree = (TextView) findViewById(R.id.mem_free);
        extStorage = (TextView) findViewById(R.id.ext_storage);

        wifiTest = (TestView) findViewById(R.id.wifi_test);
        bluetoothTest = (TestView) findViewById(R.id.bluetooth_test);
        sdCardTest = (TestView) findViewById(R.id.sd_card_test);
        usbHostTest = (TestView) findViewById(R.id.usb_host_test);

        speakerPlayBtn = (Button) findViewById(R.id.speaker_play_btn);
        leftChannelBtn = (Button) findViewById(R.id.left_channel_btn);
        rightChannelBtn = (Button) findViewById(R.id.right_channel_btn);
        recordText = (TextView) findViewById(R.id.record_text);
        uvMeter = (VUMeter) findViewById(R.id.uvMeter);
        recordBtn = (Button) findViewById(R.id.record_btn);

        keyVolumeLess = (TextView) findViewById(R.id.key_volume_less);
        keyVolumeAdd = (TextView) findViewById(R.id.key_volume_add);

        lcdTestBtn = (Button) findViewById(R.id.lcd_test_btn);
        cameraTestBtn = (Button) findViewById(R.id.camera_test_btn);
        sensorTestBtn = (Button) findViewById(R.id.sensor_test_btn);
        nfcTestBtn = (Button) findViewById(R.id.nfc_test_btn);
        gpioTestBtn= (Button) findViewById(R.id.gpio_test_btn);
        uninstallBtn = (Button) findViewById(R.id.uninstall_btn);

        speakerPlayBtn.setOnClickListener(this);
        leftChannelBtn.setOnClickListener(this);
        rightChannelBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);

        lcdTestBtn.setOnClickListener(this);
        cameraTestBtn.setOnClickListener(this);
        sensorTestBtn.setOnClickListener(this);
        nfcTestBtn.setOnClickListener(this);
        gpioTestBtn.setOnClickListener(this);
        uninstallBtn.setOnClickListener(this);
    }

    /**
     * 配置默认控件
     */
    private void initViewdefault(){
        lcdTestBtn.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"LcdTest"))==1?View.VISIBLE:View.GONE);
        cameraTestBtn.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"CameraTest"))==1?View.VISIBLE:View.GONE);
        sensorTestBtn.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"SensorTest"))==1?View.VISIBLE:View.GONE);
        nfcTestBtn.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"NfcTest"))==1?View.VISIBLE:View.GONE);
        gpioTestBtn.setVisibility(Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"GpioTest"))==1?View.VISIBLE:View.GONE);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Log.d(TAG, "initData: build----"+Build.BOARD);
        String chip = getString(R.string.chip_version) + Build.DEVICE;
        App.mProductBean.setCpu(Build.DEVICE);
        String build = getString(R.string.system_version) + "Model:" + Build.MODEL
                + "，Android:" + Build.VERSION.RELEASE;

        String sn = getString(R.string.sn_num) + Util.getSn();
        App.mProductBean.setSn(Util.getSn());
        String firmware = getString(R.string.firmware_version) + Build.DISPLAY;
        App.mProductBean.setFirmVersion(Build.DISPLAY);

        String total = getString(R.string.mem_total) + Util.getTotalMemory(this);
        App.mProductBean.setDdr(Util.getTotalMemory(this));
        String free = getString(R.string.mem_free) + Util.getAvailMemory(this);
        String storage = getString(R.string.ext_storage) + Util.getStorageSize(this);
        App.mProductBean.setEmmc(Util.getStorageSize(this));
        chipVersion.setText(chip);
        systemVersion.setText(build);
        snNum.setText(sn);
        firmwareVersion.setText(firmware);

        memTotal.setText(total);
        memFree.setText(free);
        extStorage.setText(storage);

        wifiTest.setStatus(TestStatus.FAILED);
        bluetoothTest.setStatus(TestStatus.FAILED);
        sdCardTest.setStatus(TestStatus.FAILED);
        usbHostTest.setStatus(TestStatus.FAILED);

        storageManager = (StorageManager) getApplicationContext().
                getSystemService(Context.STORAGE_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onClick(View view) {
        App.mProductBean.setTp(1); //触屏可用
        switch (view.getId()) {
            case R.id.speaker_play_btn: // 播放
                if (mPlayer.isPlaying()) {
                    speakerPlayBtn.setText(getString(R.string.speaker_pause));
                    mPlayer.pause();
                } else {
                    speakerPlayBtn.setText(getString(R.string.speaker_play));
                    mPlayer.start();
                }
                break;
            case R.id.left_channel_btn: // 左声道
                mPlayer.setVolume(1, 0);
                ResultDialog.getInstance(mContext,this).setRequest("leftsound").show();
                break;
            case R.id.right_channel_btn: // 右声道
                mPlayer.setVolume(0, 1);
                ResultDialog.getInstance(mContext,this).setRequest("rightsound").show();
                break;
            case R.id.record_btn: // 录音测试
                if (mPlayer.isPlaying()) mPlayer.pause();
                mRecordHandler.sendEmptyMessage(MSG_TEST_MIC_START);
                recordBtn.setText(getString(R.string.record_replay));
                recordBtn.setEnabled(false);
//                ResultDialog.getInstance(mContext,this).setRequest("micro").show();
                break;
            case R.id.lcd_test_btn: // LCD测试
                Intent intent = new Intent(this, LcdTestActivity.class);
//                startActivity(intent);
                startActivityForResult(intent,000);
                break;
            case R.id.camera_test_btn: // 摄像头测试+
                Intent intent1 = new Intent(this, CameraActivity.class);
//                startActivity(intent1);
                startActivityForResult(intent1,000);
                break;
            case R.id.sensor_test_btn:  //sensor
//                Intent intent2 = new Intent(this, VideoActivity.class);
                Intent intent2 = new Intent(this, SensorActivity.class);
//                startActivity(intent2);
                startActivityForResult(intent2,000);
//                finish();
                break;
            case R.id.nfc_test_btn: //NFC
                //finish();
//                Intent intent3 = new Intent(this, GpioActivity.class);
                Intent intent3 = new Intent(this, NFCActivity.class);
//                startActivity(intent3);
                startActivityForResult(intent3,000);
                break;
            case R.id.gpio_test_btn:    //gpio&network
                Intent gpiointent = new Intent(this, GpioActivity.class);
                startActivityForResult(gpiointent,000);
                break;
            case R.id.uninstall_btn: // 关闭
                //uninstallPackage("com.bestom.producetest");
                if (App.mProductBean.getPowerkey()==0){
                    ResultDialog.getInstance(mContext,this).setRequest("powerkey").show();
                }else {
                    if (Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"T103_usbgpio"))==1) {
                        //由于usb口是gpio控制的，默认为低，测试中拉高gpio，测试完毕 还原默认低
                        CommandExec.CommandResult commandResult = CommandExec.execCommand("echo \"0\" >sys/micro_usb_sd_ctl/usb_power_enable", false);
                        Log.i("//cs", "echo \"0\" >sys/micro_usb_sd_ctl/usb_power_enable" + commandResult.result + "errorMsg:" + commandResult.errorMsg + "successMsg:" + commandResult.successMsg);
                    }
                    finish();
                    System.exit(0);
//                    ResultDialog.getInstance(mContext,this).setRequest("").show();
                }

                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d(TAG, "--------- onKeyDown ------------");
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN: //音量减
                keyVolumeLess.setBackgroundResource(R.color.colorPrimary);
                mHandler.sendEmptyMessageDelayed(VOLUME_LESS_WHAT, 500);
                App.mProductBean.setVdkey(1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP: //音量加
                keyVolumeAdd.setBackgroundResource(R.color.colorPrimary);
                mHandler.sendEmptyMessageDelayed(VOLUME_ADD_WHAT, 500);
                App.mProductBean.setVpkey(1);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppUtil.hasNavBar(this)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(this);
        }

        checkUpview();
        if (mWifiManager != null) {
            //发送扫描wifi
            Message message=new Message();
            message.what=WIFI_MSG_SCAN;
            mHandler.sendMessageDelayed(message,3000);

            mWifiManager.setWifiEnabled(true);
        }
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.cancelDiscovery();
                }
            }, 60 * 1000);
            bluetoothAdapter.startDiscovery();
        } else if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }

        Log.i(TAG, "onResume: ");
    }

    private void checkUpview(){
        if (App.mProductBean.getLeftsound()==1){
            leftChannelBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getLeftsound()==2){
            leftChannelBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getRightsound()==1){
            rightChannelBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getRightsound()==2){
            rightChannelBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getMicro()==1){
            recordBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getMicro()==2){
            recordBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getRgb()==1){
            lcdTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getRgb()==2){
            lcdTestBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getFrontcamera()==1){
            cameraTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getFrontcamera()==2){
            cameraTestBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getBackcamera()==1){
            cameraTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getBackcamera()==2){
            cameraTestBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getGsensor()==1&&App.mProductBean.getLightsensor()==1){
            sensorTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getGsensor()==2||App.mProductBean.getLightsensor()==2){
            sensorTestBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getNfc()==1&&App.mProductBean.getNfclight()==1){
            nfcTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getNfc()==2||App.mProductBean.getNfclight()==2){
            nfcTestBtn.setBackgroundResource(R.color.colorRed);
        }
        if (App.mProductBean.getGpio()==1&&App.mProductBean.getNetwork()==1){
            gpioTestBtn.setBackgroundResource(R.color.colorGreen);
        }else if (App.mProductBean.getGpio()==2||App.mProductBean.getNetwork()==2){
            gpioTestBtn.setBackgroundResource(R.color.colorRed);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        switch (mRecorder.state()) {
            case Recorder.IDLE_STATE:
                mRecorder.delete();
                break;
            case Recorder.PLAYING_STATE:
                mRecorder.stop();
                mRecorder.delete();
                break;
            case Recorder.RECORDING_STATE:
                mRecorder.stop();
                mRecorder.clear();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(bluetoothReceiver);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mRecordHandler != null) {
            mRecordHandler.removeCallbacksAndMessages(null);
            mRecordHandler = null;
        }

        if (mPlayer != null) {
            mPlayer.stop();
            this.mPlayer.release();
            this.mPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    // 创建监听权限的接口对象
    PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
        @Override
        public void passPermissons() {

            //发送扫描wifi
            Message message=new Message();
            message.what=WIFI_MSG_SCAN;
            mHandler.sendMessageDelayed(message,3000);

            //GPS wifi 模块需要用到gps
            locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            wifiTesting();
            bluetoothTesting();

        }

        @Override
        public void forbitPermissons() {

        }
    };

    /**********************************************************
     * about wifi ethernet
     */
    private void wifiTesting() {
        // wifi
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WIFIBroadcastReceiver();

        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        localIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        localIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        localIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, localIntentFilter);

        mWifiManager.setWifiEnabled(true);
    }

    class WIFIBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action:" + action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    mHandler.sendEmptyMessage(WIFI_MSG_SCAN);
                }
            }
            if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
                boolean connected = intent.getBooleanExtra(
                        WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                if (connected && mReadyToTest) {
                    Log.d(TAG, "already connect to:" + mWifiManager.getConnectionInfo().getSSID());
                    //mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 1000);
                    wifiTest.setStatus(TestStatus.SUCCEED);
                    //mWifiManager.setWifiEnabled(false);
                }
            }
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                List<ScanResult> resultList = mWifiManager.getScanResults();

                Log.i(TAG, "onReceive: resultList.size"+resultList.size());

                if (!resultList.isEmpty()) {
                    mReadyToTest = true;
                    Log.d(TAG, "---------- ScanResult SIZE ----------- " + resultList.size());
                    wifiTest.setStatus(TestStatus.SUCCEED);
                    App.mProductBean.setWifi(1); //wifi可用
                    //mWifiManager.setWifiEnabled(false);
                }else {

                    App.mProductBean.setWifi(2); //wifi不可用，异常 ，无列表
                }
            }
        }
    }

    /**********************************************************
     * about bluetooth
     */
    private void bluetoothTesting() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //不支持蓝牙
        if (bluetoothAdapter == null) {
            bluetoothTest.setStatus(TestStatus.FAILED);
            return;
        }
        //判断打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //蓝牙状态
        localIntentFilter.addAction(BluetoothDevice.ACTION_FOUND); //注册广播接收信号
        registerReceiver(bluetoothReceiver, localIntentFilter);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) { //蓝牙状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (state) {
                    case BluetoothAdapter.STATE_ON: //蓝牙打开
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bluetoothAdapter.cancelDiscovery();
                            }
                        }, 60 * 1000);
                        bluetoothAdapter.startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_OFF: //蓝牙关闭
                        bluetoothTest.setStatus(TestStatus.FAILED);
                        break;
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { //发现蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mHandler.sendEmptyMessage(FOUND_BLUETOOTH_WHAT);
                Log.d(TAG, "蓝牙设备:" + device.getName() + " 设备地址:" + device.getAddress());
                App.mProductBean.setBt(1); //bt可用
            }
        }
    };


    /**********************************************************
     * check TF卡 USB
     */
    private void getStorageList() {
        String[] paths = AppUtil.getVolumePaths(storageManager);
        if (paths != null) {
            if (paths.length > 0) {
                mInterSD = paths[0];
            }
            if (paths.length > 1) {
                mExternalSD = paths[1];
            }
            if (paths.length > 2) {
                mUSB = paths[2];
            }
        }
    }

    private boolean getTFState() {
        if (TextUtils.isEmpty(mExternalSD)) {
            return false;
        }
        try {
            return "mounted".equals(Environment.getStorageState(new File(mExternalSD)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean getUSBState() {
        if (TextUtils.isEmpty(mUSB)) {
            return false;
        }
        try {
            return "mounted".equals(Environment.getStorageState(new File(mUSB)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    class CheckStorageRunnable implements Runnable {
        @Override
        public void run() {
//            for (int i = 0; i < 50; i++)

            while (true){
                getStorageList();
                if (getTFState()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sdCardTest.setStatus(TestStatus.SUCCEED);
                            App.mProductBean.setSd(1);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sdCardTest.setStatus(TestStatus.FAILED);
                            App.mProductBean.setSd(2);
                        }
                    });
                }
                if (getUSBState()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usbHostTest.setStatus(TestStatus.SUCCEED);
                            App.mProductBean.setUsb(1);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usbHostTest.setStatus(TestStatus.FAILED);
                            App.mProductBean.setUsb(2);
                        }
                    });
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**********************************************************
     * about speaker
     */
    private void initSpeaker() {
        mPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor fd = getAssets().openFd("test_music.mp3");
            mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                    fd.getDeclaredLength());
            mPlayer.prepare();
            mPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**********************************************************
     * about mic
     */
    private void initRecord() {
        // record
        mRecorder = new Recorder();
        uvMeter.setRecorder(mRecorder);
    }

    int mTimes;
    private static final int MSG_TEST_MIC_ING = 0;
    private static final int MSG_TEST_MIC_OVER = 1;
    private static final int MSG_TEST_MIC_START = 2;

    class RecordHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                case MSG_TEST_MIC_START:
                    removeMessages(MSG_TEST_MIC_START);
                    mTimes = RECORD_TIME;
                    recordText.setText(String.valueOf(mTimes));
                    mRecorder.startRecording(3, ".amr");
                    sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
                    break;
                case MSG_TEST_MIC_ING:
                    if (mTimes > 0) {
                        recordText.setText(String.valueOf(mTimes));
                        mTimes--;
                        Log.i(TAG, "mTimes=" + mTimes);
                        sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
                    } else {
                        removeMessages(MSG_TEST_MIC_ING);
                        sendEmptyMessage(MSG_TEST_MIC_OVER);
                    }

                    ResultDialog.getInstance(mContext,MainActivity.this).setRequest("micro").show();
                    break;
                case MSG_TEST_MIC_OVER:
                    removeMessages(MSG_TEST_MIC_OVER);
                    mRecorder.stopRecording();
                    if (mRecorder.sampleLength() > 0) {
                        recordText.setText(R.string.record_success);
                        mRecorder.startPlayback();
                    } else {
                        recordText.setText(R.string.record_error);
                    }
                    recordBtn.setEnabled(true);
                    break;
            }
            uvMeter.invalidate();
        }
    }

    /**
     * 自卸载程序
     * <permission android:name="android.permission.DELETE_PACKAGES" />
     *
     * @param packageName 包名
     */
    private void uninstallPackage(String packageName) {
        Uri uninstallUri = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, uninstallUri);
        startActivity(uninstallIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==001){
            if(AppUtil.hasNavBar(mContext)) {
                AppUtil.hideBottomUIMenu(this);
            }

            String request = data.getStringExtra("request");
            if (request.equals("camera")){
                ResultDialog.getInstance(mContext,MainActivity.this).setRequest("frontcamera").show();
            }else if (request.equals("2camera")){
                Intent intent=new Intent(this,Camera2Activity.class);
                startActivityForResult(intent,001);
            }
            else {
                ResultDialog.getInstance(mContext,MainActivity.this).setRequest(request).show();
            }
        }
    }
}
