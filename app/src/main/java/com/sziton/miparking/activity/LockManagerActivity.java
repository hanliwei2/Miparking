package com.sziton.miparking.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.sziton.miparking.R;
import com.sziton.miparking.adapter.HasConnectedAdapter;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.BleUuids;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.bluetoothkit.SubpackageManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.custom.AddPopwindow;
import com.sziton.miparking.custom.PayPwdView;
import com.sziton.miparking.custom.PwdFragment;
import com.sziton.miparking.custom.PwdFragmentManager;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.encryption.EncryptManager;
import com.sziton.miparking.utils.ArrayMergeUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.HandlerUtil;
import com.sziton.miparking.utils.ToastUtil;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by fwj on 2017/11/9.
 */

public class LockManagerActivity extends FragmentActivity implements View.OnClickListener,AdapterView.OnItemClickListener,PayPwdView.InputCallBack{
    private RelativeLayout backRL;
    private TextView devicePairTV;
    private TextView scanPairTV;
    private BluetoothClient mClient;
    private BluetoothStateListener mBluetoothStateListener;
    private TextView addTV;
    private LinearLayout noLockLL;
    private ListView hasConnectedLV;
    private MySharedPreferences mySharedPreferences;
    private TreeMap<String,MyDeviceBean> hasConnectedTreeMap;
    private ArrayList<String> keysArrayList;
    private AddPopwindow addPopwindow;
    private Dialog loadingDialog;
    private int REQUEST_CODE=1;

    //以下开始是连接配对设备
    private PwdFragment pwdFragment;
    private byte[] sendData;
    private int times=0;
    private Handler sendHandler;//分包发送数据的计时器
    private Runnable sendRunnable;
    private int sendDelayed=20;//间隔20毫秒发送一包数据
    private int sendTimes=5;//5表示发送4次，第5次就关闭计时器
    private Handler receiveHandler;//接收数据的计时器
    private Runnable receiveRunnable;
    private int receiveDelayed=500;//满500毫秒就当接收数据完成
    private byte[] receiveData;//接收到的数据
    private String secretStr;//输入完成后的地锁密码，设备返回都是0，所以这边取
    //private int notifyTimes=0;//用于判断notify，只打开一次
    private String deviceAddress;
    private String deviceName;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("Miparing--SendData","what为："+msg.what);
            PwdFragmentManager.dismissPwdFragment(pwdFragment);
            switch (msg.what){
                case Constants.SEND_DATA_FAILD:
                    ToastUtil.shortToast(LockManagerActivity.this,LockManagerActivity.this.getResources().getString(R.string.lock_pair_failed));
                    break;

                case Constants.RECEIVE_DATA_SUCCESS:
                    byte[] dataArray= (byte[]) msg.obj;

                    String logData="";
                    for(int i=0;i<dataArray.length;i++){
                        int intData=dataArray[i]&0xFF;
                        logData=logData+intData+" ";
                    }
                    Log.i("Miparing--SendData","收到返回包为："+logData);

                    if(dataArray!=null&&dataArray.length==68){
                        byte[] statusArray= Arrays.copyOfRange(dataArray,62,68);
                        String statusStr=new String(statusArray);
                        Log.i("Miparing--SendData","收到返回statusStr为："+statusStr);
                        //接收的包里面60-68个字节拼接出是否成功,USUCCV表示成功，其他表示失败
                        if(statusStr.equals("USUCCV")){
                            ToastUtil.shortToast(LockManagerActivity.this,LockManagerActivity.this.getResources().getString(R.string.lock_pair_success));
                            //获取返回的密码，暂时返回的密码都是0
                            /*byte[] lockSecretArray= Arrays.copyOfRange(dataArray,56,62);
                            String lockSecretStr=new String(lockSecretArray);
                            Log.i("Miparing--SendData","收到返回lockSecretStr为："+lockSecretStr);*/
                            //配对成功，那就保存到本地
                            TreeMap<String,MyDeviceBean> deviceTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
                            if(deviceTreeMap==null){
                                deviceTreeMap=new TreeMap<>();
                            }
                            MyDeviceBean myDeviceBean=new MyDeviceBean();
                            myDeviceBean.setAddress(deviceAddress);
                            myDeviceBean.setSerect(secretStr);
                            deviceTreeMap.put(deviceName,myDeviceBean);
                            mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_SECRET,secretStr);
                            mySharedPreferences.setBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED,true);//保存已连接过的状态
                            mySharedPreferences.setTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP,deviceTreeMap);//保存这条设备

