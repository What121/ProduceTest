package com.bestom.producetest.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bestom.producetest.R;
import com.bestom.producetest.activity.MainActivity;
import com.bestom.producetest.base.App;
import com.bestom.producetest.base.RequestBean;
import com.bestom.producetest.base.ResponseBean;
import com.bestom.producetest.service.clientmsg;
import com.bestom.producetest.utils.AppUtil;
import com.bestom.producetest.utils.ProperTiesUtils;
import com.bestom.producetest.utils.TimeUtil;
import com.bestom.producetest.utils.UUIDUtil;
import com.google.gson.Gson;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import static com.bestom.producetest.base.App.configfilename;


public class ResultDialog extends AlertDialog  {

    private static ResultDialog resultDialog;
    private  Context mContext;
    private MainActivity mActivity;
//    private AVLoadingIndicatorView avi;

    private String request="";

    Button pass_bt,no_bt;
    TextView tv_title,tv_result;

    private static final String TAG = "ResultDialog";


    public static ResultDialog getInstance(Context context, MainActivity activity) {
        if (null == resultDialog) {
            resultDialog = new ResultDialog(context, R.style.ResultDialog,activity); //设置AlertDialog背景透明
            resultDialog.setCancelable(false);
            resultDialog.setCanceledOnTouchOutside(false);
        }
        return resultDialog;
    }

    public ResultDialog setRequest(String request){
        this.request=request;
        return resultDialog;
    }

    public ResultDialog setAgin(String request){
        this.request=request;
        return resultDialog;
    }

    private ResultDialog(Context context, int themeResId,MainActivity activity) {
        super(context,themeResId);
        mContext=context;
        mActivity=activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    protected void onStart() {
        super.onStart();

        this.setContentView(R.layout.dialog_result);

//        avi =  this.findViewById(R.id.avi);
        pass_bt = (Button) this.findViewById(R.id.PASS);
        pass_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //根据request写入不同的参数中
                saveData(true);
                dismiss();
            }
        });
        no_bt= (Button) resultDialog.findViewById(R.id.NO);
        no_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //根据request写入不同的参数中
                saveData(false);
                dismiss();
            }
        });
        tv_title= (TextView) resultDialog.findViewById(R.id.tv_title);
        tv_result= (TextView) resultDialog.findViewById(R.id.result_tv);
    }

    private void saveData(boolean flag){
        if (request.equals("screen")){
            App.mProductBean.setRgb(flag ?1:2);
        }else if (request.equals("frontcamera")){
            App.mProductBean.setFrontcamera(flag ?1:2);
        }else if (request.equals("backcamera")){
            App.mProductBean.setBackcamera(flag ?1:2);
        } else if (request.equals("GSensor")){
            App.mProductBean.setGsensor(flag ?1:2);
        }else if (request.equals("LightSensor")){
            App.mProductBean.setLightsensor(flag ?1:2);
        } else if (request.equals("nfc")){
            App.mProductBean.setNfc(flag ?1:2);
        }else if (request.equals("nfclight")){
            App.mProductBean.setNfclight(flag ?1:2);
        }else if (request.equals("leftsound")){
            App.mProductBean.setLeftsound(flag ?1:2);
        }else if (request.equals("rightsound")){
            App.mProductBean.setRightsound(flag ?1:2);
        }else if (request.equals("micro")){
            App.mProductBean.setMicro(flag ?1:2);
        }else if (request.equals("powerkey")){
            App.mProductBean.setPowerkey(flag ?1:2);
        }else if (request.equals("GpioTest")){
            App.mProductBean.setGpio(flag ?1:2);
        }else if (request.equals("NetTest")){
            App.mProductBean.setNetwork(flag?1:2);
        }
        else{
            Toast.makeText(mContext,"productbean don't have "+request,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void show() {

        if (AppUtil.hasNavBar(mContext)) {//隐藏底部导航栏
            AppUtil.hideBottomUIMenu(mActivity);
        }

        if (request.equals("")){
            super.show();
//            Toast.makeText(mContext,"please setrequest to save resultdata",Toast.LENGTH_SHORT).show();
//            this.dismiss();

            tv_title.setText("测试结果上传中...");
            pass_bt.setVisibility(View.GONE);
            no_bt.setVisibility(View.GONE);

            //添加本次测试的时间
            App.mProductBean.setTime(TimeUtil.getCurDate());
            Gson gson=new Gson();
            String msg=gson.toJson(App.mProductBean);
            final RequestBean requestBean=new RequestBean(UUIDUtil.getUUID(),msg);

//            Log.i(TAG, "show: requestbody"+new Gson().toJson(requestBean));

            //region net 上传数据
            /*
            HttpUtil.doPostBody("https://www.baidu.com/", "", new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
//                    Toast.makeText(mContext,"update fail "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"update onFailure "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
//                    tv_result.setText("update fail "+e.getMessage());

                    mActivity.getHandler().sendEmptyMessageDelayed(30,1000);
                    Log.i(TAG, "update onFailure "+e.getMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
//                    Toast.makeText(mContext,"update success "+response.toString(),Toast.LENGTH_SHORT).show();
//                    tv_result.setText("update success "+response.toString());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"update success "+response.toString(),Toast.LENGTH_LONG).show();
                        }
                    });
                    mActivity.getHandler().sendEmptyMessageDelayed(30,1000);
                    Log.i(TAG, "update success "+response.toString());
                }
            });
            */
            //endregion

            //region websocket client sendmsg
            if (App.wsClient.getReadyState()!= ReadyState.OPEN){
                tv_title.setText("与服务器连接不通，尝试重连...");

                App.wsClient.close();
                App.wsClient.notifaction(new clientmsg() {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_title.setText("与服务器连接成功，正在上传数据...");

                                dorequest(requestBean);
//                        Toast.makeText(mContext,"update success "+msg,Toast.LENGTH_LONG).show();
//                                mActivity.getHandler().sendEmptyMessageDelayed(50,3000);
                            }
                        });
                    }
                    //region onMsg
                    @Override
                    public void onMsg(String s) {

                    }
                    //endregion

                    @Override
                    public void onClose(int i, final String s, boolean b) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_title.setText("与服务器连接失败，请检查网络..."+s);
//                        Toast.makeText(mContext,"update success "+msg,Toast.LENGTH_LONG).show();
                                mActivity.getHandler().sendEmptyMessageDelayed(30,3000);
                            }
                        });
                    }
                    //region onError
                    @Override
                    public void onError(Exception e) {

                    }
                    //endregion
                }).reconnect();
            }else {
                dorequest(requestBean);
            }

            //endregion

        }
        else {
            super.show();
//        avi.show();
            tv_title.setVisibility(View.VISIBLE);
            if (request.equals("screen")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_screen));
            }
            else if (request.equals("leftsound")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_leftsound));
            }
            else if (request.equals("rightsound")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_rightsound));
            }
            else if (request.equals("micro")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_micro));
            }
            else if (request.equals("frontcamera")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_frontcamera));
            }
            else if (request.equals("backcamera")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_backcamera));
            }
            else if (request.equals("LightSensor")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_lightsensor));
            }
            else if (request.equals("GSensor")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_gsensor));
            }
            else if (request.equals("nfc")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_nfc));
            }
            else if (request.equals("nfclight")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_nfclight));
            }
            else if (request.equals("powerkey")){
                tv_title.setText(mContext.getResources().getString(R.string.result_dialog_powerkey));
            }
            else {
                tv_title.setText(request);
            }
            tv_title.append(" "+mContext.getResources().getString(R.string.result_dialog));

            pass_bt.setVisibility(View.VISIBLE);
            no_bt.setVisibility(View.VISIBLE);
        }

    }

    private void dorequest(final RequestBean requestBean){
        final Gson gson=new Gson();
        String requestbody = gson.toJson(requestBean );
        Log.i(TAG, "show: bodyjson:"+requestbody);

        App.wsClient.notifaction(new clientmsg() {
            //region onOpen
            @Override
            public void onOpen(ServerHandshake serverHandshake) {

            }
            //endregion

            @Override
            public void onMsg(final String s) {
                try {
                    Gson rgson=new Gson();
                    ResponseBean responseBean = rgson.fromJson(s,ResponseBean.class);
                    final String msg = responseBean.getMsg();
                    if (responseBean.getId().equals(requestBean.getId())){
                        //进行id校对，防止消息队列 混乱
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_title.setText("response:"+msg);
                                Toast.makeText(mContext,"response: "+msg,Toast.LENGTH_LONG).show();
                            }
                        });

                        Log.i(TAG, "response: "+msg);
                        mActivity.getHandler().sendEmptyMessageDelayed(30,2000);

                    }else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_title.setText("response error:"+msg);
                                Toast.makeText(mContext,"response msg error "+msg,Toast.LENGTH_LONG).show();
                            }
                        });
