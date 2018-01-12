package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.sziton.miparking.R;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.DialogUtil;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by fwj on 2017/11/25.
 */

public class ScanBleActivity extends Activity {
    private BluetoothClient mClient;
    private ArrayList<SearchResult> lockArraylist;
    private TreeMap<String,MyDeviceBean> hasConnectedTreeMap;//本地保存着的之前连接过的
    private ArrayList<String> keysArrayList;
    private ArrayList<MyDeviceBean> valuesArraylist;
    private MySharedPreferences mySharedPreferences;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanble);
        initView();
        initData();
    }

    private void initView(){
        loadingDialog=DialogUtil.createLoadingDialog(this,getResources().getString(R.string.connecting_dialog_text));
    }

    private void initData(){
        mySharedPreferences= MySharedPreferences.getInstance(this);
        lockArraylist=new ArrayList<>();
        mClient= ClientManager.getClient();
        scanDevice();
    }

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
                if(!lockArraylist.contains(device)){
                    //添加进列表前，遍历保存在本地的设备列表，若本地已保存该设备，则不添加进搜索出的列表中
                    if(valuesArraylist!=null&&valuesArraylist.size()>0){
                        if(!valuesArraylist.contains(device.getAddress())){
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
                //搜索到设备，放到arraylist里传到设备列表页
                DialogUtil.closeDialog(loadingDialog);
                Bundle bundle=new Bundle();
                bundle.putSerializable(Constants.SCAN_DEVICE_LIST,lockArraylist);
                Intent lockListIntent=new Intent();
                lockListIntent.putExtras(bundle);
                lockListIntent.setClass(ScanBleActivity.this,LockListActivity.class);
                startActivity(lockListIntent);
                finish();
            }

            @Override
            public void onSearchCanceled() {
                Log.i("Miparking----scanBle","SearchCanceled");
                mClient.stopSearch();
                finish();
            }
        });
    }
}
