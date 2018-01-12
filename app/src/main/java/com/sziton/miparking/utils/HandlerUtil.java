package com.sziton.miparking.utils;

import android.os.Handler;
import android.os.Message;

public class HandlerUtil {

	/**
	 * 不带参数
	 * @param code 表示msg.what的值
	 * @param handler
	 */
	public static void sendMessage(int code,Handler handler){
		Message msg=new Message();
		msg.what=code;
		handler.sendMessage(msg);
	}
	
	/**
	 * 带obj参数
	 * @param code 表示msg.what的值
	 * @param obj  发送的内容，obj格式
	 * @param handler 传进来的handler
	 */
	public static void sendMessage(int code,Object obj,Handler handler){
		Message msg=new Message();
		msg.what=code;
		msg.obj=obj;
		handler.sendMessage(msg);
	}

	/**
	 *
	 * @param code 表示msg.what的值
	 * @param str 发送的内容，String格式
	 * @param handler
	 */
	public static void sendFailureMessage(int code,String str,Handler handler){
		Message msg=new Message();
		msg.what=code;
		msg.obj=str;
		handler.sendMessage(msg);
	}

	
	public static void sendUpdateUi(int code,String str,Handler handler){
		Message msg=new Message();
		msg.what=code;
		msg.obj=str;
		handler.sendMessage(msg);
	}
	
	public static void sendSkipMessage(int code,Handler handler){
		Message msg=new Message();
		msg.what=code;
		handler.sendMessage(msg);
	}
}
