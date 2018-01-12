package com.sziton.miparking.encryption;

import android.os.Build;
import android.util.Log;

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by fwj on 2017/12/4.
 */

public class EncryptManager {

    /**
     * 加密key，key是动态生成的
     * @return
     */
    public static String keyEncrypt(String secret){
        byte[] byteArray=new byte[8];
        String key="";
        for(int i=0;i<secret.length()/2;i++){
            byteArray[i]=(byte)Integer.parseInt(secret.substring(i*2,i*2+2));
        }
        byteArray[3]=byteArray[0];
        byteArray[4]=byteArray[1];
        byteArray[5]=byteArray[2];
        byteArray[6]=byteArray[0];
        byteArray[7]=byteArray[1];
        for(int i=0;i<byteArray.length;i++){
            key=key+byteArray[i];
        }
        return key;
    }

    /**
     * 配对，密码填写两遍
     * 前48位加密，后16位不加密
     * @param oldSecret
     * @param newSecret
     * @return
     */
    public static byte[] pairEncrypt(String oldSecret,String newSecret){
        String key=keyEncrypt(oldSecret);
        byte[] pairData=null;
        String pairEncryption = "";
        String idHigh="7";//指令id高位，固定为7
        String secretOld=oldSecret;//设置的锁的密码
        String secretNew=newSecret;//重复一遍锁的密码
        //手机唯一的mac地址，重复写两遍，为了撑满24位
        String phoneMac=getMacAddr().replace(":","");
        String mac=phoneMac+phoneMac;
        String manager="0";//0代表非管理员，即配对；1代表管理员，即修改
        String temp="000000000";//固定这么写
        String idLow="1";//指令id低位，固定这么写
        //加密前的48位字符串
        String pairStr=idHigh+secretOld+secretNew+mac+manager+temp+idLow;
        Log.i("Miparing--data",pairStr);
        //每16位加密一次
        try {
            for (int i = 0; i < pairStr.length(); i = i + 16) {
                String everyStr = pairStr.substring(i, i + 16);
                pairEncryption = pairEncryption + Unit8.EncryStrHex(everyStr, key);
            }
        } catch (Exception e) {
            //ToastUtil.shortToast(MyApplication.getInstance(), "配对加密出错！");
        }
        String phone= Build.MODEL;//手机型号
        //判断一下手机型号的长度，若不足16位子，则用0补足16位子；若超过16位，则截取前面16位
        if(phone.length()<16){
            int subLength=16-phone.length();
            for(int i=0;i<subLength;i++){
                phone=phone+"0";
            }
        }else if(phone.length()>16){
            phone=phone.substring(0,16);
        }
        pairData=(pairEncryption+phone).getBytes();
        return pairData;
    }

    /**
     * 修改密码，
     * @param oldSecret 旧密码
     * @param newSecret 新密码
     * @return
     */
    public static byte[] modifySecretEncrypt(String oldSecret,String newSecret){
        String key=keyEncrypt(oldSecret);
        byte[] modifySecretData=null;
        String modifySecretEncryption = "";
        String idHigh="7";//指令id高位，固定为7
        String secretOld=oldSecret;//设置的锁的密码
        String secretNew=newSecret;//重复一遍锁的密码
        //手机唯一的mac地址，重复写两遍，为了撑满24位
        String phoneMac=getMacAddr().replace(":","");
        String mac=phoneMac+phoneMac;
        String manager="1";//0代表非管理员，即配对；1代表管理员，即修改
        String temp="000000000";//固定这么写
        String idLow="1";//指令id低位，固定这么写
        //加密前的字符串
        String modifySecretStr=idHigh+secretOld+secretNew+mac+manager+temp+idLow;
        //每16位加密一次
        try {
            for (int i = 0; i < modifySecretStr.length(); i = i + 16) {
                String everyStr = modifySecretStr.substring(i, i + 16);
                modifySecretEncryption = modifySecretEncryption + Unit8.EncryStrHex(everyStr, key);
            }
        } catch (Exception e) {
            //ToastUtil.shortToast(MyApplication.getInstance(), "修改密码加密出错！");
        }
        String phone= Build.MODEL;//手机型号
        //判断一下手机型号的长度，若不足16位子，则用0补足16位子；若超过16位，则截取前面16位
        if(phone.length()<16){
            int subLength=16-phone.length();
            for(int i=0;i<subLength;i++){
                phone=phone+"0";
            }
        }else if(phone.length()>16){
            phone=phone.substring(0,16);
        }
        modifySecretData=(modifySecretEncryption+phone).getBytes();
        return modifySecretData;
    }


