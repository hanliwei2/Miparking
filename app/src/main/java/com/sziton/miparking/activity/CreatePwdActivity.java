package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.daoimpl.ForgetPwdDaoImpl;
import com.sziton.miparking.daoimpl.RegisterDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.AccountJudgeUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fwj on 2017/11/15.
 */

public class CreatePwdActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private TextView showPwdTV;
    private EditText passwordET;
    private ImageView nextIV;
    private boolean isShow;//密码是否显示，true表示显示中，false表示隐藏中
    private MySharedPreferences mySharedPreferences;
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            switch(msg.what){
                //注册，网络请求成功，里面根据返回值判断成功还是失败
                case Constants.REGISTER_REQUEST_SUCCESS:
                    JSONObject registerJson= (JSONObject) msg.obj;
                    try {
                        String success=registerJson.getString("Success");
                        if(success.equals("True")){
                            //注册成功
                            ToastUtil.shortToast(CreatePwdActivity.this,"注册成功！");
                            Intent loginIntent=new Intent();
                            loginIntent.setClass(CreatePwdActivity.this,LoginActivity.class);
                            startActivity(loginIntent);
                            MyApplication.clearActivity();
                        }else{
                            //注册失败
                            String errorMessage=registerJson.getString("ErrorMessage");
                            ToastUtil.shortToast(CreatePwdActivity.this,success+"--->>"+errorMessage);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                //注册，网络请求失败
                case Constants.REGISTER_REQUEST_FAILURE:
                    ToastUtil.shortToast(CreatePwdActivity.this,CreatePwdActivity.this.getResources().getString(R.string.internet_error_text));
                    break;

                //忘记密码，网络请求成功，里面根据返回值判断成功还是失败
                case Constants.FORGETPWD_REQUEST_SUCCESS:
                    JSONObject forgetpwdJson= (JSONObject) msg.obj;
                    try {
                        String success=forgetpwdJson.getString("Success");
                        if(success.equals("True")){
                            //重置成功
                            ToastUtil.shortToast(CreatePwdActivity.this,"重置密码成功！");
                            Intent loginIntent=new Intent();
                            loginIntent.setClass(CreatePwdActivity.this,LoginActivity.class);
                            startActivity(loginIntent);
                            MyApplication.clearActivity();
                        }else{
                            //注册失败
                            String errorMessage=forgetpwdJson.getString("ErrorMessage");
                            ToastUtil.shortToast(CreatePwdActivity.this,success+"--->>"+errorMessage);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                //忘记密码，网络请求失败
                case Constants.FORGETPWD_REQUEST_FAILURE:
                    ToastUtil.shortToast(CreatePwdActivity.this,CreatePwdActivity.this.getResources().getString(R.string.internet_error_text));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createpwd);
        initView();
        initData();
    }

    private void initView(){
        MyApplication.addActivity(this);
        backRL= (RelativeLayout) findViewById(R.id.createpwdBackRL);
        showPwdTV= (TextView) findViewById(R.id.createpwdShowPwdTV);
        passwordET= (EditText) findViewById(R.id.createpwdET);
        nextIV= (ImageView) findViewById(R.id.createpwdNextIV);

        backRL.setOnClickListener(this);
        showPwdTV.setOnClickListener(this);
        nextIV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 显示密码
     */
    private void handleShowPwd(){
        if(!isShow){
            showPwdTV.setText(getResources().getString(R.string.createpwd_hide_text));
            passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isShow=true;
        }else{
            showPwdTV.setText(getResources().getString(R.string.createpwd_show_text));
            passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isShow=false;
        }
    }

    /**
     * 点击下一步
     */
    private void handleNext(){
        int registerOrForgetpwd=mySharedPreferences.getIntValue(Constants.REGISTER_OR_FORGETPWD);
        //注册
        if(registerOrForgetpwd==Constants.REGISTER_TAG){
            String url= Paths.appUrl;
            //以下是接口需要的参数
            String timestamp= EncryptUtil.getTimestamp();
            String signatureNonce=EncryptUtil.getSignatureNonce();
            String action= Constants.REGISTER_ACTION;
            String uid= mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
            String name="";
            if(!TextUtils.isEmpty(uid)&&uid.length()>=11){
                name=uid.substring(uid.length()-11,uid.length());
            }
            String password=passwordET.getText().toString();
            String sex=Constants.REGISTER_SEX_DEFAULT_VALUE;
            //String icon= Base64Convert.Bitmap2StrByBase64(BitmapFactory.decodeResource(getResources(),R.drawable.logo_iv));
            String icon= Constants.REGISTER_ICON_DEFAULT_VALUE;
            String birthday=Constants.REGISTER_BIRTHDAY_DEFAULT_VALUE;

            //通过签名算法得到的Signature
            Map<String,Object> paramsMap=new HashMap<>();
            paramsMap.put("Timestamp",timestamp);
            paramsMap.put("SignatureNonce",signatureNonce);
            paramsMap.put("Action",action);
            paramsMap.put("Uid",uid);
            paramsMap.put("Name",name);
            paramsMap.put("Password",password);
            paramsMap.put("Sex",sex);
            paramsMap.put("Icon",icon);
            paramsMap.put("Birthday",birthday);
            String signature=EncryptUtil.getSignature(paramsMap);

            if(!AccountJudgeUtil.judgePassword(CreatePwdActivity.this,password)){
                //不符合要求,return掉
                return;
            }else{
                //符合要求，提交服务器
                loadingDialog= DialogUtil.createLoadingDialog(this);
                RegisterDaoImpl registerDaoImpl=new RegisterDaoImpl();
                registerDaoImpl.postRegister( url,signature,timestamp,signatureNonce,action,uid,name,password,sex,icon,birthday,mHandler);
            }

        //忘记密码
        }else if(registerOrForgetpwd==Constants.FORGETPWD_TAG){
            String url= Paths.appUrl;
            //以下是接口需要的参数
            String timestamp= EncryptUtil.getTimestamp();
            String signatureNonce=EncryptUtil.getSignatureNonce();
            String action= Constants.FORGETPWD_ACTION;
            String uid= mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
            String password=passwordET.getText().toString();

            //通过签名算法得到的Signature
            Map<String,Object> paramsMap=new HashMap<>();
            paramsMap.put("Timestamp",timestamp);
            paramsMap.put("SignatureNonce",signatureNonce);
            paramsMap.put("Action",action);
            paramsMap.put("Uid",uid);
            paramsMap.put("Password",password);
            String signature=EncryptUtil.getSignature(paramsMap);

            if(!AccountJudgeUtil.judgePassword(CreatePwdActivity.this,password)){
                //不符合要求,return掉
                return;
            }else{
                //符合要求，提交服务器
                ForgetPwdDaoImpl forgetPwdDao=new ForgetPwdDaoImpl();
                forgetPwdDao.postForgetPwd(url,signature,timestamp,signatureNonce,action,uid,password,mHandler);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.createpwdBackRL:
                handleBack();
                break;

            case R.id.createpwdShowPwdTV:
                handleShowPwd();
                break;

            case R.id.createpwdNextIV:
                handleNext();
                break;
        }
    }


}
