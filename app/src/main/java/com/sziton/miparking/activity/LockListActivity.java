package com.sziton.miparking.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.sziton.miparking.R;
import com.sziton.miparking.adapter.LockListAdapter;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.BleUuids;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.bluetoothkit.SubpackageManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.custom.PayPwdView;
import com.sziton.miparking.custom.PwdFragment;
import com.sziton.miparking.custom.PwdFragmentManager;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.encryption.EncryptManager;
import com.sziton.miparking.utils.ArrayMergeUtil;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.HandlerUtil;
import com.sziton.miparking.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by fwj on 2017/11/24.
 */

public class LockListActivity extends FragmentActivity implements View.OnClickListener,AdapterView.OnItemClickListener,PayPwdView.InputCallBack{
    private RelativeLayout backRL;
    private ListView lockListLV;
    private TextView noDeviceTV;
    private BluetoothClient mClient;
    private MySharedPreferences mySharedPreferences;
    private SearchResult device;

    private ArrayList<SearchResult> lockArraylist;//搜索到的设备
    private TreeMap<String,MyDeviceBean> hasConnectedTreeMap;//本地保存着的之前连接过的
    private ArrayList<String> keysArrayList;
    private ArrayList<MyDeviceBean> valuesArraylist;
    private Dialog loadingDialog;
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

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("Miparing--SendData","what为："+msg.what);
            PwdFragmentManager.dismissPwdFragment(pwdFragment);
            switch (msg.what){
                case Constants.SEND_DATA_FAILD:
                    ToastUtil.shortToast(LockListActivity.this,LockListActivity.this.getResources().getString(R.string.lock_pair_failed));
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
                            ToastUtil.shortToast(LockListActivity.this,LockListActivity.this.getResources().getString(R.string.lock_pair_success));
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
                            myDeviceBean.setAddress(device.getAddress());
                            myDeviceBean.setSerect(secretStr);
                            deviceTreeMap.put(device.getName(),myDeviceBean);
                            mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_SECRET,secretStr);
                            mySharedPreferences.setBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED,true);//保存已连接过的状态
                            mySharedPreferences.setTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP,deviceTreeMap);//保存这条设备

                            //配对成功，就调用Myaoolication中的方法添加设备监听
                            MyApplication.registerPairedDeviceListener(mClient,device.getName(),device.getAddress(),secretStr);

                            LockListActivity.this.finish();
                        }else{
                            ToastUtil.shortToast(LockListActivity.this,LockListActivity.this.getResources().getString(R.string.lock_pair_failed));
                            mClient.disconnect(device.getAddress());
                        }
                    }else{
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(LockListActivity.this,LockListActivity.this.getResources().getString(R.string.lock_pair_failed));
                        mClient.disconnect(device.getAddress());
                    }
                    break;

                case Constants.RECEIVE_DATA_FAILED:
                    ToastUtil.shortToast(LockListActivity.this,LockListActivity.this.getResources().getString(R.string.lock_pair_failed));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locklist);
        initView();
        initData();
    }

    private void initView(){
        loadingDialog=DialogUtil.createLoadingDialog(this,getResources().getString(R.string.searching_dialog_text));
        backRL= (RelativeLayout) findViewById(R.id.lockListBackRL);
        lockListLV= (ListView) findViewById(R.id.lockListLV);
        noDeviceTV= (TextView) findViewById(R.id.locklistNoDeviceTV);

        backRL.setOnClickListener(this);
    }

    private void initData(){
        lockArraylist=new ArrayList<>();
        mySharedPreferences= MySharedPreferences.getInstance(this);
        mClient= ClientManager.getClient();
        //打开设备列表就搜索
        scanDevice();

        //setData(lockArrayList);
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.lockListBackRL:
                handleBack();
                break;

        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("Miparking----lockList","positon-->>"+String.valueOf(position)+"，status-->>"+mClient.getConnectStatus(lockArraylist.get(position).getAddress()));
        device=lockArraylist.get(position);
        if(mClient.getConnectStatus(device.getAddress())== com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            ToastUtil.shortToast(LockListActivity.this,"已连接，请勿重复连接！");
        }else if(mClient.getConnectStatus(device.getAddress())== com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTING){
            ToastUtil.shortToast(LockListActivity.this,"正在连接，请稍等！");
        }else{
        loadingDialog=DialogUtil.createLoadingDialog(this,LockListActivity.this.getResources().getString(R.string.connecting_dialog_text));
        mClient.connect(device.getAddress(), MyApplication.getConnectOptions(), new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                    DialogUtil.closeDialog(loadingDialog);
                    mySharedPreferences.setStringValue(Constants.CURRENT_DEVICE_MAC,device.getAddress());
                    mySharedPreferences.setBooleanValue(Constants.IS_NEED_DISCONNECT,Constants.NEED_DISCONNECT);
                    pwdFragment= PwdFragmentManager.showPwdFragment(LockListActivity.this,LockListActivity.this,
                            getResources().getString(R.string.lock_pair_title),getResources().getString(R.string.lock_pair_content));

                }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                    Log.i("Miparing--SendData","连接失败地址为："+device.getAddress());
                    ToastUtil.shortToast(LockListActivity.this,"连接失败！");
                    DialogUtil.closeDialog(loadingDialog);
                }
            }
        });
        }
    }

    //假数据
