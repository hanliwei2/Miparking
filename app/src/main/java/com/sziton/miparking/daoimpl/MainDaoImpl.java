package com.sziton.miparking.daoimpl;

import android.os.Handler;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.dao.MainDao;
import com.sziton.miparking.utils.HandlerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by fwj on 2017/11/20.
 */

public class MainDaoImpl {
    private final int requestSuccess= Constants.MAIN_VIEWPAGER_SUCCESS;
    private final int requestFailure=Constants.MAIN_VIEWPAGER_FAILURE;

    public void postGetAdvertIcon(String url, String signature, String timestamp, String signatureNonce, String action, final Handler handler){
        MainDao mainDao=new MainDao();
        mainDao.postGetAdvertIcon(url,signature,timestamp,signatureNonce,action,new Callback(){

            @Override
            public void onFailure(Request request, IOException e) {
                HandlerUtil.sendMessage(requestFailure,handler);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    //response.body()返回的是一个字符流，这里必须用.string()，里面是new了一个String,即把字符流转成了String
                    String result=response.body().string();
                    try {
                        JSONObject jsonObject=new JSONObject(result);
                        HandlerUtil.sendMessage(requestSuccess,jsonObject,handler);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        HandlerUtil.sendMessage(requestFailure,handler);
                    }
                }else{
                    HandlerUtil.sendMessage(requestFailure,handler);
                }
            }
        });

    }
}
