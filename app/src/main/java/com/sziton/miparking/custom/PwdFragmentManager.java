package com.sziton.miparking.custom;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.inuker.bluetooth.library.BluetoothClient;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;

/**
 * Created by fwj on 2017/12/5.
 */

public class PwdFragmentManager {

    public static PwdFragment showPwdFragment(FragmentActivity fragmentActivity, PayPwdView.InputCallBack callBack, String title, String content){
        Bundle bundle = new Bundle();
        bundle.putString(PwdFragment.EXTRA_TITLE, title);
        bundle.putString(PwdFragment.EXTRA_CONTENT, content);

        PwdFragment fragment = new PwdFragment();
        fragment.setArguments(bundle);
        fragment.setPaySuccessCallBack(callBack);
        fragment.show(fragmentActivity.getSupportFragmentManager(),"pwd");
        return fragment;
    }

    /**
     * 关闭输入密码框
     * @param pwdFragment
     */
    public static void dismissPwdFragment(PwdFragment pwdFragment){
        if (pwdFragment != null && !pwdFragment.isHidden()) {
            pwdFragment.dismiss();
        }
    }

    /**
     * 关闭输入密码框，并断开设备
     * @param pwdFragment
     */
    public static void dismissAndDisconnect(PwdFragment pwdFragment){
        if (pwdFragment != null && !pwdFragment.isHidden()) {
            //点击x号的时候，把连接断开
            MySharedPreferences mySharedPreferences=MySharedPreferences.getInstance(MyApplication.getInstance());
            if(!TextUtils.isEmpty(mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC))){
                BluetoothClient mClient= ClientManager.getClient();
                mClient.disconnect(mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC));
            }
            pwdFragment.dismiss();
        }
    }

}
