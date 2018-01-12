package com.sziton.miparking.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by fwj on 2017/8/14.
 */

public class ToastUtil {

    public static void shortToast(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_LONG).show();
    }
}
