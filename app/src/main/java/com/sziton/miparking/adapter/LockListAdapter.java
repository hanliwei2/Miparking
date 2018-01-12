package com.sziton.miparking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;
import com.sziton.miparking.R;

import java.util.ArrayList;

/**
 * Created by fwj on 2017/11/24.
 */

public class LockListAdapter extends BaseAdapter{
    private LayoutInflater minflater;
    private ArrayList<SearchResult> lockArrayList;

    public LockListAdapter(Context context,ArrayList<SearchResult> lockArrayList){
        this.minflater=LayoutInflater.from(context);
        this.lockArrayList=lockArrayList;
    }

    @Override
    public int getCount() {
        return lockArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return lockArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=minflater.inflate(R.layout.item_locklist_lv,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        SearchResult device=lockArrayList.get(position);
        viewHolder.getNameTV().setText(device.getName());
        return convertView;
    }

    public class ViewHolder{
        private View view;
        private TextView nameTV;

        public ViewHolder(View view){
            this.view = view;

        }

        private TextView getNameTV(){
            if(nameTV==null){
                nameTV= (TextView) view.findViewById(R.id.itemLockListNameTV);
            }
            return nameTV;
        }

    }
}
