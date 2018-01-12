package com.sziton.miparking.sms;

import android.os.Handler;
import android.os.Message;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by fwj on 2017/11/16.
 */

public class MobSmsUtil {
    private static EventHandler eh;

    /**
     * 获取验证码短信
     * @param countryId 区号，如86
     * @param phone 手机号
     */
    public static void getVerificationCode(String countryId,String phone){
        //SMSSDK中的函数，用于获取验证码短信
        SMSSDK.getVerificationCode(countryId,phone);
    }

    /**
     * 提交验证码
     * @param countryId 区号，如86
     * @param phone 手机号
     * @param verificationCode 验证码
     */
    public static void submitVerificationCode(String countryId,String phone,String verificationCode){
        //SMSSDK中的函数，用于提交验证码短信
        SMSSDK.submitVerificationCode(countryId,phone,verificationCode);
    }

    /**
     * 注册短信回调，在使用短信sdk前先调用这个方法进行注册
     * @param mHandler 用于把短信返回的值传出去
     */
    public static void registerEventHandler(final Handler mHandler){
        if(eh==null){
            eh=new EventHandler(){
                @Override
                public void afterEvent(int event, int result, Object data) {
                    Message msg = new Message();
                    msg.arg1 = event;
                    msg.arg2 = result;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                }
            };
        }
        /*EventHandler eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        };*/
        SMSSDK.registerEventHandler(eh);
    }

    /**
     * 在activity的ondestory里调用，用于关闭短信的广播
     */
    public static void unregisterEventHandler(){
        if(eh!=null){
            SMSSDK.unregisterEventHandler(eh);
            eh=null;
        }
    }

//提供的smssdk的demo里的方法,用于在页面上弹出对应情况的弹框，这个各种情况分别写到对应的activity上，所以这里注掉
/*    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg) {

            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                System.out.println("--------result"+event);
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();

                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    //已经验证
                    Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();


                } else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                    //已经验证
                    Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
                    textV.setText(data.toString());


                }

            } else {
//				((Throwable) data).printStackTrace();
//				Toast.makeText(MainActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
//					Toast.makeText(MainActivity.this, "123", Toast.LENGTH_SHORT).show();
                int status = 0;
                try {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        Toast.makeText(MainActivity.this, des, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    SMSLog.getInstance().w(e);
                }
            }


        }
    };*/
}
