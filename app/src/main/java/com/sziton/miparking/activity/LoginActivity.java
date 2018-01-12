package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.country.CountryPageActivity;
import com.sziton.miparking.daoimpl.LoginDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.AccountJudgeUtil;
import com.sziton.miparking.utils.AsyncHttpCilentUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.SetUiUtil;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fwj on 2017/11/8.
 */

public class LoginActivity extends Activity implements View.OnClickListener{
    private TextView forgetPwdTV;
    private TextView countryTV;
    private EditText phoneET;
    private TextView showPwdTV;
    private EditText passwordET;
    private TextView registerTV;
    private ImageView loginIV;
    private boolean isShow;//密码是否显示，true表示显示中，false表示隐藏中
    private MySharedPreferences mySharedPreferences;
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            switch(msg.what){
                //网络请求成功，里面根据值判断登录成功或失败
                case Constants.LOGIN_REQUEST_SUCCESS:
                    JSONObject jsonObject= (JSONObject) msg.obj;
                    try {
                        String success=jsonObject.getString("Success");
                        if(success.equals("True")){
                            //登录成功
                            //Log.i("Miparking","jsonObject----->>"+jsonObject.toString());
                            JSONObject userInfo=jsonObject.getJSONObject("Result");
                            //Log.i("Miparking","userInfo---->>"+userInfo.toString());
                            mySharedPreferences.setBooleanValue(Constants.LOGIN_ISLOGIN,true);
                            mySharedPreferences.setStringValue(Constants.COOKIE, AsyncHttpCilentUtil.getCookie(AsyncHttpCilentUtil.getCookieStore(LoginActivity.this)));
                            mySharedPreferences.setStringValue(Constants.REGISTER_PHONE,userInfo.getString("uid"));//保存手机号，带+86的
                            mySharedPreferences.setStringValue(Constants.REGISTER_NAME_DEFAULT_KEY,userInfo.getString("name"));//用户名称
                            mySharedPreferences.setStringValue(Constants.REGISTER_SEX_DEFAULT_KEY,userInfo.getString("sex"));//性别
                            mySharedPreferences.setStringValue(Constants.REGISTER_ICON_DEFAULT_KEY,userInfo.getString("icon"));//头像
                            mySharedPreferences.setStringValue(Constants.REGISTER_BIRTHDAY_DEFAULT_KEY,userInfo.getString("birthday"));//生日
                            mySharedPreferences.setFloatValue(Constants.LOGIN_BALANCE_DEFAULT_KEY,Float.valueOf(userInfo.getString("balance")));//余额

                            ToastUtil.shortToast(LoginActivity.this,"登录成功！");
                            Intent loginIntent=new Intent();
                            loginIntent.setClass(LoginActivity.this,MainActivity.class);
                            startActivity(loginIntent);
                            MyApplication.clearActivity();
                        }else{
                            //登录失败
                            String errorMessage=jsonObject.getString("ErrorMessage");
                            ToastUtil.shortToast(LoginActivity.this,errorMessage+"！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                //网络请求失败
                case Constants.LOGIN_REQUEST_FAILURE:
                    ToastUtil.shortToast(LoginActivity.this,LoginActivity.this.getResources().getString(R.string.internet_error_text));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        //若是之前登录过，就直接到主页
        if(mySharedPreferences.getBooleanValue(Constants.LOGIN_ISLOGIN)){
            Intent mainIntent=new Intent();
            mainIntent.setClass(this,MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    private void initView(){
        MyApplication.addActivity(this);
        forgetPwdTV= (TextView) findViewById(R.id.loginForgetpwdTV);
        countryTV= (TextView) findViewById(R.id.loginCountryTV);
        phoneET= (EditText) findViewById(R.id.loginPhoneET);
        showPwdTV= (TextView) findViewById(R.id.loginShowpasswordTV);
        passwordET= (EditText) findViewById(R.id.loginPasswordET);
        registerTV= (TextView) findViewById(R.id.loginRegisterTV);
        loginIV= (ImageView) findViewById(R.id.loginLoginIV);

        forgetPwdTV.setOnClickListener(this);
        countryTV.setOnClickListener(this);
        showPwdTV.setOnClickListener(this);
        registerTV.setOnClickListener(this);
        loginIV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
        mySharedPreferences.setStringValue(Constants.COUNTRY_ID,getResources().getString(R.string.default_country_id));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetUiUtil.setCountryId(this,mySharedPreferences,countryTV);
    }

    /**
     * 忘记密码
     */
    private void handleForgetPwd(){
        Intent forgetpwdIntent=new Intent();
        forgetpwdIntent.setClass(this,ForgetPwdActivity.class);
        startActivity(forgetpwdIntent);
    }

    /**
     * 区号
     */
    private void handleCountry(){
        Intent countryIntent=new Intent();
        countryIntent.setClass(this, CountryPageActivity.class);
        startActivity(countryIntent);
    }

    /**
     * 显示密码
     */
    private void handleShowPwd(){
        if(!isShow){
            showPwdTV.setText(getResources().getString(R.string.login_password_hide));
            passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isShow=true;
        }else{
            showPwdTV.setText(getResources().getString(R.string.login_password_show));
            passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isShow=false;
        }
    }

    /**
     * 注册
     */
    private void handleRegister(){
        Intent registerIntent=new Intent();
        registerIntent.setClass(this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * 登录
     */
    private void handleLogin(){
        String url= Paths.appUrl;
        //以下是接口需要的参数
        String timestamp= EncryptUtil.getTimestamp();
        String signatureNonce=EncryptUtil.getSignatureNonce();
        String action= Constants.LOGIN_ACTION;

        String phone=phoneET.getText().toString();//填写的手机号，不包括区号
        String uid=getResources().getString(R.string.country_plus)+mySharedPreferences.getStringValue(Constants.COUNTRY_ID)+phone;
        String password=passwordET.getText().toString();

        //通过签名算法得到的Signature
        Map<String,Object> paramsMap=new HashMap<>();
        paramsMap.put("Timestamp",timestamp);
        paramsMap.put("SignatureNonce",signatureNonce);
        paramsMap.put("Action",action);
        paramsMap.put("Uid",uid);
        paramsMap.put("Password",password);
        String signature=EncryptUtil.getSignature(paramsMap);

        if(AccountJudgeUtil.judgePhoneEmpty(this,phone)||AccountJudgeUtil.judgePasswordEmpty(this,password)){
            //不符合要求,return掉
            return;
        }else{
            //符合要求，提交服务器
            loadingDialog=DialogUtil.createLoadingDialog(this);
            LoginDaoImpl loginDaoImpl=new LoginDaoImpl();
            loginDaoImpl.postLogin(url,signature,timestamp,signatureNonce,action,uid,password,mHandler);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.loginForgetpwdTV:
                handleForgetPwd();
                break;

            case R.id.loginCountryTV:
                handleCountry();
                break;

            case R.id.loginShowpasswordTV:
                handleShowPwd();
                break;

            case R.id.loginRegisterTV:
                handleRegister();
                break;

            case R.id.loginLoginIV:
                handleLogin();
                break;
        }
    }
}
