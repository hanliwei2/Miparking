package com.sziton.miparking.utils;

/**
 * Created by fwj on 2017/8/23.
 */

public class DataConvertUtil {

/*    public static String hexToAscii(String hex){
        StringBuilder sb=new StringBuilder();
        char[] array=hex.toCharArray();
        for(int i=0;i<array.length;i++){
            sb=sb.append((int)array[i]);
        }
        return sb.toString();
    }*/

    public static byte[] strToByteArray(String str){
        char[] charArry=str.toCharArray();
        //byte[] byteArray=new byte[charArry.length];
        byte[] byteArray=str.getBytes();
/*        for(int i=0;i<charArry.length;i++){

            String hexString=Integer.toHexString((int)charArry[i]);
            byteArray[i]=(byte)Integer.parseInt(hexString);
        }*/
        return byteArray;
    }



/*    public static byte[] strToByteArray(String str){
        byte[] byteArray=new byte[20];
        for(int i=0;i<str.length()/2;i=i+2){
            byteArray[i]=(byte)(Integer.parseInt("0x"+str.substring(i,i+2)));
        }
        return null;
    }*/
}
