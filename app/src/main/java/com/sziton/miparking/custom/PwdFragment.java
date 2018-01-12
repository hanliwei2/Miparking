package com.sziton.miparking.custom;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.ToastUtil;

/**
 * Created by Laiyimin on 2017/4/20.
 */

public class PwdFragment extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_CONTENT = "extra_content";    //提示框内容
    public static final String EXTRA_TITLE="extra_title";//提示框标题

    private PayPwdView psw_input;
    private PayPwdView.InputCallBack inputCallBack;
    private TextView tv_title;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.fragment_pwd);
        dialog.setCanceledOnTouchOutside(false); // 外部点击取消

        // 设置宽度为屏宽, 靠近屏幕底部。
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.AnimBottom);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);

        initView(dialog);
        return dialog;
    }

    private void initView(Dialog dialog) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            tv_title= (TextView) dialog.findViewById(R.id.tv_title);
            tv_title.setText(bundle.getString(EXTRA_TITLE));
            TextView tv_content = (TextView) dialog.findViewById(R.id.tv_content);
            tv_content.setText(bundle.getString(EXTRA_CONTENT));
        }

        psw_input = (PayPwdView) dialog.findViewById(R.id.payPwdView);
        PwdInputMethodView inputMethodView = (PwdInputMethodView) dialog.findViewById(R.id.inputMethodView);
        psw_input.setInputMethodView(inputMethodView);
        psw_input.setInputCallBack(inputCallBack);

        dialog.findViewById(R.id.iv_close).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                ToastUtil.shortToast(MyApplication.getInstance(),"取消"+tv_title.getText().toString());
                //点击x号的时候，把连接断开
                MySharedPreferences mySharedPreferences=MySharedPreferences.getInstance(MyApplication.getInstance());
                //这个if里是true表示需要断开设备
                //if(mySharedPreferences.getBooleanValue(Constants.IS_NEED_DISCONNECT)){
                    if(!TextUtils.isEmpty(mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC))){
                        BluetoothClient mClient= ClientManager.getClient();
                        mClient.disconnect(mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC));
                    }
                //}
                dismiss();
                break;

        }
    }

    /**
     * 设置输入回调
     *
     * @param inputCallBack
     */
    public void setPaySuccessCallBack(PayPwdView.InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }


}
