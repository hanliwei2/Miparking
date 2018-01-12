package com.sziton.miparking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.sziton.miparking.R;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.ClientManager;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by fwj on 2017/11/27.
 */

public class HasConnectedAdapter extends BaseAdapter {
    private LayoutInflater minflater;
    //private ArrayList<SearchResult> hasConnectedArrayList;
    private BluetoothClient mClient;
    private TreeMap<String,MyDeviceBean> hasConnectedTreeMap;
    private ArrayList<String> keysArrayList;

    public HasConnectedAdapter(Context context, TreeMap<String,MyDeviceBean> hasConnectedTreeMap, ArrayList<String> keysArrayList){
        this.minflater=LayoutInflater.from(context);
        this.hasConnectedTreeMap=hasConnectedTreeMap;
        this.keysArrayList=keysArrayList;
        mClient= ClientManager.getClient();
    }

    @Override
    public int getCount() {
        return keysArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return keysArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=minflater.inflate(R.layout.item_hasconnected_lv,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        //SearchResult device=keysArrayList.keySet();
        //设备名
        String deviceName=keysArrayList.get(position);
        //设备地址
        String deviceAddress=hasConnectedTreeMap.get(deviceName).getAddress();
        viewHolder.getNameTV().setText(deviceName);
        //已连接或未连接
        String connectStatus;
        if(mClient.getConnectStatus(deviceAddress)== Constants.STATUS_DEVICE_CONNECTED){
            connectStatus="已连接";
        }else{
            connectStatus="未连接";
        }
        viewHolder.getStatusTV().setText(connectStatus);
        return convertView;
    }

    public class ViewHolder{
        private View view;
        private TextView nameTV;
        private TextView statusTV;

        public ViewHolder(View view){
            this.view = view;

        }

        private TextView getNameTV(){
            if(nameTV==null){
                nameTV= (TextView) view.findViewById(R.id.itemHasConnectedNameTV);
            }
            return nameTV;
        }

        private TextView getStatusTV(){
            if(statusTV==null){
                statusTV= (TextView) view.findViewById(R.id.itemHasConnectedStatusTV);
            }
            return statusTV;
        }

    }
}
