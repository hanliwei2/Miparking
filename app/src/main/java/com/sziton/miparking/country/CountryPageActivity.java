/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package com.sziton.miparking.country;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.sziton.miparking.R;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;

/** 国家列表界面 */
public class CountryPageActivity extends Activity implements TextWatcher, GroupListView.OnItemClickListener{
    private EditText editText;
    private CountryListView listView;
    private MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化搜索引擎
        SearchEngine.prepare(CountryPageActivity.this, new Runnable() {
            
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        // 初始化搜索引擎
                        setContentView(R.layout.country_list_page);

                        listView = (CountryListView) findViewById(R.id.clCountry);
                        editText = (EditText) findViewById(R.id.et_put_identify);
                        listView.setOnItemClickListener(CountryPageActivity.this);
                        editText.addTextChangedListener(CountryPageActivity.this);
                    }
                });
            }
        });
        mySharedPreferences=MySharedPreferences.getInstance(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        listView.onSearch(s.toString().toLowerCase());
    }

    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemClick(GroupListView parent, View view, int group, int position) {
        if(position >= 0){
            String[] country = listView.getCountry(group, position);
            /*Toast.makeText(CountryPageActivity.this, "您选择的是:"+ country[0]
                    +  "id:" + country[1], Toast.LENGTH_SHORT).show();*/
            mySharedPreferences.setStringValue(Constants.COUNTRY_ID,country[1]);
            finish();
        }
    }

}
