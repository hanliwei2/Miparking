package com.sziton.miparking.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.sziton.miparking.R;
import com.sziton.miparking.adapter.SystemmsgAdapter;
import com.sziton.miparking.bean.SystemmsgBean;

import java.util.ArrayList;

/**
 * Created by fwj on 2017/11/10.
 */

public class SystemmsgActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private RelativeLayout backRL;
    private ListView systemmsgLV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systemmsg);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.systemmsgBackRL);
        systemmsgLV= (ListView) findViewById(R.id.systemmsgLV);

        backRL.setOnClickListener(this);
    }

    private void initData(){
        ArrayList<SystemmsgBean> systemmsgList=new ArrayList<>();
        setData(systemmsgList);
        SystemmsgAdapter adapter=new SystemmsgAdapter(this,systemmsgList);
        systemmsgLV.setAdapter(adapter);
        systemmsgLV.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.systemmsgBackRL:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    //假数据
    public void setData(ArrayList<SystemmsgBean> systemmsgList){
        for(int i=0;i<10;i++){
            SystemmsgBean systemmsgBean=new SystemmsgBean(getResources().getString(R.string.systemmsg_date_text),getResources().getString(R.string.systemmsg_content_text));
            systemmsgList.add(systemmsgBean);
        }
    }
}
