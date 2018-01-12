package com.sziton.miparking.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.Base64Convert;
import com.sziton.miparking.utils.HandlerUtil;
import com.sziton.miparking.utils.ToastUtil;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

/**
 * Created by fwj on 2017/11/8.
 */

public class SettingActivity extends Activity implements View.OnClickListener {
    private RelativeLayout backRL;
    private RelativeLayout righttopRL;
    private RoundedImageView headIV;
    private TextView nameTV;
    private TextView phoneTV;
    private LinearLayout lockmanagerLL;
    private LinearLayout helpcenterLL;
    private LinearLayout shareLL;
    private LinearLayout systemmsgLL;
    private MySharedPreferences mySharedPreferences;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //分享成功
                case Constants.SHARE_SUCCESS:
                    ToastUtil.shortToast(MyApplication.getInstance(), getResources().getString(R.string.share_success));
                    break;
                //分享失败
                case Constants.SHARE_ERROR:
                    ToastUtil.shortToast(MyApplication.getInstance(), getResources().getString(R.string.share_error));
                    break;
                //分享取消
                case Constants.SHARE_CANCEL:
                    ToastUtil.shortToast(MyApplication.getInstance(), getResources().getString(R.string.share_cancel));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initData();
    }

    private void initView() {
        MyApplication.addActivity(this);
        backRL = (RelativeLayout) findViewById(R.id.settingBackRL);
        righttopRL = (RelativeLayout) findViewById(R.id.settingRighttopRL);
        headIV = (RoundedImageView) findViewById(R.id.settingHeadIV);
        nameTV = (TextView) findViewById(R.id.settingNameTV);
        phoneTV = (TextView) findViewById(R.id.settingPhoneTV);
        lockmanagerLL = (LinearLayout) findViewById(R.id.settingLockmanagerLL);
        helpcenterLL = (LinearLayout) findViewById(R.id.settingHelpcenterLL);
        shareLL = (LinearLayout) findViewById(R.id.settingShareLL);
        systemmsgLL = (LinearLayout) findViewById(R.id.settingSystemmsgLL);

        backRL.setOnClickListener(this);
        headIV.setOnClickListener(this);
        //righttopRL.setOnClickListener(this);
        lockmanagerLL.setOnClickListener(this);
        helpcenterLL.setOnClickListener(this);
        shareLL.setOnClickListener(this);
        systemmsgLL.setOnClickListener(this);

    }

    private void initData() {
        mySharedPreferences = MySharedPreferences.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //登录成功，从返回的数据里获取icon作为头像
        String iconStr = mySharedPreferences.getStringValue(Constants.REGISTER_ICON_DEFAULT_KEY);
        if (!TextUtils.isEmpty(iconStr)) {
            Bitmap iconBitmap = Base64Convert.base64ToBitmap(iconStr);
            headIV.setImageBitmap(iconBitmap);
        }
        //设置名字
        String name = mySharedPreferences.getStringValue(Constants.REGISTER_NAME_DEFAULT_KEY);
        if (!TextUtils.isEmpty(name)) {
            nameTV.setText(name);
        }
        //设置电话
        String phone = mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);//这个带+86的，设置前吧+86去掉
        //暂时不去掉+86
        if (!TextUtils.isEmpty(phone)) {
            phoneTV.setText(phone);
        }

        /*if(!TextUtils.isEmpty(phone)&&phone.length()>=11){
            phoneTV.setText(phone.substring(phone.length()-11,phone.length()));
        }*/
    }

    /**
     * 返回
     */
    private void handleBack() {
        finish();
    }

    /**
     * 头像
     */
    private void handleHead() {
        Intent personalIntent = new Intent();
        personalIntent.setClass(this, PersonalActivity.class);
        startActivity(personalIntent);
    }

    /**
     * 地锁管理
     */
    private void handleLockManager() {
        Intent lockmanagerIntent = new Intent();
        lockmanagerIntent.setClass(this, LockManagerActivity.class);
        startActivity(lockmanagerIntent);
    }

    /**
     * 帮助中心
     */
    private void handleHelpCenter() {
        Intent helpcenterIntent = new Intent();
        helpcenterIntent.setClass(this, HelpCenterActivity.class);
        startActivity(helpcenterIntent);
    }

    /**
     * 分享给好友
     */
    private void handleShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // text是分享文本，所有平台都需要这个字段
        //oks.setText(getResources().getString(R.string.share_content));
        //设置
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {

                //QQ
                if ("QQ".equals(platform.getName())) {
                    //分享链接，即使用mob文档上的分享链接这条
                    paramsToShare.setTitle(getString(R.string.app_name));
                    paramsToShare.setTitleUrl(Constants.SHARE_URL);
                    paramsToShare.setText(getResources().getString(R.string.share_content));
                    paramsToShare.setImageUrl(Constants.SHARE_IMAGE_URL);
                }
                //QQ空间
                if ("QZone".equals(platform.getName())) {
                    //分享文本，如果要分享图文的话，分享网络图片无效，图片必须是本地图片，且要上传到mob的服务器，所以这里暂时只分享文本
                    paramsToShare.setTitle(getString(R.string.app_name));
                    paramsToShare.setTitleUrl(Constants.SHARE_URL);
                    paramsToShare.setText(getResources().getString(R.string.share_content));
                    //paramsToShare.setImagePath(Constants.SHARE_IMAGE_URL);
                    paramsToShare.setSite(getString(R.string.app_name));
                    paramsToShare.setSiteUrl(Constants.SHARE_URL);
                }
                //微信好友
                if ("Wechat".equals(platform.getName())) {
                    //分享网页
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                    //标题
                    paramsToShare.setTitle(getString(R.string.app_name));
                    paramsToShare.setText(getResources().getString(R.string.share_content));
                    //设置分享的图标
                    Bitmap imageData = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_miparking);
                    paramsToShare.setImageData(imageData);
                    //链接地址
                    paramsToShare.setUrl(Constants.SHARE_URL);
                }
                //微信朋友圈
                if ("WechatMoments".equals(platform.getName())) {
                    //分享网页
                    paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
                    //标题
                    paramsToShare.setTitle(getString(R.string.app_name));
                    paramsToShare.setText(getResources().getString(R.string.share_content));
                    //设置分享的图标
                    Bitmap imageData = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_miparking);
                    paramsToShare.setImageData(imageData);
                    //链接地址
                    paramsToShare.setUrl(Constants.SHARE_URL);

                }
                //新浪微博
                if ("SinaWeibo".equals(platform.getName())) {
                    //分享图文，微博要分享链接就把链接拼接在文本的后面，写在settext里面
                    paramsToShare.setText(getResources().getString(R.string.share_content) + Constants.SHARE_URL);
                    //paramsToShare.setImageUrl(Constants.SHARE_IMAGE_URL);
                }
                //短信
                if ("ShortMessage".equals(platform.getName())) {
                    //分享文本，暂时就分享个url，直接把url写在settext里了
                    paramsToShare.setText(Constants.SHARE_URL);
                }
            }
        });

        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Log.d("ShareSDK", "onComplete ---->  分享成功");
                HandlerUtil.sendMessage(Constants.SHARE_SUCCESS, mHandler);
                /*platform.isClientValid();*/
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Log.d("ShareSDK", "onError ---->  分享失败" + throwable.getStackTrace().toString());
                Log.d("ShareSDK", "onError ---->  分享失败" + throwable.getMessage());
                throwable.getMessage();
                throwable.printStackTrace();
                HandlerUtil.sendMessage(Constants.SHARE_ERROR, mHandler);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Log.d("ShareSDK", "onCancel ---->  分享取消");
                HandlerUtil.sendMessage(Constants.SHARE_CANCEL, mHandler);
            }
        });

        // 启动分享GUI
        oks.show(this);
    }

    /**
     * 系统消息
     */
    private void handleSystemMsg() {
        Intent systemmsgIntent = new Intent();
        systemmsgIntent.setClass(this, SystemmsgActivity.class);
        startActivity(systemmsgIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingBackRL:
                handleBack();
                break;

            case R.id.settingHeadIV:
                handleHead();
                break;

            //右上角的按钮，暂时屏蔽
            case R.id.settingRighttopRL:
                break;

            case R.id.settingLockmanagerLL:
                handleLockManager();
                break;

            case R.id.settingHelpcenterLL:
                handleHelpCenter();
                break;

            //分享给好友
            case R.id.settingShareLL:
                handleShare();
                break;

            case R.id.settingSystemmsgLL:
                handleSystemMsg();
                break;
        }
    }
}
