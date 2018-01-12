package com.sziton.miparking.utils;

import android.text.TextUtils;

import com.sziton.miparking.constants.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by fwj on 2017/11/17.
 */

public class EncryptUtil {

    /**
     *
     * @param paramsMap 参数的map，即(Timestamp,string)，(SignatureNonce,string)和接口需要的参数，都是key和value的形式
     * @return
     */
    public static String getSignature(Map<String,Object> paramsMap){
        //map按key正序排序
        Map<String,Object> sortMap=sortMapByKey(paramsMap);
        //拼接成key1=value1&key2=value2&key3=value3这样形式的string,最后在前面拼接上固定的Secretkey=Secretvalue
        String signature="";
        for(Map.Entry<String,Object> entry:sortMap.entrySet()){
            signature=signature+"&"+entry.getKey()+"="+entry.getValue();
        }
        signature= Constants.SECRET_VALUE+signature;
        //md5加密，传进去的str加密后变成byte[]，最后转成16进制的string输出
        signature=md5(signature);
        return signature;
    }

    /**
     * 时间戳（1970到现在的秒数）
     * api每次请求时都要获取时间戳
     */
    public static String getTimestamp(){
        long currentTimeMillis=System.currentTimeMillis();
        long currentTimeSeconds=currentTimeMillis/1000;//转成秒
        String timestamp=String.valueOf(currentTimeSeconds);
        return timestamp;
    }

    /**
     * 唯一随机数，用于防止网络重放攻击。用户在不同请求间要使用不同的随机数值
     */
    public static String getSignatureNonce(){
        UUID uuid=UUID.randomUUID();
        String signatureNonce=uuid.toString();
        return signatureNonce;
    }


    /**
     * 把map按key正序排序，Signature签名算法中使用
     * @param map
     * @return
     */
    public static Map<String,Object> sortMapByKey(Map<String,Object> map){
        if(map==null||map.isEmpty()){
            return null;
        }
        Map<String,Object> sortMap=new TreeMap<String, Object>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    /**
     * md5加密，Signature签名算法中使用
     * 传进去的str加密后变成byte[]，最后转成16进制的string输出
     * @param string
     * @return
     */
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


}

/**
 * 正序比较器
 */
class MapKeyComparator implements Comparator<String>{

    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
}


