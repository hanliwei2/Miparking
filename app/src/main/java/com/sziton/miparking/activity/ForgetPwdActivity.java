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
import com.sziton.miparking.country.CountryPageActivity;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.sms.MobSmsUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.NetHelper;
import com.sziton.miparking.utils.SetUiUtil;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONObject;

import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

/**
 * Created by fwj on 2017/11/8.
 */

public class ForgetPwdActivity extends Activity{
    private RelativeLayout backRL;
    private TextView countryTV;
    private EditText phoneET;
    private ImageView registerIV;
    private MySharedPreferences mySharedPreferences;
    private String phone;//手机号
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            //以下为获取短信验证码后的弹框信息
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("mobSmsForgetpwd", "event=" + event+"-----------result=" + result+"-----------data=" + data);
            if (result == SMSSDK.RESULT_COMPLETE) {
                System.out.println("--------result"+event);
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    ToastUtil.shortToast(ForgetPwdActivity.this,"提交验证码成功！");

                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //验证码已经发送
                    mySharedPreferences.setStringValue(Constants.REGISTER_PHONE,
                            getResources().getString(R.string.country_plus)+mySharedPreferences.getStringValue(Constants.COUNTRY_ID)+
                            phone);//保存手机号
                    mySharedPreferences.setIntValue(Constants.REGISTER_OR_FORGETPWD,Constants.FORGETPWD_TAG);
                    ToastUtil.shortToast(ForgetPwdActivity.this,"验证码已经发送！");
                    Intent authcodeIntent=new Intent();
                    authcodeIntent.setClass(ForgetPwdActivity.this,AuthcodeActivity.class);
                    startActivity(authcodeIntent);
                } else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                    //获取国家列表成功
                }
            }else{
                //获取验证码错误
                int status = 0;
                try {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        ToastUtil.shortToast(ForgetPwdActivity.this,des);
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
        setContentView(R.layout.activity_forgetpassword);
        initView();
        initData();
    }

    private void initView(){
        MyApplication.addActivity(this);
        backRL= (RelativeLayout) findViewById(R.id.forgetpwdBackRL);
        countryTV= (TextView) findViewById(R.id.forgetpwdCountryTV);
        phoneET= (EditText) findViewById(R.id.forgetpwdPhoneET);
        registerIV= (ImageView) findViewById(R.id.forgetpwdNextIV);

        backRL.setOnClickListener(listener);
        countryTV.setOnClickListener(listener);
        registerIV.setOnClickListener(listener);
    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetUiUtil.setCountryId(this,mySharedPreferences,countryTV);
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 区号按钮
     */
    private void handleCountry(){
        Intent countryIntent=new Intent();
        countryIntent.setClass(ForgetPwdActivity.this, CountryPageActivity.class);
        startActivity(countryIntent);
    }

    /**
     * 点击下一步按钮
     */
    private void handleNext(){
        //有网络
        if(NetHelper.IsHaveInternet(this)){
            loadingDialog= DialogUtil.createLoadingDialog(this);
            String countryId=countryTV.getText().toString();
            phone=phoneET.getText().toString();
            //手机号不符合要求，return掉 ,这个手机号判断注掉，正则判断的手机号段不全，而且mob自己会验证手机号
            //手机号符合要求，获取验证码
            MobSmsUtil.registerEventHandler(mHandler);
            MobSmsUtil.getVerificationCode(countryId,phone);
            //无网络
        }else{
            ToastUtil.shortToast(this,getResources().getString(R.string.internet_error_text));
        }

    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.forgetpwdBackRL:
                    handleBack();
                    break;

                case R.id.forgetpwdCountryTV:
                    handleCountry();
                    break;

                case R.id.forgetpwdNextIV:
                    handleNext();
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobSmsUtil.unregisterEventHandler();
    }
}
