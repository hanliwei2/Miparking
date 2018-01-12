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

public class IconPopwindow extends PopupWindow {
    private TextView cameraTV;
    private TextView albumTV;
    private TextView iconCancelTV;
    private View mMenuView;

    public IconPopwindow(Activity activity, View.OnClickListener itemsOnclick){
        super(activity);
        LayoutInflater inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView=inflater.inflate(R.layout.popwindow_personal_icon,null);
        cameraTV= (TextView) mMenuView.findViewById(R.id.popwindowCameraTV);
        albumTV= (TextView) mMenuView.findViewById(R.id.popwindowAlbumTV);
        iconCancelTV= (TextView) mMenuView.findViewById(R.id.popwindowIconCancleTV);
        //取消按钮
        iconCancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //设置打开相机和打开相册按钮
        cameraTV.setOnClickListener(itemsOnclick);
        albumTV.setOnClickListener(itemsOnclick);
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

                int height = mMenuView.findViewById(R.id.iconPopwindowLL).getTop();
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
