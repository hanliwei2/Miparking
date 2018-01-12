package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.sms.MobSmsUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.NetHelper;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONObject;

import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

/**
 * Created by fwj on 2017/11/15.
 */

public class AuthcodeActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private TextView contentTV;
    private EditText authcodeET;
    private ImageView nextIV;
    private MySharedPreferences mySharedPreferences;
    private String phone;//这个带+86的
    private String subPhone;//这个去掉+86的
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            //以下为提交短信验证码后的弹框信息
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("mobSmsSubmitCode", "event=" + event+"-----------result=" + result+"-----------data=" + data);
            if (result == SMSSDK.RESULT_COMPLETE) {
                System.out.println("--------result"+event);
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    ToastUtil.shortToast(AuthcodeActivity.this,"提交验证码成功！");
                    Intent authcodeIntent=new Intent();
                    authcodeIntent.setClass(AuthcodeActivity.this,CreatePwdActivity.class);
                    startActivity(authcodeIntent);
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //验证码已经发送
                    ToastUtil.shortToast(AuthcodeActivity.this,"验证码已经发送！");
                } else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                    //获取国家列表成功
                }
            }else{
                //提交验证码错误
                int status = 0;
                try {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        ToastUtil.shortToast(AuthcodeActivity.this,des);
                        return;
                    }
                } catch (Exception e) {
                    SMSLog.getInstance().w(e);
                }
            }
            MobSmsUtil.unregisterEventHandler();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authcode);
        initView();
        initData();
    }

    private void initView(){
        MyApplication.addActivity(this);
        backRL= (RelativeLayout) findViewById(R.id.authcodeBackRL);
        contentTV= (TextView) findViewById(R.id.authcodeContentTV);
        authcodeET= (EditText) findViewById(R.id.authcodeET);
        nextIV= (ImageView) findViewById(R.id.authcodeNextIV);

        backRL.setOnClickListener(this);
        nextIV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
        phone=mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);//这个带+86的
        if(!TextUtils.isEmpty(phone)&&phone.length()>=11){
            subPhone=phone.substring(phone.length()-11,phone.length());
            contentTV.setText(subPhone);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 点击下一步按钮
     */
    private void handleNext(){
        //有网络
        if(NetHelper.IsHaveInternet(this)){
            loadingDialog= DialogUtil.createLoadingDialog(this);
            String countryId=mySharedPreferences.getStringValue(Constants.COUNTRY_ID);
            String verificationCode=authcodeET.getText().toString();
            //提交验证码
            MobSmsUtil.registerEventHandler(mHandler);
            MobSmsUtil.submitVerificationCode(countryId,subPhone,verificationCode);
            //无网络
        }else{
            ToastUtil.shortToast(this,getResources().getString(R.string.internet_error_text));
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.authcodeBackRL:
                handleBack();
                break;

            case R.id.authcodeNextIV:
                handleNext();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobSmsUtil.unregisterEventHandler();
    }
}
