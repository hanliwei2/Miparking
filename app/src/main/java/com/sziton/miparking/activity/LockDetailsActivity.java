package com.sziton.miparking.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.sziton.miparking.R;
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

import java.util.Arrays;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by fwj on 2017/11/24.
 */

public class LockDetailsActivity extends FragmentActivity implements View.OnClickListener,PayPwdView.InputCallBack{
    private RelativeLayout backRL;
    private TextView connectStatusTV;
    private TextView batteryTV;
    private TextView deleteTV;
    private TextView commentStatusTV;
    private LinearLayout queryStatusLL;
    private LinearLayout changePasswordLL;
    private LinearLayout closeLockLL;
    private LinearLayout openLockLL;
    private TextView titleTV;
    private BluetoothClient mClient;
    private MySharedPreferences mySharedPreferences;
    private String deviceAddress;

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
    private Dialog loadingDialog;
    private Dialog openDialog;//开锁时转圈的框框
    private Dialog closeDialog;//关锁时转圈的框框
    private String lockAddress;
    private String deviceName;//设备名字，这是treemap中的key
    private boolean isNotified;
    private boolean isNeedToast;//判断是否是打开页面的查询，true表示需要弹出查询成功，false表示不需要弹出
    private PwdFragment pwdFragment;
    private String oldPwd;//旧密码
    private String newPwd;//新密码
    private String newPwdAgain;//再次输入新密码
    private int showPwdTimes=0;
    private String lockStatus="";//开关锁前查询到的锁的状态
    private int queryDelayed=2000;//2秒查询一次升降状态
    private int dismissDelayed=10000;//10秒后关闭转圈框
    private boolean isLockOpened=false;
    private boolean isOpeningClosed=false;//用与开锁时给上升中和下降中判断，不然如果一直返回上升中或下降中，会成死循环，现在当dialog10秒后消失的时候，就设置成true，就不再执行了
    private boolean isLockClosed=false;
    private boolean isClosingClosed=false;//用与关锁时给上升中和下降中判断



    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("Miparing--SendData","what为："+msg.what);
            DialogUtil.closeDialog(loadingDialog);
            switch (msg.what){
                /**
                 * 查询状态
                 */
                case Constants.QUERY_SEND_DATA_FAILD:
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lock_query_failed));
                    break;

                case Constants.QUERY_RECEIVE_DATA_SUCCESS:
                    byte[] dataArray= (byte[]) msg.obj;

                    String logData="";
                    for(int i=0;i<dataArray.length;i++){
                        int intData=dataArray[i]&0xFF;
                        logData=logData+intData+" ";
                    }
                    Log.i("Miparing--SendData","收到返回包为："+logData);

                    if(dataArray!=null&&dataArray.length==10){
                        byte[] requestArray= Arrays.copyOfRange(dataArray,4,8);
                        String requestStr=new String(requestArray);//请求是否失败，SUCC表示成功

                        if(requestStr.equals("SUCC")){
                            //查询成功
                            //设置连接状态
                            String connectStatus;
                            if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
                                connectStatus=getResources().getString(R.string.lockdetails_connectstatus_connected);
                            }else{
                                connectStatus=getResources().getString(R.string.lockdetails_connectstatus_notconnect);
                            }
                            //设置连接状态
                            connectStatusTV.setText(connectStatus);

                            byte statusByte= dataArray[2];
                            int statusInt=statusByte;//锁的状态，第3位是锁的状态，2开启，1关闭，0异常，3上升，4下降,比较的时候按照十进制比较
                            Log.i("Miparing--SendData","锁的状态为："+statusInt);
                            byte batteryByte=dataArray[3];
                            int batteryInt=batteryByte;//电量，第四位是电量，1高，3中，5低，9没电
                            Log.i("Miparing--SendData","电量为："+batteryInt);
                            String batteryStr = "";
                            String statusStr="";
                            //电量
                            switch(batteryInt){
                                case 49:
                                    batteryStr=getResources().getString(R.string.lockdetails_battery_high);
                                    break;
                                case 51:
                                    batteryStr=getResources().getString(R.string.lockdetails_battery_middle);
                                    break;
                                case 53:
                                    batteryStr=getResources().getString(R.string.lockdetails_battery_low);
                                    break;
                                case 57:
                                    batteryStr=getResources().getString(R.string.lockdetails_battery_no);
                                    break;
                                default:
                                    batteryStr=getResources().getString(R.string.lockdetails_battery_high);
                                    break;
                            }
                            //锁的状态
                            switch(statusInt){
                                case 48:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_error);
                                    break;
                                case 49:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_close);
                                    break;
                                case 50:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_open);
                                    break;
                                case 51:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_rising);
                                    break;
                                case 52:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_droping);
                                    break;
                                default:
                                    statusStr=getResources().getString(R.string.lockdetails_commentstatus_close);
                                    break;
                            }
                            batteryTV.setText(batteryStr);//设置电量
                            commentStatusTV.setText(statusStr);//设置锁的状态
                            if(isNeedToast){
                                ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lock_query_success));
                            }
                        }else{
                            //查询失败
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lock_query_failed));
                        }

                    }else{
                        //接收的包不全，认定为查询失败
                        ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lock_query_failed));
                    }
                    break;

                case Constants.QUERY_RECEIVE_DATA_FAILED:
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lock_query_failed));
                    break;

                //地锁未连接
                case Constants.LOCK_NOT_CONNECT:
                    ToastUtil.shortToast(LockDetailsActivity.this,getResources().getString(R.string.lockdetails_notconnect));
                    connectStatusTV.setText(getResources().getString(R.string.lockdetails_connectstatus_notconnect));
                    batteryTV.setText("");
                    commentStatusTV.setText("");
                    break;

                /**
                 * 修改密码
                 */
                case Constants.CHANGE_SEND_DATA_FAILD:
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_change_failed));
                    break;

                case Constants.CHANGE_RECEIVE_DATA_FAILED:
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_change_failed));
                    break;

                case Constants.CHANGE_RECEIVE_DATA_SUCCESS:
                    byte[] changeArray= (byte[]) msg.obj;

                    String logData1="";
                    for(int i=0;i<changeArray.length;i++){
                        int intData=changeArray[i]&0xFF;
                        logData=logData1+intData+" ";
                    }
                    Log.i("Miparing--SendData","修改密码收到返回包为："+logData1);

                    if(changeArray!=null&&changeArray.length==68){
                        byte[] statusArray= Arrays.copyOfRange(changeArray,62,68);
                        String statusStr=new String(statusArray);
                        Log.i("Miparing--SendData","修改密码收到返回statusStr为："+statusStr);
                        //接收的包里面60-68个字节拼接出是否成功,USUCCV表示成功，其他表示失败
                        if(statusStr.equals("USUCCV")){
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_change_success));
                            //修改完密码，取出本地保存的，把这个名字的移除掉，然后设置新密码重新添加进去
                            TreeMap<String,MyDeviceBean> deviceTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
                            deviceTreeMap.get(deviceName).setSerect(newPwd);
                            /*MyDeviceBean myDeviceBean=new MyDeviceBean();
                            myDeviceBean.setAddress(lockAddress);
                            myDeviceBean.setSerect(secretStr);
                            deviceTreeMap.put(device.getName(),myDeviceBean);*/
                            mySharedPreferences.setTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP,deviceTreeMap);//保存这条设备

                        }else{
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_change_failed));
                        }
                    }else{
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_change_failed));
                    }
                    break;


                /**
                 * 开锁
                 */
                //锁的状态
                case Constants.OPEN_STATUS_ISOPENED://已在开启状态
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_isopened));
                    DialogUtil.closeDialog(openDialog);
                    break;
                case Constants.OPEN_STATUS_ERROR://异常
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_error));
                    DialogUtil.closeDialog(openDialog);
                    //弹出确认框
                    new AlertDialog.Builder(LockDetailsActivity.this).setTitle("锁状态异常，继续开锁吗？")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的操作
                                    openOrCloseLock(Constants.OPEN_LOCK);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    dialog.dismiss();
                                }
                            }).show();
                    break;
                case Constants.OPEN_STATUS_RISING://上升中
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_rising));
                    if(!isOpeningClosed){
                        showOpenDialog(getResources().getString(R.string.lockdetails_open_isrising));
                        Handler openRiseHandler=new Handler();
                        Runnable openRiseRunnable=new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.OPEN_LOCK);
                            }
                        };
                        openRiseHandler.postDelayed(openRiseRunnable,queryDelayed);
                    }

                    break;
                case Constants.OPEN_STATUS_DROPING://下降中
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_droping));
                    if(!isOpeningClosed) {
                        showOpenDialog(getResources().getString(R.string.lockdetails_open_isdroping));
                        Handler openDropHandler = new Handler();
                        Runnable openDropRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.OPEN_LOCK);
                            }
                        };
                        openDropHandler.postDelayed(openDropRunnable, queryDelayed);
                    }
                    break;
                case Constants.OPEN_STATUS_ISCLOSED://关闭状态
                    DialogUtil.closeDialog(openDialog);
                    if(!isLockOpened){
                        //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_isclosed));
                        openOrCloseLock(Constants.OPEN_LOCK);
                    }
                    break;
                //开锁是否成功
                case Constants.OPEN_SEND_DATA_FAILD://发送数据失败
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    break;
                case Constants.OPEN_RECEIVE_DATA_FAILED://开启 通知失败
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    break;
                case Constants.OPEN_RECEIVE_DATA_SUCCESS://开锁成功
                    byte[] openArray= (byte[]) msg.obj;

                    String logData2="";
                    for(int i=0;i<openArray.length;i++){
                        int intData=openArray[i]&0xFF;
                        logData2=logData2+intData+" ";
                    }
                    Log.i("Miparing--SendData","开锁收到返回包为："+logData2);

                    //文档上写的收到数据长度是74，但是实际收到的长度是84
                    if(openArray!=null&&openArray.length==84){
                        byte[] statusArray= Arrays.copyOfRange(openArray,0,8);
                        String statusStr=new String(statusArray);
                        Log.i("Miparing--SendData","开锁收到返回statusStr为："+statusStr);
                        //接收的包里面0-8个字节拼接出是否成功,SUCCSUCC表示成功，其他表示失败
                        if(statusStr.equals("SUCCSUCC")){
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_success));
                            //开锁成功，弹出转圈框，继续查询状态，直到开锁完成
                            isLockOpened=true;
                            showOpenDialog(getResources().getString(R.string.lockdetails_open_isdroping));

                            Handler openQueryHandler=new Handler();
                            Runnable openQueryRunnable=new Runnable() {
                                @Override
                                public void run() {
                                    queryLock(Constants.OPEN_LOCK);
                                }
                            };
                            openQueryHandler.postDelayed(openQueryRunnable,queryDelayed);

                        }else{
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                        }
                    }else{
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    }
                    break;

                /**
                 * 关锁
                 */
                //锁的状态
                case Constants.CLOSE_STATUS_ISOPENED://锁是开启状态
                    DialogUtil.closeDialog(closeDialog);
                    if(!isLockClosed){
                        //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_isopened));
                        openOrCloseLock(Constants.CLOSE_LOCK);
                    }
                    break;
                case Constants.CLOSE_STATUS_ERROR://异常
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_error));
                    DialogUtil.closeDialog(closeDialog);
                    //弹出确认框
                    new AlertDialog.Builder(LockDetailsActivity.this).setTitle("锁状态异常，继续关锁吗？")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的操作
                                    openOrCloseLock(Constants.CLOSE_LOCK);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    dialog.dismiss();
                                }
                            }).show();
                    break;
                case Constants.CLOSE_STATUS_RISING://上升中
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_rising));
                    if(!isClosingClosed){
                        showCloseDialog(getResources().getString(R.string.lockdetails_close_isrising));
                        Handler closeRiseHandler=new Handler();
                        Runnable closeRiseRunnable=new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.CLOSE_LOCK);
                            }
                        };
                        closeRiseHandler.postDelayed(closeRiseRunnable,queryDelayed);
                    }
                    break;
                case Constants.CLOSE_STATUS_DROPING://下降中
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_droping));
                    if(!isClosingClosed) {
                        showCloseDialog(getResources().getString(R.string.lockdetails_close_isdroping));
                        Handler closeDropHandler = new Handler();
                        Runnable closeDropRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.CLOSE_LOCK);
                            }
                        };
                        closeDropHandler.postDelayed(closeDropRunnable, queryDelayed);
                    }
                    break;
                case Constants.CLOSE_STATUS_ISCLOSED://关闭状态
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_isclosed));
                    DialogUtil.closeDialog(closeDialog);
                    break;
                //关锁是否成功
                case Constants.CLOSE_SEND_DATA_FAILD://发送数据失败
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    break;
                case Constants.CLOSE_RECEIVE_DATA_FAILED://开启 通知失败
                    ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    break;
                case Constants.CLOSE_RECEIVE_DATA_SUCCESS://开锁成功
                    byte[] closeArray= (byte[]) msg.obj;

                    String logData3="";
                    for(int i=0;i<closeArray.length;i++){
                        int intData=closeArray[i]&0xFF;
                        logData3=logData3+intData+" ";
                    }
                    Log.i("Miparing--SendData","开锁收到返回包为："+logData3);

                    //文档上写的收到数据长度是74，但是实际收到的长度是84
                    if(closeArray!=null&&closeArray.length==84){
                        byte[] statusArray= Arrays.copyOfRange(closeArray,0,8);
                        String statusStr=new String(statusArray);
                        Log.i("Miparing--SendData","开锁收到返回statusStr为："+statusStr);
                        //接收的包里面0-8个字节拼接出是否成功,SUCCSUCC表示成功，其他表示失败
                        if(statusStr.equals("SUCCSUCC")){
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_success));
                            //关锁成功，弹出转圈框，继续查询状态，直到关锁完成
                            isLockClosed=true;
                            showCloseDialog(getResources().getString(R.string.lockdetails_close_isrising));

                            Handler closeQueryHandler=new Handler();
                            Runnable closeQueryRunnable=new Runnable() {
                                @Override
                                public void run() {
                                    queryLock(Constants.CLOSE_LOCK);
                                }
                            };
                            closeQueryHandler.postDelayed(closeQueryRunnable,queryDelayed);

                        }else{
                            ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                        }
                    }else{
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdetails);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.lockDetailsBackRL);
        connectStatusTV= (TextView) findViewById(R.id.lockDetailsConnectStatusTV);
        batteryTV= (TextView) findViewById(R.id.lockDetailsBatteryTV);
        deleteTV= (TextView) findViewById(R.id.lockDetailsDeleteTV);
        commentStatusTV= (TextView) findViewById(R.id.lockDetailsCommentStatusTV);
        queryStatusLL= (LinearLayout) findViewById(R.id.lockdetailsQueryStatusLL);
        changePasswordLL= (LinearLayout) findViewById(R.id.lockdetailsChangePasswordLL);
        closeLockLL= (LinearLayout) findViewById(R.id.lockdetailsCloseLockLL);
        openLockLL= (LinearLayout) findViewById(R.id.lockdetailsOpenLockLL);

        titleTV= (TextView) findViewById(R.id.lockDetailsTitleTV);

        backRL.setOnClickListener(this);
        deleteTV.setOnClickListener(this);
        queryStatusLL.setOnClickListener(this);
        changePasswordLL.setOnClickListener(this);
        closeLockLL.setOnClickListener(this);
        openLockLL.setOnClickListener(this);

    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
        mClient= ClientManager.getClient();
        Intent intent=getIntent();
        deviceName=intent.getStringExtra("deviceName");
        deviceAddress=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
        //String connectStatus;
        //设置标题
        titleTV.setText(deviceName);

        handleQueryStatus(false);
        /*if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            connectStatus=getResources().getString(R.string.lockdetails_connectstatus_connected);
        }else{
            connectStatus=getResources().getString(R.string.lockdetails_connectstatus_notconnect);
        }
        //设置连接状态
        connectStatusTV.setText(connectStatus);*/
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 删除
     */
    private void handleDelete(){
        //弹出确认框
        new AlertDialog.Builder(LockDetailsActivity.this).setTitle("确定删除此设备吗？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        deleteDevice();
                        MyApplication.whenDeleteDevice(deviceName);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                        dialog.dismiss();
                    }
                }).show();
    }

    //删除设备的方法
    private void deleteDevice(){
        if(mClient.getConnectStatus(lockAddress)== com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            mClient.disconnect(lockAddress);
        }
        TreeMap<String,MyDeviceBean> hasConnectedTreeMap=mySharedPreferences.getTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP);
        hasConnectedTreeMap.remove(deviceName);
        mySharedPreferences.setTreeMap(Constants.HASCONNECTED_LOCK_TREEMAP,hasConnectedTreeMap);
        if(hasConnectedTreeMap.size()==0){
            mySharedPreferences.setBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED,false);
        }
        finish();
    }

    /**
     * 查询状态
     * needToast 表示是否需要弹出查询成功的弹框，true表示弹出，false表示不弹出
     */
    private void handleQueryStatus(boolean needToast){
        isNeedToast=needToast;
        loadingDialog= DialogUtil.createLoadingDialog(this,getResources().getString(R.string.loading_dialog_text));
        //连接状态下
        if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            receiveData=new byte[0];
            String lockSecret=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_SECRET);
            lockAddress=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
            times=0;
            sendData= EncryptManager.inquireLockStatusEncrypt(lockSecret,lockSecret);
            sendHandler=new Handler();
            sendRunnable=new Runnable() {
                @Override
                public void run() {
                    times=times+1;
                    if(times<sendTimes){
                        sendHandler.postDelayed(sendRunnable,sendDelayed);
                        final byte[] subData= SubpackageManager.sendSubData(sendData,times);
                        mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                    sendDataSuccess(subData);
                                }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                    Log.i("Miparing--SendData","发送数据第"+times+"包失败");
                                    sendHandler.removeCallbacks(sendRunnable);
                                    HandlerUtil.sendFailureMessage(Constants.QUERY_SEND_DATA_FAILD,"发送数据第"+times+"包失败",mHandler);
                                }
                            }
                        });
                    }else{
                        //只要发送4包，第五次就移除计时器
                        sendHandler.removeCallbacks(sendRunnable);
                        //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                        //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                        //打开并接收通知，里面有返回的数据,只有第一次的时候才开启notify，不然会有很多
                        if(!isNotified){

                        mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                            @Override
                            public void onNotify(UUID service, UUID character, byte[] value) {
                                //拼接包，共返回四个包，把每个包拼接起来
                                receiveData= ArrayMergeUtil.byteArrayMerge(receiveData,value);


                                //以下是为了查看log而打印出来，与逻辑无关
                                String logData="";
                                for(int i=0;i<value.length;i++){
                                    int intData=value[i]&0xFF;
                                    logData=logData+intData+" ";
                                }
                                Log.i("Miparing--SendData","收到通知为："+logData);
                            }

                            @Override
                            public void onResponse(int code) {
                                if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                    Log.i("Miparing--SendData","打开通知成功！");
                                    isNotified=true;
                                }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                    Log.i("Miparing--SendData","打开通知失败！");
                                    isNotified=false;
                                    HandlerUtil.sendFailureMessage(Constants.QUERY_RECEIVE_DATA_FAILED,"打开通知失败",mHandler);
                                }
                            }
                        });

                        }
                        //receiveData=new byte[0];
                        //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                        receiveHandler=new Handler();
                        receiveRunnable=new Runnable() {
                            @Override
                            public void run() {
                                HandlerUtil.sendMessage(Constants.QUERY_RECEIVE_DATA_SUCCESS,receiveData,mHandler);
                            }
                        };
                        receiveHandler.postDelayed(receiveRunnable,receiveDelayed);
                    }

                }
            };
            sendHandler.postDelayed(sendRunnable,sendDelayed);
        }else{
            //设备未连接
            HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT,"地锁未连接",mHandler);
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

    /**
     * 修改密码
     */
    private void handleChangePassword(){
        //连接状态下
        if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            //弹出输入旧密码框
            showPwdTimes=0;
            mySharedPreferences.setBooleanValue(Constants.IS_NEED_DISCONNECT,Constants.NOT_DISCONNECT);//表示按x时不需要断开设备
            pwdFragment= PwdFragmentManager.showPwdFragment(LockDetailsActivity.this,this,
                    getResources().getString(R.string.lockdetails_changepwd_old_title),getResources().getString(R.string.lockdetails_changepwd_old_content));
        }else{
            //设备未连接
            HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT,"地锁未连接",mHandler);
        }

    }

    /**
     * 关锁
     */
    private void handleCloseLock(){
        //需要加一个确认框，告知用户确认车是否还在车位上
        //弹出确认框
        new AlertDialog.Builder(LockDetailsActivity.this).setTitle("请检查汽车是否还在车位上")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        doClose();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                        dialog.dismiss();
                    }
                }).show();

    }

    //点击关锁按钮后的操作，单独封装出来，因为handleCloseLock里面要放确认框，而直接把这个方法放到确认按钮里
    private void doClose(){
        loadingDialog= DialogUtil.createLoadingDialog(LockDetailsActivity.this,getResources().getString(R.string.loading_dialog_text));
        //连接状态下
        if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            isLockClosed=false;
            isClosingClosed=false;
            queryLock(Constants.CLOSE_LOCK);

        }else{
            //设备未连接
            HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT,"地锁未连接",mHandler);
        }
    }

    /**
     * 开锁
     */
    private void handleOpenLock(){
        loadingDialog= DialogUtil.createLoadingDialog(this,getResources().getString(R.string.loading_dialog_text));
        //连接状态下
        if(mClient.getConnectStatus(deviceAddress)==com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED){
            //点击开锁，给个false，表示未开，开锁成功后，赋值成true，表示已开，防止重复开锁
            isLockOpened=false;
            isOpeningClosed=false;
            queryLock(Constants.OPEN_LOCK);

        }else{
            //设备未连接
            HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT,"地锁未连接",mHandler);
        }
    }

    //开关锁前的查询动作，这个不是查询状态时调用的方法,
    //因为开锁和关锁都要用这个方法查询状态，所以isOpen用于判断是开锁还是关锁，true表示开锁，false表示关锁
    private void queryLock(final boolean isOpen){
            receiveData=new byte[0];
            String lockSecret=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_SECRET);
            lockAddress=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
            times=0;
            sendData= EncryptManager.inquireLockStatusEncrypt(lockSecret,lockSecret);
            sendHandler=new Handler();
            sendRunnable=new Runnable() {
                @Override
                public void run() {
                    times=times+1;
                    if(times<sendTimes){
                        sendHandler.postDelayed(sendRunnable,sendDelayed);
                        final byte[] subData= SubpackageManager.sendSubData(sendData,times);
                        mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                    sendDataSuccess(subData);
                                }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                    Log.i("Miparing--SendData","开锁前查询，发送数据第"+times+"包失败");
                                    sendHandler.removeCallbacks(sendRunnable);
                                    if(isOpen){
                                        HandlerUtil.sendFailureMessage(Constants.OPEN_STATUS_ERROR,getResources().getString(R.string.lockdetails_open_error),mHandler);
                                    }else{
                                        HandlerUtil.sendFailureMessage(Constants.CLOSE_STATUS_ERROR,getResources().getString(R.string.lockdetails_close_error),mHandler);
                                    }
                                }
                            }
                        });
                    }else{
                        //只要发送4包，第五次就移除计时器
                        sendHandler.removeCallbacks(sendRunnable);
                        //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                        //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                        //打开并接收通知，里面有返回的数据,只有第一次的时候才开启notify，不然会有很多
                        if(!isNotified){

                            mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                                @Override
                                public void onNotify(UUID service, UUID character, byte[] value) {
                                    //拼接包，共返回四个包，把每个包拼接起来
                                    receiveData= ArrayMergeUtil.byteArrayMerge(receiveData,value);


                                    //以下是为了查看log而打印出来，与逻辑无关
                                    String logData="";
                                    for(int i=0;i<value.length;i++){
                                        int intData=value[i]&0xFF;
                                        logData=logData+intData+" ";
                                    }
                                    Log.i("Miparing--SendData","收到通知为："+logData);
                                }

                                @Override
                                public void onResponse(int code) {
                                    if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                        Log.i("Miparing--SendData","打开通知成功！");
                                        isNotified=true;
                                    }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                        Log.i("Miparing--SendData","打开通知失败！");
                                        isNotified=false;
                                        if(isOpen){
                                            HandlerUtil.sendFailureMessage(Constants.OPEN_STATUS_ERROR,getResources().getString(R.string.lockdetails_open_error),mHandler);
                                        }else{
                                            HandlerUtil.sendFailureMessage(Constants.CLOSE_STATUS_ERROR,getResources().getString(R.string.lockdetails_close_error),mHandler);
                                        }
                                    }
                                }
                            });

                        }
                        //receiveData=new byte[0];
                        //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                        receiveHandler=new Handler();
                        receiveRunnable=new Runnable() {
                            @Override
                            public void run() {
                                //开关锁前的查询完后就解析数据，不扔出去了

                                String logData="";
                                for(int i=0;i<receiveData.length;i++){
                                    int intData=receiveData[i]&0xFF;
                                    logData=logData+intData+" ";
                                }
                                Log.i("Miparing--SendData","收到返回包为："+logData);

                                if(receiveData!=null&&receiveData.length==10){
                                    byte[] requestArray= Arrays.copyOfRange(receiveData,4,8);
                                    String requestStr=new String(requestArray);//请求是否失败，SUCC表示成功

                                    if(requestStr.equals("SUCC")){
                                        //查询成功

                                        byte statusByte= receiveData[2];
                                        int statusInt=statusByte;//锁的状态，第3位是锁的状态，2开启，1关闭，0异常，3上升，4下降,比较的时候按照十进制比较
                                        Log.i("Miparing--SendData","锁的状态为："+statusInt);
                                        //锁的状态
                                        switch(statusInt){
                                            case 48:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                                }
                                                break;
                                            case 49:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_open_isclosed), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_close_isclosed), mHandler);
                                                }
                                                break;
                                            case 50:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISOPENED, getResources().getString(R.string.lockdetails_open_isopened), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISOPENED, getResources().getString(R.string.lockdetails_close_isopened), mHandler);
                                                }
                                                break;
                                            case 51:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_RISING, getResources().getString(R.string.lockdetails_open_rising), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_RISING, getResources().getString(R.string.lockdetails_close_rising), mHandler);
                                                }
                                                break;
                                            case 52:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_DROPING, getResources().getString(R.string.lockdetails_open_droping), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_DROPING, getResources().getString(R.string.lockdetails_close_droping), mHandler);
                                                }
                                                break;
                                            default:
                                                if(isOpen) {
                                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_open_isclosed), mHandler);
                                                }else{
                                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_close_isclosed), mHandler);
                                                }
                                                break;
                                        }
                                    }else{
                                        //查询失败
                                        if(isOpen) {
                                            HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                        }else{
                                            HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                        }
                                    }

                                }else{
                                    //接收的包不全，认定为查询失败
                                    if(isOpen) {
                                        HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                    }else{
                                        HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                    }
                                }
                            }
                        };
                        receiveHandler.postDelayed(receiveRunnable,receiveDelayed);
                    }

                }
            };
            sendHandler.postDelayed(sendRunnable,sendDelayed);
    }

    //开关锁的动作方法
    private void openOrCloseLock(final boolean isOpen){

        receiveData=new byte[0];
        String lockSecret=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_SECRET);
        lockAddress=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
        times=0;
        sendData= EncryptManager.unlockImmediatelyEncrypt(lockSecret);
        sendHandler=new Handler();
        sendRunnable=new Runnable() {
            @Override
            public void run() {
                times=times+1;
                if(times<sendTimes){
                    sendHandler.postDelayed(sendRunnable,sendDelayed);
                    final byte[] subData= SubpackageManager.sendSubData(sendData,times);
                    mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                sendDataSuccess(subData);
                            }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                Log.i("Miparing--SendData","发送数据第"+times+"包失败");
                                sendHandler.removeCallbacks(sendRunnable);
                                if(isOpen){
                                    HandlerUtil.sendFailureMessage(Constants.OPEN_SEND_DATA_FAILD,"发送数据第"+times+"包失败",mHandler);
                                }else{
                                    HandlerUtil.sendFailureMessage(Constants.CLOSE_SEND_DATA_FAILD,"发送数据第"+times+"包失败",mHandler);
                                }
                            }
                        }
                    });
                }else{
                    //只要发送4包，第五次就移除计时器
                    sendHandler.removeCallbacks(sendRunnable);
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                    //打开并接收通知，里面有返回的数据,只有第一次的时候才开启notify，不然会有很多
                    if(!isNotified){

                        mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                            @Override
                            public void onNotify(UUID service, UUID character, byte[] value) {
                                //拼接包，共返回四个包，把每个包拼接起来·
                                receiveData= ArrayMergeUtil.byteArrayMerge(receiveData,value);


                                //以下是为了查看log而打印出来，与逻辑无关
                                String logData="";
                                for(int i=0;i<value.length;i++){
                                    int intData=value[i]&0xFF;
                                    logData=logData+intData+" ";
                                }
                                Log.i("Miparing--SendData","收到通知为："+logData);
                            }

                            @Override
                            public void onResponse(int code) {
                                if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                    Log.i("Miparing--SendData","打开通知成功！");
                                    isNotified=true;
                                }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                    Log.i("Miparing--SendData","打开通知失败！");
                                    isNotified=false;
                                    if(isOpen) {
                                        HandlerUtil.sendFailureMessage(Constants.OPEN_RECEIVE_DATA_FAILED, "打开通知失败", mHandler);
                                    }else{
                                        HandlerUtil.sendFailureMessage(Constants.CLOSE_RECEIVE_DATA_FAILED, "打开通知失败", mHandler);
                                    }
                                }
                            }
                        });

                    }
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    receiveHandler=new Handler();
                    receiveRunnable=new Runnable() {
                        @Override
                        public void run() {
                            if(isOpen) {
                                HandlerUtil.sendMessage(Constants.OPEN_RECEIVE_DATA_SUCCESS,receiveData,mHandler);
                            }else{
                                HandlerUtil.sendMessage(Constants.CLOSE_RECEIVE_DATA_SUCCESS,receiveData,mHandler);
                            }
                        }
                    };
                    receiveHandler.postDelayed(receiveRunnable,receiveDelayed);
                }

            }
        };
        sendHandler.postDelayed(sendRunnable,sendDelayed);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lockDetailsBackRL:
                handleBack();
                break;

            case R.id.lockDetailsDeleteTV:
                handleDelete();
                break;

            case R.id.lockdetailsQueryStatusLL:
                handleQueryStatus(true);
                break;

            case R.id.lockdetailsChangePasswordLL:
                handleChangePassword();
                break;

            case R.id.lockdetailsCloseLockLL:
                handleCloseLock();
                break;

            case R.id.lockdetailsOpenLockLL:
                handleOpenLock();
                break;

        }
    }

    @Override
    public void onInputFinish(String result) {
        showPwdTimes=showPwdTimes+1;
        if(showPwdTimes==1){
            //新密码框
            PwdFragmentManager.dismissPwdFragment(pwdFragment);
            oldPwd=result;//此时新密码弹框还没出来，所以result是旧密码
            pwdFragment= PwdFragmentManager.showPwdFragment(LockDetailsActivity.this,this,
                    getResources().getString(R.string.lockdetails_changepwd_new_title),getResources().getString(R.string.lockdetails_changepwd_new_content));
        }
        if(showPwdTimes==2){
            //再次新密码框
            PwdFragmentManager.dismissPwdFragment(pwdFragment);
            newPwd=result;//此时再次新密码弹框还没出来，所以result是新密码
            pwdFragment= PwdFragmentManager.showPwdFragment(LockDetailsActivity.this,this,
                    getResources().getString(R.string.lockdetails_changepwd_newagain_title),getResources().getString(R.string.lockdetails_changepwd_newagain_content));
        }
        if(showPwdTimes==3){
            PwdFragmentManager.dismissPwdFragment(pwdFragment);
            newPwdAgain=result;//此时是再次新密码输入完，所以result是再次新密码
            if(newPwd.equals(newPwdAgain)){
                loadingDialog= DialogUtil.createLoadingDialog(this,getResources().getString(R.string.loading_dialog_text));
                receiveData=new byte[0];
                lockAddress=mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
                times=0;
                sendData= EncryptManager.modifySecretEncrypt(oldPwd,newPwd);
                sendHandler=new Handler();
                sendRunnable=new Runnable() {
                    @Override
                    public void run() {
                        times=times+1;
                        if(times<sendTimes){
                            sendHandler.postDelayed(sendRunnable,sendDelayed);
                            final byte[] subData= SubpackageManager.sendSubData(sendData,times);
                            mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                                @Override
                                public void onResponse(int code) {
                                    if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                        sendDataSuccess(subData);
                                    }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                        Log.i("Miparing--SendData","发送数据第"+times+"包失败");
                                        sendHandler.removeCallbacks(sendRunnable);
                                        HandlerUtil.sendFailureMessage(Constants.CHANGE_SEND_DATA_FAILD,"发送数据第"+times+"包失败",mHandler);
                                    }
                                }
                            });
                        }else{
                            //只要发送4包，第五次就移除计时器
                            sendHandler.removeCallbacks(sendRunnable);

                            //判断若没有开启过通知，就开启，否则不再重复开启
                            if(!isNotified){

                                mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                                    @Override
                                    public void onNotify(UUID service, UUID character, byte[] value) {
                                        //拼接包，共返回四个包，把每个包拼接起来
                                        receiveData= ArrayMergeUtil.byteArrayMerge(receiveData,value);


                                        //以下是为了查看log而打印出来，与逻辑无关
                                        String logData="";
                                        for(int i=0;i<value.length;i++){
                                            int intData=value[i]&0xFF;
                                            logData=logData+intData+" ";
                                        }
                                        Log.i("Miparing--SendData","收到通知为："+logData);
                                    }

                                    @Override
                                    public void onResponse(int code) {
                                        if(code== com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS){
                                            Log.i("Miparing--SendData","打开通知成功！");
                                            isNotified=true;
                                        }else if(code== com.inuker.bluetooth.library.Constants.REQUEST_FAILED){
                                            Log.i("Miparing--SendData","打开通知失败！");
                                            isNotified=false;
                                            HandlerUtil.sendFailureMessage(Constants.CHANGE_RECEIVE_DATA_FAILED,"打开通知失败",mHandler);
                                        }
                                    }
                                });

                            }

                            receiveHandler=new Handler();
                            receiveRunnable=new Runnable() {
                                @Override
                                public void run() {
                                    HandlerUtil.sendMessage(Constants.CHANGE_RECEIVE_DATA_SUCCESS,receiveData,mHandler);
                                }
                            };
                            receiveHandler.postDelayed(receiveRunnable,receiveDelayed);
                        }

                    }
                };
                sendHandler.postDelayed(sendRunnable,sendDelayed);

            }else{
                //新密码不一致，请重新设置
                ToastUtil.shortToast(LockDetailsActivity.this,"新密码不一致，请重新设置！");
            }
        }
    }

    /**
     * 开锁时的转圈框，表示上升或者下降中
     * 一定时间后，关闭上升或下降的转圈框
     * @param content 显示的内容
     */
    private void showOpenDialog(String content){
        if(openDialog==null||!openDialog.isShowing()){
            openDialog=DialogUtil.createLoadingDialog(LockDetailsActivity.this,content);
            Handler disMissHandler=new Handler();
            Runnable disMissRunnable=new Runnable() {
                @Override
                public void run() {
                    DialogUtil.closeDialog(openDialog);
                    isOpeningClosed=true;
                }
            };
            disMissHandler.postDelayed(disMissRunnable,dismissDelayed);
        }

    }

    /**
     * 关锁时的转圈框，表示上升或者下降中
     * 一定时间后，关闭上升或下降的转圈框
     * @param content 显示的内容
     */
    private void showCloseDialog(String content){
        if(closeDialog==null||!closeDialog.isShowing()){
            closeDialog=DialogUtil.createLoadingDialog(LockDetailsActivity.this,content);
            Handler disMissHandler=new Handler();
            Runnable disMissRunnable=new Runnable() {
                @Override
                public void run() {
                    DialogUtil.closeDialog(closeDialog);
                    isClosingClosed=true;
                }
            };
            disMissHandler.postDelayed(disMissRunnable,dismissDelayed);
        }

    }
}
