package com.sziton.miparking.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sziton.miparking.R;

/**
 * Created by fwj on 2017/11/28.
 */

public class AddPopwindow extends PopupWindow {
    private TextView addDevicePairTV;
    private TextView addScanPairTV;
    private TextView addCancleTV;
    private View mMenuView;

    public AddPopwindow(Activity activity, View.OnClickListener itemsOnclick){
        super(activity);
        LayoutInflater inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView=inflater.inflate(R.layout.popwindow_lockmanager_add,null);
        addDevicePairTV= (TextView) mMenuView.findViewById(R.id.popwindowDevicePairTV);
        addScanPairTV= (TextView) mMenuView.findViewById(R.id.popwindowScanPairTV);
        addCancleTV= (TextView) mMenuView.findViewById(R.id.popwindowCancleTV);
        //取消按钮
        addCancleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //设置设备配对和扫描配对按钮
        addDevicePairTV.setOnClickListener(itemsOnclick);
        addScanPairTV.setOnClickListener(itemsOnclick);
        //设置popwindow的view
        this.setContentView(mMenuView);
        //设置弹出窗体的宽度
        this.setWidth(LinearLayout.LayoutParams.FILL_PARENT);
        //设置弹出窗体的高度
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        //设置弹出窗体可点击
        this.setFocusable(true);
        //设置弹出窗体动画效果
        //this.setAnimationStyle(R.style.AnimBottom);

        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.addPopwindowLL).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });

    }
}
