package com.sziton.miparking.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.sziton.miparking.R;

/**
 * Created by fwj on 2017/11/29.
 */

public class HelpCenterActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpcenter);
        initView();
        initData();
    }

    private void initView(){
        backRL= (RelativeLayout) findViewById(R.id.helpCenterBackRL);

        backRL.setOnClickListener(this);
    }

    private void initData(){

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
            case R.id.helpCenterBackRL:
                handleBack();
                break;
        }
    }
}