    /**
     * 查询锁开启状态
     * @param oldSecret 重复两遍密码
     * @param newSecret
     * @return
     */
    public static byte[] inquireLockStatusEncrypt(String oldSecret,String newSecret){
        String key=keyEncrypt(oldSecret);
        Log.i("Miparing--SendData","加密时的key为："+key);
        byte[] inquireLockStatusData=null;
        String inquireLockStatusEncryption = "";
        String idHigh="7";//指令id高位，固定为7
        String secretOld=oldSecret;//设置的锁的密码
        String secretNew=newSecret;//重复一遍锁的密码
        //手机唯一的mac地址，重复写两遍，为了撑满24位
        String phoneMac=getMacAddr().replace(":","");
        String mac=phoneMac+phoneMac;
        //根据文档，获取当前时间，格式为YYMMDDHHmm
        SimpleDateFormat formater = new SimpleDateFormat("yyMMddHHmm");
        Date date = new Date(System.currentTimeMillis());
        String time = formater.format(date);//挂失时间取当前时间
        String idLow="7";//指令id低位，固定这么写
        //加密前的字符串
        String inquireLockStatusStr=idHigh+secretOld+secretNew+mac+time+idLow;
        Log.i("Miparing--SendData","加密前的查询字符串："+inquireLockStatusStr);
        //每16位加密一次
        try {
            for (int i = 0; i < inquireLockStatusStr.length(); i = i + 16) {
                String everyStr = inquireLockStatusStr.substring(i, i + 16);
                inquireLockStatusEncryption = inquireLockStatusEncryption + Unit8.EncryStrHex(everyStr, key);
            }
        } catch (Exception e) {
            //ToastUtil.shortToast(MyApplication.getInstance(), "查询锁状态加密出错！");
        }
        Log.i("Miparing--SendData","加密后的查询字符串："+inquireLockStatusEncryption);
        String phone= Build.MODEL;//手机型号
        //判断一下手机型号的长度，若不足16位子，则用0补足16位子；若超过16位，则截取前面16位
        if(phone.length()<16){
            int subLength=16-phone.length();
            for(int i=0;i<subLength;i++){
                phone=phone+"0";
            }
        }else if(phone.length()>16){
            phone=phone.substring(0,16);
        }
        inquireLockStatusData=(inquireLockStatusEncryption+phone).getBytes();

        String logData="";
        for(int i=0;i<inquireLockStatusData.length;i++){
            int intData=inquireLockStatusData[i]&0xFF;
            logData=logData+intData+" ";
        }
        Log.i("Miparing--SendData","拼接后的数据为："+logData);
        return inquireLockStatusData;
    }

    /**
     * 开锁和关锁
     * @param lockSecret 锁的密码
     * @return
     */
    public static byte[] unlockImmediatelyEncrypt(String lockSecret){
        String key=keyEncrypt(lockSecret);
        byte[] unlockImmediatelyData=null;
        String unlockImmediatelyEncryption = "";
        String idHigh="7";//指令id高位，固定为7
        String secret=lockSecret;//设置的锁的密码
        //手机唯一的mac地址，重复写两遍，为了撑满24位
        String phoneMac=getMacAddr().replace(":","");
        String mac=phoneMac+phoneMac;
        //根据文档，获取当前时间，格式为YYMMDDHHmm
        SimpleDateFormat formater = new SimpleDateFormat("yyMMddHHmm");
        Date date = new Date(System.currentTimeMillis());
        String time = formater.format(date);//挂失时间取当前时间
        String index="000000";//mac地址所属于的地址id
        String idLow="8";//指令id低位，固定这么写
        //加密前的字符串
        String unlockImmediatelyStr=idHigh+secret+mac+time+index+idLow;
        //每16位加密一次
        try {
            for (int i = 0; i < unlockImmediatelyStr.length(); i = i + 16) {
                String everyStr = unlockImmediatelyStr.substring(i, i + 16);
                unlockImmediatelyEncryption = unlockImmediatelyEncryption + Unit8.EncryStrHex(everyStr, key);
            }
        } catch (Exception e) {
            //ToastUtil.shortToast(MyApplication.getInstance(), "开关锁加密出错！");
        }
        String phone= Build.MODEL;//手机型号
        //判断一下手机型号的长度，若不足16位子，则用0补足16位子；若超过16位，则截取前面16位
        if(phone.length()<16){
            int subLength=16-phone.length();
            for(int i=0;i<subLength;i++){
                phone=phone+"0";
            }
        }else if(phone.length()>16){
            phone=phone.substring(0,16);
        }
        unlockImmediatelyData=(unlockImmediatelyEncryption+phone).getBytes();
        return unlockImmediatelyData;
    }


    /**
     * 获取手机唯一的mac地址,兼容6.0,7.0及以上
     */
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 用于测试加密是否正确
     * @param secret
     */
    public static void testEncrypt(String secret){
        String key=keyEncrypt(secret);
        Log.i("Miparking--Encrypt","加密的key为："+key);
        String unlockImmediatelyEncryption = "";
        //加密前的字符串
        String unlockImmediatelyStr="015109021825601510902182512345612345611171503009";
        //每16位加密一次
        try {
            for (int i = 0; i < unlockImmediatelyStr.length(); i = i + 16) {
                String everyStr = unlockImmediatelyStr.substring(i, i + 16);
                unlockImmediatelyEncryption = unlockImmediatelyEncryption + Unit8.EncryStrHex(everyStr, key);
            }
        } catch (Exception e) {
            //ToastUtil.shortToast(MyApplication.getInstance(), "开关锁加密出错！");
            Log.i("Miparking--Encrypt","加密出错");
        }
        Log.i("Miparking--Encrypt","加密的值为："+unlockImmediatelyEncryption);
        /*String logData="";
        for(int i=0;i<unlockImmediatelyEncryption.getBytes().length;i++){
            int intData=unlockImmediatelyEncryption.getBytes()[i];
            logData=logData+intData+" ";
        }
        Log.i("Miparking--Encrypt","转为byte[]后的值为："+logData);*/
    }

}
