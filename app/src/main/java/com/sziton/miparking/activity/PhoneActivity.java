package com.sziton.miparking.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.daoimpl.PersonalDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fwj on 2017/11/29.
 */

public class PhoneActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private EditText phoneET;
    private TextView saveTV;
    private MySharedPreferences mySharedPreferences;
    private String phoneValue;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Constants.PERSONAL_REQUEST_SUCCESS:
                    JSONObject jsonObject= (JSONObject) msg.obj;
                    try {
                        String success=jsonObject.getString("Success");
                        if(success.equals("True")){
                            //修改成功
                            String uid=getResources().getString(R.string.country_plus)+mySharedPreferences.getStringValue(Constants.COUNTRY_ID)+phoneValue;
                            mySharedPreferences.setStringValue(Constants.REGISTER_PHONE,uid);//用户手机号，带+86

                            ToastUtil.shortToast(PhoneActivity.this,"修改成功！");
                            finish();
                        }else{
                            //修改失败
                            String errorMessage=jsonObject.getString("ErrorMessage");
                            ToastUtil.shortToast(PhoneActivity.this,errorMessage+"！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.PERSONAL_REQUEST_FAILURE:
                    ToastUtil.shortToast(PhoneActivity.this,"网络异常！");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.phoneBackRL);
        phoneET= (EditText) findViewById(R.id.phoneET);
        saveTV= (TextView) findViewById(R.id.phoneSaveTV);

        backRL.setOnClickListener(this);
        saveTV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences= MySharedPreferences.getInstance(this);
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 保存
     */
    private void handleSave(){
        String url= Paths.appUrl;
        //以下是接口需要的参数
        String timestamp= EncryptUtil.getTimestamp();
        String signatureNonce=EncryptUtil.getSignatureNonce();
        String action= Constants.PERSONAL_ACTION;

        String uid=getResources().getString(R.string.country_plus)+mySharedPreferences.getStringValue(Constants.COUNTRY_ID)+mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
        String phoneKey=Constants.PERSONAL_PHONE_KEY;//map中手机号的key
        phoneValue=phoneET.getText().toString();//填写的手机号

        //通过签名算法得到的Signature
        Map<String,Object> paramsMap=new HashMap<>();
        paramsMap.put("Timestamp",timestamp);
        paramsMap.put("SignatureNonce",signatureNonce);
        paramsMap.put("Action",action);
        paramsMap.put("Uid",uid);
        paramsMap.put(phoneKey,phoneValue);
        String signature=EncryptUtil.getSignature(paramsMap);

        if(TextUtils.isEmpty(phoneValue)){
            //不符合要求,return掉
            ToastUtil.shortToast(this,"手机号不能为空！");
            return;
        }else{
            //符合要求，提交服务器
            PersonalDaoImpl personalDaoImpl=new PersonalDaoImpl();
            personalDaoImpl.postPersonal(url,signature,timestamp,signatureNonce,action,uid,phoneKey,phoneValue,mHandler);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.phoneBackRL:
                handleBack();
                break;

            case R.id.phoneSaveTV:
                handleSave();
                break;
        }
    }
}