                            //配对成功，就调用Myaoolication中的方法添加设备监听
                            MyApplication.registerPairedDeviceListener(mClient,deviceName,deviceAddress,secretStr);

                            //刷新页面
                            refreshPage();
                        }else{
                            ToastUtil.shortToast(LockManagerActivity.this,LockManagerActivity.this.getResources().getString(R.string.lock_pair_failed));
                            mClient.disconnect(deviceAddress);
                        }
                    }else{
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(LockManagerActivity.this,LockManagerActivity.this.getResources().getString(R.string.lock_pair_failed));
                        mClient.disconnect(deviceAddress);
                    }
                    break;

                case Constants.RECEIVE_DATA_FAILED:
                    ToastUtil.shortToast(LockManagerActivity.this,LockManagerActivity.this.getResources().getString(R.string.lock_pair_failed));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockmanager);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.lockmanagerBackRL);
        addTV= (TextView) findViewById(R.id.lockManagerAddTV);
        devicePairTV= (TextView) findViewById(R.id.lockmanagerDevicePairTV);
        scanPairTV= (TextView) findViewById(R.id.lockmanagerScanPairTV);
        noLockLL= (LinearLayout) findViewById(R.id.lockManagerNoLockLL);
        hasConnectedLV= (ListView) findViewById(R.id.lockManagerHasConnectedLV);

        backRL.setOnClickListener(this);
        addTV.setOnClickListener(this);
        devicePairTV.setOnClickListener(this);
        scanPairTV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences= MySharedPreferences.getInstance(this);
        mClient= ClientManager.getClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //之前连接过，则显示设备列表
        refreshPage();
    }

    //用于打开时和扫码后刷新页面
    private void refreshPage(){
        if(mySharedPreferences.getBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED)){

            hasConnectedTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
            if(hasConnectedTreeMap!=null&&hasConnectedTreeMap.size()>0){
                keysArrayList=new ArrayList<String>(hasConnectedTreeMap.keySet());
                HasConnectedAdapter adapter=new HasConnectedAdapter(this,hasConnectedTreeMap,keysArrayList);
                hasConnectedLV.setAdapter(adapter);
                hasConnectedLV.setOnItemClickListener(this);
            }

            noLockLL.setVisibility(View.GONE);
            hasConnectedLV.setVisibility(View.VISIBLE);
            addTV.setVisibility(View.VISIBLE);
            //之前没连接过，则显示设备配对
        }else{
            noLockLL.setVisibility(View.VISIBLE);
            hasConnectedLV.setVisibility(View.GONE);
            addTV.setVisibility(View.GONE);
        }
    }

    /**
     * 添加
     */
    private void handleAdd(){
        addPopwindow=new AddPopwindow(LockManagerActivity.this,itemsOnclick);
        //显示窗口
        addPopwindow.showAtLocation(LockManagerActivity.this.findViewById(R.id.lockManagerLL), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
    }

    /**
     * 设备配对
     */
    private void handleDevicePair(){
        if(mBluetoothStateListener==null){
            //注册回调监听蓝牙状态
            mBluetoothStateListener = new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean openOrClosed) {
                    if(openOrClosed){
                        //蓝牙打开
                        ToastUtil.shortToast(LockManagerActivity.this,"蓝牙已打开");
                        Intent scanIntent=new Intent();
                        scanIntent.setClass(LockManagerActivity.this,LockListActivity.class);
                        startActivity(scanIntent);

                    }else{
                        //蓝牙关闭
                        ToastUtil.shortToast(LockManagerActivity.this,"蓝牙已关闭");
                    }
                }

            };
            mClient.registerBluetoothStateListener(mBluetoothStateListener);
        }

        //先判断是否支持ble
        if(mClient.isBleSupported()){
            if(!mClient.isBluetoothOpened()){
                //打开蓝牙有延迟，打开后在注册蓝牙监听里做逻辑处理
                mClient.openBluetooth();
            }else{
                Intent scanIntent=new Intent();
                scanIntent.setClass(this,LockListActivity.class);
                startActivity(scanIntent);
            }
        }else{
            ToastUtil.shortToast(this,"不支持蓝牙4.0！");
        }
    }

    /**
     * 扫码配对
     */
    private void handleScanPair(){
        //打开相机页面时，6.0以上判断相机权限，若没有获取，则进入获取页面，拒绝权限时，直接关闭
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    1);
        } else{
            //相机权限已开，就跳转到扫描页面
            Intent intent = new Intent(this, com.uuzuche.lib_zxing.activity.CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    /**
     * 根据用户选择是或否来打开相机权限或者直接关闭
     * @param requestCode
     * @param grantResults
     */
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                //相机权限已开，就跳转到扫描页面
                Intent scanIntent=new Intent();
                scanIntent.setClass(this, com.uuzuche.lib_zxing.activity.CaptureActivity.class);
                startActivity(scanIntent);
            } else {
                // Permission Denied
                //  displayFrameworkBugMessageAndExit();
                //ToastUtil.shortToast(this,"请在应用管理中打开“相机”访问权限！");
                ToastUtil.shortToast(this,"必须允许“相机”访问权限才能扫描配对！");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //判断相机权限
        doNext(requestCode,grantResults);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.lockmanagerBackRL:
                finish();
                break;

            case R.id.lockManagerAddTV:
                handleAdd();
                break;

            case R.id.lockmanagerDevicePairTV:
                handleDevicePair();
                break;

            case R.id.lockmanagerScanPairTV:
                handleScanPair();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String deviceName=keysArrayList.get(position);
        final String deviceAddress=hasConnectedTreeMap.get(deviceName).getAddress();
        final String secret=hasConnectedTreeMap.get(deviceName).getSerect();
        mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_MAC,deviceAddress);
        mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_SECRET,secret);
        //已连接，直接跳转到设备详细信息页
        if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            Log.i("Miparking-hasconnected","STATUS_DEVICE_CONNECTED");
            Intent detailsIntent=new Intent();
            detailsIntent.putExtra("deviceName",deviceName);
            detailsIntent.setClass(LockManagerActivity.this,LockDetailsActivity.class);
            startActivity(detailsIntent);
            //未连接，则去连接，并跳转到设备详细信息页
        }else{
            Log.i("Miparking-hasconnected","STATUS_DEVICE_DISCONNECTED");
            loadingDialog=DialogUtil.createLoadingDialog(this,LockManagerActivity.this.getResources().getString(R.string.connecting_dialog_text));
            mClient.connect(deviceAddress, MyApplication.getConnectOptions(), new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile data) {
                    if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                        ToastUtil.shortToast(LockManagerActivity.this,"连接成功！");
                        Intent detailsIntent=new Intent();
                        detailsIntent.putExtra("deviceName",deviceName);
                        detailsIntent.setClass(LockManagerActivity.this,LockDetailsActivity.class);
                        startActivity(detailsIntent);
                    }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                        ToastUtil.shortToast(LockManagerActivity.this,"连接失败！");
                    }
                    DialogUtil.closeDialog(loadingDialog);
                }
            });
        }

    }

    View.OnClickListener itemsOnclick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addPopwindow.dismiss();
            switch (v.getId()){
                case R.id.popwindowDevicePairTV:
                    handleDevicePair();
                    break;

                case R.id.popwindowScanPairTV:
                    handleScanPair();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    //ToastUtil.shortToast(LockManagerActivity.this,"解析结果为："+result);

                    //扫描到的就是设备名字
                    deviceName=result;
                    //判断是否已配对，若已配对，就不再添加，并给出提示
                    TreeMap<String,MyDeviceBean> deviceTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
                    if(deviceTreeMap!=null&&deviceTreeMap.size()>0){
                        for(String key:deviceTreeMap.keySet()){
                            if(deviceName.equals(key)){
                                ToastUtil.shortToast(LockManagerActivity.this,"请不要重复添加设备！");
                                return;
                            }
                        }
                    }
                    //扫描出一串字符串，例如：DS08DC2C26B06BDF,从第五位开始截取就是mac地址
                    String addressStr=deviceName.substring(4);
                    if(addressStr.length()!=12){
                        ToastUtil.shortToast(LockManagerActivity.this,"请扫描正确的设备类型！");
                        //finish();
                        return;
                    }
                    deviceAddress=addressStr.substring(0,2)+":"+addressStr.substring(2,4)+":"+addressStr.substring(4,6)+":"+addressStr.substring(6,8)+":"
                            +addressStr.substring(8,10)+":"+addressStr.substring(10,12);

                    //开始连接
                    loadingDialog= DialogUtil.createLoadingDialog(this,LockManagerActivity.this.getResources().getString(R.string.connecting_dialog_text));
                    mClient.connect(deviceAddress, MyApplication.getConnectOptions(), new BleConnectResponse() {
                        @Override
                        public void onResponse(int code, BleGattProfile data) {
                            if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                DialogUtil.closeDialog(loadingDialog);
                                mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_MAC,deviceAddress);
                                mySharedPreferences.setBooleanValue(Constants.IS_NEED_DISCONNECT,Constants.NEED_DISCONNECT);
                                pwdFragment= PwdFragmentManager.showPwdFragment(LockManagerActivity.this,LockManagerActivity.this,
                                        getResources().getString(R.string.lock_pair_title),getResources().getString(R.string.lock_pair_content));

                            }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                Log.i("Miparing--SendData","连接失败地址为："+deviceAddress);
                                ToastUtil.shortToast(LockManagerActivity.this,"连接失败！");
                                DialogUtil.closeDialog(loadingDialog);
                            }
                        }
                    });

                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    ToastUtil.shortToast(LockManagerActivity.this,"解析二维码失败");
                }
            }
        }
    }


    /**
     * 空方法，用于发送一个包成功后调用
     */
    private void sendDataSuccess(byte[] sendData){
        String logData="";
        for(int i=0;i<sendData.length;i++){
            int intData=sendData[i]&0xFF;
            logData=logData+intData+" ";
        }
        Log.i("Miparing--SendData","发送的数据为："+logData);
    }

    @Override
    public void onInputFinish(String result) {
        receiveData = new byte[0];
        secretStr=result;//输入完成后的密码赋值给全局变量，若配对成功，就把密码保存到本地
        //notifyTimes=notifyTimes+1;
        times=0;
        sendData= EncryptManager.pairEncrypt(result,result);
        sendHandler=new Handler();
        sendRunnable=new Runnable() {
            @Override
            public void run() {
                times=times+1;
                if(times<sendTimes){
                    sendHandler.postDelayed(sendRunnable,sendDelayed);
                    final byte[] subData= SubpackageManager.sendSubData(sendData,times);
                    mClient.write(deviceAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                sendDataSuccess(subData);
                            }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                Log.i("Miparing--SendData","发送数据第"+times+"包失败");
                                //有包发送失败，那就关掉密码框（扔到上层去处理ui）并断开连接，然后把发送失败传出去
                                mClient.disconnect(deviceAddress);
                                sendHandler.removeCallbacks(sendRunnable);
                                HandlerUtil.sendFailureMessage(Constants.SEND_DATA_FAILD,"发送数据第"+times+"包失败",mHandler);
                            }
                        }
                    });
                }else{
                    //只要发送4包，第五次就移除计时器
                    sendHandler.removeCallbacks(sendRunnable);
                    //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                    //打开并接收通知，里面有返回的数据

                    mClient.notify(deviceAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                        @Override
                        public void onNotify(UUID service, UUID character, byte[] value) {
                            //拼接包，共返回四个包，把每个包拼接起来
                            receiveData = ArrayMergeUtil.byteArrayMerge(receiveData, value);

                            //以下是为了查看log而打印出来，与逻辑无关
                            String logData = "";
                            for (int i = 0; i < value.length; i++) {
                                int intData = value[i] & 0xFF;
                                logData = logData + intData + " ";
                            }
                            Log.i("Miparing--SendData", "收到通知为：" + logData);
                        }

                        @Override
                        public void onResponse(int code) {
                            if (code == com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS) {
                                Log.i("Miparing--SendData", "打开通知成功！");

                            } else if (code == com.inuker.bluetooth.library.Constants.REQUEST_FAILED) {
                                Log.i("Miparing--SendData", "打开通知失败！");
                                mClient.disconnect(deviceAddress);
                                HandlerUtil.sendFailureMessage(Constants.RECEIVE_DATA_FAILED, "打开通知失败", mHandler);
                            }
                        }
                    });

                    //receiveData = new byte[0];
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    receiveHandler = new Handler();
                    receiveRunnable = new Runnable() {
                        @Override
                        public void run() {
                            HandlerUtil.sendMessage(Constants.RECEIVE_DATA_SUCCESS, receiveData, mHandler);
                        }
                    };
                    receiveHandler.postDelayed(receiveRunnable, receiveDelayed);
                }

            }
        };
        sendHandler.postDelayed(sendRunnable,sendDelayed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        mBluetoothStateListener=null;
    }
}
