package com.sziton.miparking.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sziton.miparking.R;
import com.sziton.miparking.bean.SystemmsgBean;

import java.util.ArrayList;

/**
 * Created by fwj on 2017/11/10.
 */

public class SystemmsgAdapter  extends BaseAdapter{
    private LayoutInflater minflater;
    private ArrayList<SystemmsgBean> systemmsgList;

    public SystemmsgAdapter(Context context, ArrayList<SystemmsgBean> systemmsgList){
        this.minflater=LayoutInflater.from(context);
        this.systemmsgList=systemmsgList;
    }

    @Override
    public int getCount() {
        return systemmsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return systemmsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=minflater.inflate(R.layout.item_systemmsg_lv,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        SystemmsgBean systemmsgBean=systemmsgList.get(position);
        viewHolder.getDate().setText(systemmsgBean.getDate());
        viewHolder.getContent().setText(systemmsgBean.getContent());
        return convertView;
    }

    public class ViewHolder{
        private View view;
        private TextView dateTV;
        private TextView contentTV;

        public ViewHolder(View view){
            this.view = view;

        }

        private TextView getDate(){
            if(dateTV==null){
                dateTV= (TextView) view.findViewById(R.id.itemSystemmsgDateTV);
            }
            return dateTV;
        }

        private TextView getContent(){
            if(contentTV==null){
                contentTV= (TextView) view.findViewById(R.id.itemSystemmsgContentTV);
            }
            return contentTV;
        }
    }
}
