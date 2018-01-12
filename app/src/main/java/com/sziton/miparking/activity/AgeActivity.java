package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.daoimpl.PersonalDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.DialogUtil;
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

public class AgeActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private DatePicker ageDP;
    private TextView saveTV;
    private MySharedPreferences mySharedPreferences;
    private String ageValue;
    private Dialog loadingDialog;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            switch(msg.what){
                case Constants.PERSONAL_REQUEST_SUCCESS:
                    JSONObject jsonObject= (JSONObject) msg.obj;
                    try {
                        String success=jsonObject.getString("Success");
                        if(success.equals("True")){
                            //修改成功
                            mySharedPreferences.setStringValue(Constants.REGISTER_BIRTHDAY_DEFAULT_KEY,ageValue);//生日,2017-01-01的格式

                            ToastUtil.shortToast(AgeActivity.this,"修改成功！");
                            finish();
                        }else{
                            //修改失败
                            String errorMessage=jsonObject.getString("ErrorMessage");
                            ToastUtil.shortToast(AgeActivity.this,errorMessage+"！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.PERSONAL_REQUEST_FAILURE:
                    ToastUtil.shortToast(AgeActivity.this,AgeActivity.this.getResources().getString(R.string.internet_error_text));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.ageBackRL);
        ageDP= (DatePicker) findViewById(R.id.ageDP);
        saveTV= (TextView) findViewById(R.id.ageSaveTV);

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

        String uid=mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
        String ageKey=Constants.PERSONAL_AGE_KEY;//map中年龄的key

        //获取选择器中的年月日
        int year=ageDP.getYear();
        String yearStr=year+"";
        int month=ageDP.getMonth()+1;
        String monthStr=month+"";
        if(monthStr.length()==1){
            monthStr="0"+monthStr;
        }
        int date=ageDP.getDayOfMonth();
        String dateStr=date+"";

        ageValue=yearStr+"-"+monthStr+"-"+dateStr;

        //通过签名算法得到的Signature
        Map<String,Object> paramsMap=new HashMap<>();
        paramsMap.put("Timestamp",timestamp);
        paramsMap.put("SignatureNonce",signatureNonce);
        paramsMap.put("Action",action);
        paramsMap.put("Uid",uid);
        paramsMap.put(ageKey,ageValue);
        String signature=EncryptUtil.getSignature(paramsMap);


        //符合要求，提交服务器
        loadingDialog= DialogUtil.createLoadingDialog(this);
        PersonalDaoImpl personalDaoImpl=new PersonalDaoImpl();
        personalDaoImpl.postPersonal(url,signature,timestamp,signatureNonce,action,uid,ageKey,ageValue,mHandler);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ageBackRL:
                handleBack();
                break;

            case R.id.ageSaveTV:
                handleSave();
                break;
        }
    }
}
