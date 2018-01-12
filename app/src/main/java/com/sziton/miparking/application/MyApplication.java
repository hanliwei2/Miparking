package com.sziton.miparking.application;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.mob.MobSDK;
import com.sziton.miparking.R;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.ToastUtil;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by fwj on 2017/11/16.
 */

public class MyApplication extends Application{
    private String appKey="227f85c1794fb";//mob短信平台注册的appKey
    private String appSecret="b312d46ab968910bb24ae64440191c0c";//mob短信平台注册的appSecret
    //private String appKey="moba6b6c6d6";//demo里的
    //private String appSecret="b89d2427a3bc7ad1aea1e1e8c1d36bf3";//demo里的
    private static List<Activity> activityLists;
    private static MyApplication instance;
    private BluetoothClient mClient;
    private MySharedPreferences mySharedPreferences;
    public static TreeMap<String,MyDeviceBean> myConnectedDevices;
    private ArrayList<String> myDevicesAddress;
    private  String deviceName;
    private  String deviceAddress;
    private String deviceSecret;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initView();
        initData();

    }

    private void initView(){

    }

    private void initData(){
        instance = this;
        activityLists=new ArrayList<>();
        mySharedPreferences=MySharedPreferences.getInstance(this);
        mClient= ClientManager.getClient();
        // mob短信和分享通过代码注册你的AppKey和AppSecret
        MobSDK.init(this, appKey, appSecret);

        ZXingLibrary.initDisplayOpinion(this);

        //注册蓝牙打开关闭的监听
        mClient.registerBluetoothStateListener(mBluetoothStateListener);

        //注册本地所有配对设备的状态监听
        registerMyDevicesListener();


        //打开app，若蓝牙已开启，就去连接已配对过的设备，连接上的就保存到application级别的列表中
        if(mClient.isBluetoothOpened()){
            connectMyDevices();
        }else{
            ToastUtil.shortToast(MyApplication.getInstance(),getResources().getString(R.string.app_bluetooth_notopen));
        }


        /*//创建一个BluetoothClient，建议作为一个全局单例，管理所有BLE设备的连接。
        mClient= ClientManager.getClient();
        bleManager=BleManager.getInstance(MyApplication.getInstance(),mClient);
        //注册监听蓝牙状态,打开或关闭
        bleManager.registerBluetoothStateListener();*/
    }

    //写一个基类Activity，在构造构造方法调用MyApplication.addActivity(this);就可以实现。
    public static void addActivity(Activity activity){
        activityLists.add(activity);
    }

    //统一finish掉所有的activity
    public static void clearActivity(){
        if(activityLists!=null){
            for(Activity activity:activityLists){
                if(!activity.isFinishing()){
                    activity.finish();
                }
            }
            activityLists.clear();
        }
    }

    /**
     * 创建连接配置，其他涉及连接的地方都用这里的
     */
    public static BleConnectOptions getConnectOptions(){
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(5000)   // 连接超时10s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(5000)  // 发现服务超时20s
                .build();
        return options;
    }

    /**
     * 打开app就连接本地已存储的设备
     */
    private void connectMyDevices(){
        if(mySharedPreferences.getBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED)){
            TreeMap<String,MyDeviceBean> myDevices=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
            if(myDevices!=null&&myDevices.size()>0){
                ArrayList<String> myDevicesName=new ArrayList<>();
                //遍历获取devicenames
                for(String key:myDevices.keySet()){
                    myDevicesName.add(key);
                }
                //取出每一个mac地址
                for(int i=0;i<myDevicesName.size();i++){
                    final String deviceName=myDevicesName.get(i);
                    final String deviceAddress=myDevices.get(deviceName).getAddress();

                    BleConnectOptions options = new BleConnectOptions.Builder()
                            .setConnectRetry(3)   // 连接如果失败重试3次
                            .setConnectTimeout(5000)   // 连接超时10s
                            .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                            .setServiceDiscoverTimeout(5000)  // 发现服务超时20s
                            .build();
                    mClient.connect(deviceAddress, options,new BleConnectResponse() {
                        @Override
                        public void onResponse(int code, BleGattProfile data) {
                            if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                Log.i("Miparking--application","连接上的mac地址为："+deviceAddress);
                            }else{
                                Log.i("Miparking--application","连接失败的mac地址为："+deviceAddress);
                            }
                        }
                    });
                }
            }

        }
    }

    /**
     * 监听蓝牙打开或者关闭
     * 蓝牙打开或关闭需要一段时间，可以注册回调监听状态，回调的参数如果是true表示蓝牙已打开，false表示蓝牙关闭
     */
    private final BluetoothStateListener mBluetoothStateListener=new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            if(openOrClosed){
                //蓝牙打开，就去连接本地已存储的设备
                connectMyDevices();
            }else{
                //蓝牙断开，就把本地已存储的设备列表清除掉
                ToastUtil.shortToast(MyApplication.getInstance(),getResources().getString(R.string.app_bluetooth_isclosed));
                /*if(myConnectedDevices!=null){
                    myConnectedDevices.clear();
                    myConnectedDevices=null;
                }*/
            }
        }
    };

    /**
     * 本地已配对过的所有设备再连接前都会绑定监听
     * 监听设备的连接状态，只有两个状态：连接和断开。
     */
    public void registerMyDevicesListener(){
        if(mySharedPreferences.getBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED)){
            myDevicesAddress=new ArrayList<>();
            TreeMap<String,MyDeviceBean> myDevices=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
            if(myDevices!=null&&myDevices.size()>0) {
                ArrayList<String> myDevicesName = new ArrayList<>();
                //遍历获取devicenames
                for (String key : myDevices.keySet()) {
                    myDevicesName.add(key);
                }
                //取出每一个mac地址
                for (int i = 0; i < myDevicesName.size(); i++) {
                    deviceName = myDevicesName.get(i);
                    deviceAddress = myDevices.get(deviceName).getAddress();
                    deviceSecret = myDevices.get(deviceName).getSerect();

                    //每一个已配对设备都注册状态监听
                    mClient.registerConnectStatusListener(deviceAddress, bleConnectStatusListener);
                    myDevicesAddress.add(deviceAddress);
                }
            }
        }

    }

    //设备状态监听
    BleConnectStatusListener bleConnectStatusListener=new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == com.inuker.bluetooth.library.Constants.STATUS_CONNECTED) {
                MyDeviceBean myDeviceBean=new MyDeviceBean();
                myDeviceBean.setAddress(deviceAddress);
                myDeviceBean.setSerect(deviceSecret);
                if(myConnectedDevices==null){
                    myConnectedDevices=new TreeMap<>();
                }
                myConnectedDevices.put(deviceName,myDeviceBean);
            }else if (status == com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED) {
                if(myConnectedDevices!=null&&myConnectedDevices.size()>0){
                    for(String key:myConnectedDevices.keySet()){
                        if(myConnectedDevices.get(key).getAddress().equals(mac)){
                            myConnectedDevices.remove(key);
                        }
                    }
                }
            }
        }
    };

    /**
     * 反注册本地已配对过的所有设备连接状态
     */
    public void unregisterMyDevicesListener(){
        if(myDevicesAddress!=null&&myDevicesAddress.size()>0){
            for(int i=0;i<myDevicesAddress.size();i++){
                mClient.unregisterConnectStatusListener(myDevicesAddress.get(i),bleConnectStatusListener);
            }
        }

    }

    /**
     * 供配对页面使用，配对成功，就给当前设备添加状态监听
     */
    public static void registerPairedDeviceListener(BluetoothClient mClient,final String deviceName,final String deviceAddress,final String deviceSecret){
        if(MyApplication.myConnectedDevices==null){
            MyApplication.myConnectedDevices=new TreeMap<>();
        }
        MyDeviceBean myDeviceBean=new MyDeviceBean();
        myDeviceBean.setAddress(deviceAddress);
        myDeviceBean.setSerect(deviceSecret);
        MyApplication.myConnectedDevices.put(deviceName,myDeviceBean);

        mClient.registerConnectStatusListener(deviceAddress, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {
                if (status == com.inuker.bluetooth.library.Constants.STATUS_CONNECTED) {
                    MyDeviceBean myDeviceBean=new MyDeviceBean();
                    myDeviceBean.setAddress(deviceAddress);
                    myDeviceBean.setSerect(deviceSecret);
                    MyApplication.myConnectedDevices.put(deviceName,myDeviceBean);
                }else if (status == com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED) {
                    if(MyApplication.myConnectedDevices!=null&&MyApplication.myConnectedDevices.size()>0){
                        for(String key:MyApplication.myConnectedDevices.keySet()){
                            if(MyApplication.myConnectedDevices.get(key).getAddress().equals(mac)){
                                MyApplication.myConnectedDevices.remove(key);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 供详情页删除设备用
     * @param deviceName
     */
    public static void whenDeleteDevice(String deviceName){
        if(MyApplication.myConnectedDevices!=null&&MyApplication.myConnectedDevices.size()>0){
            for(String key:MyApplication.myConnectedDevices.keySet()){
                if(key.equals(deviceName)){
                    MyApplication.myConnectedDevices.remove(deviceName);
                }
            }
            //mClient.unregisterConnectStatusListener(MyApplication.myConnectedDevices.get(deviceName), mBleConnectStatusListener);
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        //反注册监听蓝牙状态
        mClient.unregisterBluetoothStateListener(mBluetoothStateListener);

        //反注册监听设备连接状态
        unregisterMyDevicesListener();
    }
}
