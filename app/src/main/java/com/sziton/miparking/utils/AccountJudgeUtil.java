package com.sziton.miparking.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fwj on 2017/11/17.
 */

public class AccountJudgeUtil {


    //判断手机号是否符合要求，即手机号判断的正则
    public static boolean judgePhone(Context context,String phone){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(phone);
        if(m.matches()==false){
            ToastUtil.shortToast(context,"手机号格式不正确！");
        }
        return m.matches();
    }

    //判断密码是否符合要求
    public static boolean judgePassword(Context context,String password){
        boolean isPassword=true;
        if(password.length()<8){
            ToastUtil.shortToast(context,"密码长度至少为8个字符！");
            isPassword=false;
        }
        return isPassword;
    }

    //登陆时
    //判断手机号是否为空,true为空,false不为空
    public static boolean judgePhoneEmpty(Context context,String phone){
        boolean isPhoneEmpty=false;
        if(TextUtils.isEmpty(phone)){
            ToastUtil.shortToast(context,"手机号不能为空！");
            isPhoneEmpty=true;
        }
        return isPhoneEmpty;
    }

    //判断密码是否为空,true为空，false不为空
    public static boolean judgePasswordEmpty(Context context,String password){
        boolean isPasswordEmpty=false;
        if(TextUtils.isEmpty(password)){
            ToastUtil.shortToast(context,"密码不能为空！");
            isPasswordEmpty=true;
        }
        return isPasswordEmpty;
    }

}
