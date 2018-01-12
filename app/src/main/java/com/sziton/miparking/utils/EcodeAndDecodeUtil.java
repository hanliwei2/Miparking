package com.sziton.miparking.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by fwj on 2017/12/8.
 */

public class EcodeAndDecodeUtil {

    /**
     * URL编码，encode方法的第二个参数可以指定编码格式
     * @param str
     * @return
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result=str;
        }
        return result;
    }

    /**
     * URL解码，decode方法的第二个参数可以指定编码格式
     * @param str
     * @return
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result=str;
        }
        return result;
    }
}
