package com.sziton.miparking.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;

/**
 * Created by fwj on 2017/11/16.
 */

public class SetUiUtil {

    /**
     * 设置区号（登录页，注册页，忘记密码页）
     * @param context 上下文
     * @param mySharedPreferences sp对象
     * @param countryTV 区号的textview
     */
    public static void setCountryId(Context context,MySharedPreferences mySharedPreferences, TextView countryTV){
        if(TextUtils.isEmpty(mySharedPreferences.getStringValue(Constants.COUNTRY_ID))){
            mySharedPreferences.setStringValue(Constants.COUNTRY_ID,context.getResources().getString(R.string.default_country_id));
            countryTV.setText(context.getResources().getString(R.string.country_plus)+context.getResources().getString(R.string.default_country_id));
        }else{
            countryTV.setText(context.getResources().getString(R.string.country_plus)+mySharedPreferences.getStringValue(Constants.COUNTRY_ID));
        }
    }
}