//
                        Log.i(TAG, "response error "+msg);
                        mActivity.getHandler().sendEmptyMessageDelayed(50,2000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    mActivity.runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            tv_title.setText("response 格式异常:"+s);
                            Toast.makeText(mContext,"response msg 格式异常 ",Toast.LENGTH_LONG).show();
                        }
                    });
//
                    Log.i(TAG, "response 格式异常: ");
                    mActivity.getHandler().sendEmptyMessageDelayed(50,2000);
                }

            }

            @Override
            public void onClose(int i, final String s, boolean b) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_title.setText("update onclose:"+s);
                        Toast.makeText(mContext,"update onclose:"+s,Toast.LENGTH_LONG).show();
                    }
                });
//
                Log.i(TAG, "update onclose:"+s);
                mActivity.getHandler().sendEmptyMessageDelayed(50,2000);
            }

            @Override
            public void onError(final Exception e) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_title.setText("upload date error:"+e.getMessage());
                        Toast.makeText(mContext,"upload date error:"+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
//
                Log.i(TAG, "upload date error:"+e.getMessage());
                mActivity.getHandler().sendEmptyMessageDelayed(50,2000);

            }
        }).send(requestbody);
    }

    @Override
    public void dismiss() {

        super.dismiss();
        mActivity.getHandler().sendEmptyMessageDelayed(40,1000);
//        avi.hide();

        if (request.equals("GSensor")&&Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"LightSensorTest"))==1){
            setRequest("LightSensor");
            show();
        }
        if (request.equals("frontcamera")){
            if (Camera.getNumberOfCameras()>=2){
                setRequest("backcamera");
                show();
            }
        }
        if (request.equals("nfc")&&Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"NfcLightTest"))==1){
            setRequest("nfclight");
            show();
        }
        if (request.equals("GpioTest")&&Integer.valueOf(ProperTiesUtils.getProperties(mActivity,configfilename,"NetTest"))==1){
            setRequest("NetTest");
            show();
        }
        if (request.equals("sound")){
            setRequest("soundleft");
            show();
        }
        if (request.equals("powerkey")){
            setRequest("");
            show();

        }

    }

}
