package com.sziton.miparking.dao;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.sziton.miparking.utils.EcodeAndDecodeUtil;
import com.sziton.miparking.okhttp.OkHttpUtil;

/**
 * Created by fwj on 2017/11/20.
 */

public class MainDao {

    /**
     *
     * @param url https://www.shareparking.link:4433/HandlerApp.ashx
     * @param signature 签名，签名算法见vss文档
     * @param timestamp 时间戳（1970到现在的秒数）
     * @param signatureNonce 唯一随机数，用于防止网络重放攻击。用户在不同请求间要使用不同的随机数值。
     * @param action GetAdvertIcon
     * @param responseCallback 回调函数
     */
    public void postGetAdvertIcon(String url, String signature, String timestamp, String signatureNonce, String action, Callback responseCallback){
        /**
         * 服务器端url解码了，所以这里要url编码，
         */
        signature= EcodeAndDecodeUtil.getURLEncoderString(signature);
        timestamp= EcodeAndDecodeUtil.getURLEncoderString(timestamp);
        signatureNonce= EcodeAndDecodeUtil.getURLEncoderString(signatureNonce);
        action= EcodeAndDecodeUtil.getURLEncoderString(action);

        //开始okhttp请求网络
        OkHttpClient mOkHttpClient= OkHttpUtil.getClient();
        RequestBody formBody=new FormEncodingBuilder()
                .add("Signature",signature)
                .add("Timestamp",timestamp)
                .add("SignatureNonce",signatureNonce)
                .add("Action",action)
                .build();

        Request request=new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call=mOkHttpClient.newCall(request);
        call.enqueue(responseCallback);
    }

}