/*    public void setData(ArrayList<LockListBean> lockArrayList){
        for(int i=0;i<7;i++){
            LockListBean lockListBean=new LockListBean(getResources().getString(R.string.locklist_item_name));
            lockArrayList.add(lockListBean);
        }
    }*/

    /**
     * 搜索设备
     */
    private void scanDevice(){
        SearchRequest request = new SearchRequest.Builder()
                //.searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                //.searchBluetoothClassicDevice(3000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(3000)      // 再扫BLE设备2s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.i("Miparking----scanBle","SearchStarted");
                lockArraylist.clear();
                hasConnectedTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
                if(hasConnectedTreeMap!=null&&hasConnectedTreeMap.size()>0){
                    keysArrayList=new ArrayList<String>(hasConnectedTreeMap.keySet());
                    valuesArraylist=new ArrayList<MyDeviceBean>();
                    for(String deviceName:keysArrayList){
                        MyDeviceBean myDeviceBean=hasConnectedTreeMap.get(deviceName);
                        valuesArraylist.add(myDeviceBean);
                    }
                }
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Log.i("Miparking----scanBle","DeviceFounded-->>"+device.getName()+"---"+device.getAddress());
                //设备都是以DS08开头的，暂时通过这个来过滤设备，不然会搜索到全部BLE设备
                String nameStart=device.getName().substring(0,4);
                if(!lockArraylist.contains(device)&&nameStart.toUpperCase().equals("DS08")){
                    //添加进列表前，遍历保存在本地的设备列表，若本地已保存该设备，则不添加进搜索出的列表中
                    if(valuesArraylist!=null&&valuesArraylist.size()>0){
                        ArrayList<String> addressArraylist=new ArrayList<String>();
                        for(MyDeviceBean myDeviceBean:valuesArraylist){
                            addressArraylist.add(myDeviceBean.getAddress());
                        }
                        if(!addressArraylist.contains(device.getAddress())){
                            lockArraylist.add(device);
                        }
                    }else{
                        lockArraylist.add(device);
                    }

                }
            }

            @Override
            public void onSearchStopped() {
                Log.i("Miparking----scanBle","SearchStopped");
                DialogUtil.closeDialog(loadingDialog);
                if(lockArraylist!=null&&lockArraylist.size()>0){
                    LockListAdapter adapter=new LockListAdapter(LockListActivity.this,lockArraylist);
                    lockListLV.setAdapter(adapter);
                    lockListLV.setOnItemClickListener(LockListActivity.this);
                    //扫描到设备，转圈进度条隐藏，显示列表
                    lockListLV.setVisibility(View.VISIBLE);
                    noDeviceTV.setVisibility(View.GONE);
                }else{
                    //没扫描到设备，转圈进度条隐藏，显示没有搜索到设备
                    noDeviceTV.setVisibility(View.VISIBLE);
                    lockListLV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSearchCanceled() {
                Log.i("Miparking----scanBle","SearchCanceled");
                mClient.stopSearch();
                DialogUtil.closeDialog(loadingDialog);
            }
        });
    }

    //车锁密码输入完成后会弹出输入密码框，然后就向硬件发送配对命令
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
                    mClient.write(device.getAddress(), BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                sendDataSuccess(subData);
                            }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                Log.i("Miparing--SendData","发送数据第"+times+"包失败");
                                //有包发送失败，那就关掉密码框（扔到上层去处理ui）并断开连接，然后把发送失败传出去
                                mClient.disconnect(device.getAddress());
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

                        mClient.notify(device.getAddress(), BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
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
                                    mClient.disconnect(device.getAddress());
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
    public void onBackPressed() {
        super.onBackPressed();
        PwdFragmentManager.dismissAndDisconnect(pwdFragment);
        //ToastUtil.shortToast(this,getResources().getString(R.string.lock_pair_cancle));
    }
}
