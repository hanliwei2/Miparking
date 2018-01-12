package com.sziton.miparking.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

//加密传入的数据是byte类型的，并非使用decode方法将原始数据转二进制，String类型的数据 使用 str.getBytes()即可
public class Base64Convert {
	//base64加密
	//在这里使用的是encode方式，返回的是byte类型加密数据，可使用new String转为String类型
	public static String encode(String s){
		String string=new String(Base64.encode(s.getBytes(), Base64.DEFAULT));
		return string;
	}
	
	//base64解密
	//传入base64字符串
	public static String decode(String s){
		String string=new String(Base64.decode(s.getBytes(), Base64.DEFAULT));
		return string;
	}

	/**
	 * 将bitmap转换成base64字符串
	 *
	 */
	public static String Bitmap2StrByBase64(Bitmap bit){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩 
		byte[] bytes=bos.toByteArray();
		return Base64.encodeToString(bytes,Base64.DEFAULT);
	}

	/**
	 * base64转为bitmap
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
}
