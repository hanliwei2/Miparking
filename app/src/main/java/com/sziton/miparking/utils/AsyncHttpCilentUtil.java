package com.sziton.miparking.utils;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.cookie.Cookie;

import java.security.KeyStore;
import java.util.List;

public class AsyncHttpCilentUtil {
	
	private static AsyncHttpClient client = null;  
    private static PersistentCookieStore cookie_store=null;
    private static String cookie="";
	
    /**
     * 获取AsyncHttpClient的单例
     * @return
     */
    public synchronized static AsyncHttpClient getClient(){  
        if(client ==null){
            client = new AsyncHttpClient();
            client.setTimeout(1000*10);
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MyCustomSSLFactory socketFactory = new MyCustomSSLFactory(trustStore);
                socketFactory.setHostnameVerifier(MyCustomSSLFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*SSLSocketFactory socketFactory= SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(socketFactory);*/
            /*SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);*/
        }  
        return client;  
    }  
    
    
    /**
     * 请求成功会自动获取cookie并保存在cookie_store
     * 
     * 获取PersistentCookieStore的单例，这是AsyncHttpClient里封装好的，用来管理cookie的对象
     * 
     * 获取已经保存的cookie的方法
     * String cookie=cookie_store.getCookies().toString();
     * 
     *添加自己的cookie到 cookie_store
     *BasicClientCookie newCookie = new BasicClientCookie("cookiesare", "awesome");
	 *newCookie.setVersion(1);
	 *newCookie.setDomain("mydomain.com");
	 *newCookie.setPath("/");
	 *myCookieStore.addCookie(newCookie);
     * @param context
     * @return
     * 
     * myCookieStore.clear();在设置cookie之前先清除cookie
     */
    public synchronized static PersistentCookieStore getCookieStore(Context context){
    	if(cookie_store==null){
    		cookie_store=new PersistentCookieStore(context);
    	}
    	Log.i("MyLog", "cookiestore------->>"+cookie_store);
    	return cookie_store;
    }
    
    public synchronized static String getCookie(PersistentCookieStore cookie_store){
    	List<Cookie> cookielist=cookie_store.getCookies();
    	if(cookielist.isEmpty()){
    	}else{
    		for (int i = 0; i < cookielist.size(); i++) {
    			cookie = cookielist.get(i).getName()+"="+cookielist.get(i).getValue()+";";
    		}
    	}
    	return cookie;
    }

}



