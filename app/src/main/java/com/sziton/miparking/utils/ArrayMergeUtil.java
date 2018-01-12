package com.sziton.miparking.utils;

import java.util.Arrays;

/**
 * Created by fwj on 2017/12/6.
 */

public class ArrayMergeUtil {

    public static byte[] byteArrayMerge(byte[] first,byte[] second){
        byte[] result= Arrays.copyOf(first,first.length+second.length);
        //5个参数分别是，源数组，源数组要复制的起始位置，目的数组，目的数组放置的起始位置，复制的长度
        System.arraycopy(second,0,result,first.length,second.length);
        return result;
    }
}
