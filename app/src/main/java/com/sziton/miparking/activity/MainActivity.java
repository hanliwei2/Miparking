package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.bluetoothkit.BleUuids;
import com.sziton.miparking.bluetoothkit.ClientManager;
import com.sziton.miparking.bluetoothkit.SubpackageManager;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.custom.CustomIndicator;
import com.sziton.miparking.custom.PwdFragment;
import com.sziton.miparking.daoimpl.MainDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.encryption.EncryptManager;
import com.sziton.miparking.utils.ArrayMergeUtil;
import com.sziton.miparking.utils.Base64Convert;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.HandlerUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener {
    private RoundedImageView headIV;
    private ImageView lockIV;
    private ImageView openIV;
    private ViewPager viewPager;
    private PagerAdapter viewPagerAdapter;
    private List<ImageView> views = new ArrayList<ImageView>();
    private CustomIndicator indicator;
    public static int count = 3;//3页
    private int[] imgIds = {R.drawable.main_viewpager1_bg, R.drawable.main_viewpager2_bg,
            R.drawable.main_viewpager3_bg};
    private MySharedPreferences mySharedPreferences;
    private int autoDelayed = 3000;//轮播图自动轮播间隔3秒
    private boolean isLoop;//判断是否要自动播放
    // 计算初始位置
    private int maxSize = 65535;//最大值为65535
    private int pos;
    /**
     * 以下是开关锁
     */
    private String lockName;
    private String lockSecret;
    private byte[] sendData;
    private int times = 0;
    private Handler sendHandler;//分包发送数据的计时器
    private Runnable sendRunnable;
    private int sendDelayed = 20;//间隔20毫秒发送一包数据
    private int sendTimes = 5;//5表示发送4次，第5次就关闭计时器
    private Handler receiveHandler;//接收数据的计时器
    private Runnable receiveRunnable;
    private int receiveDelayed = 500;//满500毫秒就当接收数据完成
    private byte[] receiveData;//接收到的数据
    private Dialog loadingDialog;
    private Dialog openDialog;//开锁时转圈的框框
    private Dialog closeDialog;//关锁时转圈的框框
    private String lockAddress;
    private String deviceName;//设备名字，这是treemap中的key
    private boolean isNotified;
    private boolean isNeedToast;//判断是否是打开页面的查询，true表示需要弹出查询成功，false表示不需要弹出
    private PwdFragment pwdFragment;
    private String oldPwd;//旧密码
    private String newPwd;//新密码
    private String newPwdAgain;//再次输入新密码
    private int showPwdTimes = 0;
    private String lockStatus = "";//开关锁前查询到的锁的状态
    private int queryDelayed = 2000;//2秒查询一次升降状态
    private int dismissDelayed = 10000;//10秒后关闭转圈框
    private boolean isLockOpened = false;
    private boolean isOpeningClosed = false;//用与开锁时给上升中和下降中判断，不然如果一直返回上升中或下降中，会成死循环，现在当dialog10秒后消失的时候，就设置成true，就不再执行了
    private boolean isLockClosed = false;
    private boolean isClosingClosed = false;//用与关锁时给上升中和下降中判断
    private BluetoothClient mClient;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //获取轮播图，网络请求成功
                case Constants.MAIN_VIEWPAGER_SUCCESS:
                    JSONObject viewPagerJson = (JSONObject) msg.obj;
                    try {
                        String success = viewPagerJson.getString("Success");
                        if (success.equals("True")) {
                            //获取轮播图成功
                            Log.i("Miparking", "viewPagerJson----->>" + viewPagerJson.toString());
                            JSONObject resultJson = viewPagerJson.getJSONObject("Result");
                            Log.i("Miparking", "resultObject----->>" + resultJson.toString());

                            ToastUtil.shortToast(MainActivity.this, "轮播图获取成功！");

                            indicator.setCount(count);
                            initViewPager();
                            viewPager.setOnPageChangeListener(pageChangeListener);
                            viewPager.setOnTouchListener(pageTouchLinstener);

                        } else {
                            //获取轮播图失败
                            Log.i("Miparking", "viewPagerJson----->>" + viewPagerJson.toString());
                            String errorMessage = viewPagerJson.getString("ErrorMessage");
                            ToastUtil.shortToast(MainActivity.this, success + "---->>" + errorMessage + "！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                //获取轮播图，网络请求失败
                case Constants.MAIN_VIEWPAGER_FAILURE:
                    ToastUtil.shortToast(MainActivity.this, "轮播图获取网络异常！");
                    break;

                /**
                 * 开关锁
                 */
                case Constants.MAIN_NO_DEVICE:
                    DialogUtil.closeDialog(loadingDialog);
                    ToastUtil.shortToast(MainActivity.this, getResources().getString(R.string.main_no_device));
                    break;
                case Constants.MAIN_NO_CONNECTED:
                    DialogUtil.closeDialog(loadingDialog);
                    ToastUtil.shortToast(MainActivity.this, getResources().getString(R.string.main_no_connected));
                    break;

                /**
                 * 开锁
                 */
                //锁的状态
                case Constants.OPEN_STATUS_ISOPENED://已在开启状态
                    DialogUtil.closeDialog(loadingDialog);
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_isopened));
                    DialogUtil.closeDialog(openDialog);
                    break;
                case Constants.OPEN_STATUS_ERROR://异常
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(MainActivity.this,MainActivity.this.getResources().getString(R.string.lockdetails_open_error));
                    DialogUtil.closeDialog(openDialog);
                    //弹出确认框
                    new AlertDialog.Builder(MainActivity.this).setTitle("锁状态异常，继续开锁吗？")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的操作
                                    openOrCloseLock(Constants.OPEN_LOCK);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    dialog.dismiss();
                                }
                            }).show();
                    break;
                case Constants.OPEN_STATUS_RISING://上升中
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_rising));
                    if (!isOpeningClosed) {
                        showOpenDialog(getResources().getString(R.string.lockdetails_open_isrising));
                        Handler openRiseHandler = new Handler();
                        Runnable openRiseRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.OPEN_LOCK);
                            }
                        };
                        openRiseHandler.postDelayed(openRiseRunnable, queryDelayed);
                    }

                    break;
                case Constants.OPEN_STATUS_DROPING://下降中
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_droping));
                    if (!isOpeningClosed) {
                        showOpenDialog(getResources().getString(R.string.lockdetails_open_isdroping));
                        Handler openDropHandler = new Handler();
                        Runnable openDropRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.OPEN_LOCK);
                            }
                        };
                        openDropHandler.postDelayed(openDropRunnable, queryDelayed);
                    }
                    break;
                case Constants.OPEN_STATUS_ISCLOSED://关闭状态
                    DialogUtil.closeDialog(loadingDialog);
                    DialogUtil.closeDialog(openDialog);
                    if (!isLockOpened) {
                        //ToastUtil.shortToast(MainActivity.this,MainActivity.this.getResources().getString(R.string.lockdetails_open_isclosed));
                        openOrCloseLock(Constants.OPEN_LOCK);
                    }
                    break;
                //开锁是否成功
                case Constants.OPEN_SEND_DATA_FAILD://发送数据失败
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    break;
                case Constants.OPEN_RECEIVE_DATA_FAILED://开启 通知失败
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    break;
                case Constants.OPEN_RECEIVE_DATA_SUCCESS://开锁成功
                    byte[] openArray = (byte[]) msg.obj;

                    String logData2 = "";
                    for (int i = 0; i < openArray.length; i++) {
                        int intData = openArray[i] & 0xFF;
                        logData2 = logData2 + intData + " ";
                    }
                    Log.i("Miparing--SendData", "开锁收到返回包为：" + logData2);

                    //文档上写的收到数据长度是74，但是实际收到的长度是84
                    if (openArray != null && openArray.length == 84) {
                        byte[] statusArray = Arrays.copyOfRange(openArray, 0, 8);
                        String statusStr = new String(statusArray);
                        Log.i("Miparing--SendData", "开锁收到返回statusStr为：" + statusStr);
                        //接收的包里面0-8个字节拼接出是否成功,SUCCSUCC表示成功，其他表示失败
                        if (statusStr.equals("SUCCSUCC")) {
                            ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_success));
                            //开锁成功，弹出转圈框，继续查询状态，直到开锁完成
                            isLockOpened = true;
                            showOpenDialog(getResources().getString(R.string.lockdetails_open_isdroping));

                            Handler openQueryHandler = new Handler();
                            Runnable openQueryRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    queryLock(Constants.OPEN_LOCK);
                                }
                            };
                            openQueryHandler.postDelayed(openQueryRunnable, queryDelayed);

                        } else {
                            ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                        }
                    } else {
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_open_failed));
                    }
                    break;

                /**
                 * 关锁
                 */
                //锁的状态
                case Constants.CLOSE_STATUS_ISOPENED://锁是开启状态
                    DialogUtil.closeDialog(loadingDialog);
                    DialogUtil.closeDialog(closeDialog);
                    if (!isLockClosed) {
                        //ToastUtil.shortToast(MainActivity.this,MainActivity.this.getResources().getString(R.string.lockdetails_close_isopened));
                        openOrCloseLock(Constants.CLOSE_LOCK);
                    }
                    break;
                case Constants.CLOSE_STATUS_ERROR://异常
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(MainActivity.this,MainActivity.this.getResources().getString(R.string.lockdetails_close_error));
                    DialogUtil.closeDialog(closeDialog);
                    //弹出确认框
                    new AlertDialog.Builder(MainActivity.this).setTitle("锁状态异常，继续关锁吗？")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的操作
                                    openOrCloseLock(Constants.CLOSE_LOCK);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    dialog.dismiss();
                                }
                            }).show();
                    break;
                case Constants.CLOSE_STATUS_RISING://上升中
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_rising));
                    if (!isClosingClosed) {
                        showCloseDialog(getResources().getString(R.string.lockdetails_close_isrising));
                        Handler closeRiseHandler = new Handler();
                        Runnable closeRiseRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.CLOSE_LOCK);
                            }
                        };
                        closeRiseHandler.postDelayed(closeRiseRunnable, queryDelayed);
                    }
                    break;
                case Constants.CLOSE_STATUS_DROPING://下降中
                    DialogUtil.closeDialog(loadingDialog);
                    //ToastUtil.shortToast(LockDetailsActivity.this,LockDetailsActivity.this.getResources().getString(R.string.lockdetails_open_droping));
                    if (!isClosingClosed) {
                        showCloseDialog(getResources().getString(R.string.lockdetails_close_isdroping));
                        Handler closeDropHandler = new Handler();
                        Runnable closeDropRunnable = new Runnable() {
                            @Override
                            public void run() {
                                queryLock(Constants.CLOSE_LOCK);
                            }
                        };
                        closeDropHandler.postDelayed(closeDropRunnable, queryDelayed);
                    }
                    break;
                case Constants.CLOSE_STATUS_ISCLOSED://关闭状态
                    DialogUtil.closeDialog(loadingDialog);
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_isclosed));
                    DialogUtil.closeDialog(closeDialog);
                    break;
                //关锁是否成功
                case Constants.CLOSE_SEND_DATA_FAILD://发送数据失败
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    break;
                case Constants.CLOSE_RECEIVE_DATA_FAILED://开启 通知失败
                    ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    break;
                case Constants.CLOSE_RECEIVE_DATA_SUCCESS://开锁成功
                    byte[] closeArray = (byte[]) msg.obj;

                    String logData3 = "";
                    for (int i = 0; i < closeArray.length; i++) {
                        int intData = closeArray[i] & 0xFF;
                        logData3 = logData3 + intData + " ";
                    }
                    Log.i("Miparing--SendData", "开锁收到返回包为：" + logData3);

                    //文档上写的收到数据长度是74，但是实际收到的长度是84
                    if (closeArray != null && closeArray.length == 84) {
                        byte[] statusArray = Arrays.copyOfRange(closeArray, 0, 8);
                        String statusStr = new String(statusArray);
                        Log.i("Miparing--SendData", "开锁收到返回statusStr为：" + statusStr);
                        //接收的包里面0-8个字节拼接出是否成功,SUCCSUCC表示成功，其他表示失败
                        if (statusStr.equals("SUCCSUCC")) {
                            ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_success));
                            //关锁成功，弹出转圈框，继续查询状态，直到关锁完成
                            isLockClosed = true;
                            showCloseDialog(getResources().getString(R.string.lockdetails_close_isrising));

                            Handler closeQueryHandler = new Handler();
                            Runnable closeQueryRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    queryLock(Constants.CLOSE_LOCK);
                                }
                            };
                            closeQueryHandler.postDelayed(closeQueryRunnable, queryDelayed);

                        } else {
                            ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                        }
                    } else {
                        //接收的包不全，认定为配对失败
                        ToastUtil.shortToast(MainActivity.this, MainActivity.this.getResources().getString(R.string.lockdetails_close_failed));
                    }
                    break;


            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        MyApplication.addActivity(this);
        headIV = (RoundedImageView) findViewById(R.id.mainHeadIV);
        lockIV = (ImageView) findViewById(R.id.mainLockIV);
        openIV = (ImageView) findViewById(R.id.mainOpenIV);
        indicator = (CustomIndicator) findViewById(R.id.mainIndicator);
        viewPager = (ViewPager) findViewById(R.id.mainViewPager);

        headIV.setOnClickListener(this);
        lockIV.setOnClickListener(this);
        openIV.setOnClickListener(this);
    }

    private void initData() {
        mySharedPreferences = MySharedPreferences.getInstance(this);
        mClient = ClientManager.getClient();
        //初始化轮播图，3行
        indicator.setCount(count);
        initViewPager();
        viewPager.setOnPageChangeListener(pageChangeListener);
        viewPager.setOnTouchListener(pageTouchLinstener);

        //自动轮播
        isLoop = false;
        startLoop();

        //getAdvertIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //登录成功，从返回的数据里获取icon作为头像
        String iconStr = mySharedPreferences.getStringValue(Constants.REGISTER_ICON_DEFAULT_KEY);
        if (!TextUtils.isEmpty(iconStr)) {
            Bitmap iconBitmap = Base64Convert.base64ToBitmap(iconStr);
            headIV.setImageBitmap(iconBitmap);
        }
    }

    //viewpager滑动监听
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            indicator.setCurrentPosition(position % count);
            pos = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    //viewpager触摸滑动监听，滑动时若在自动轮播，那就把自动轮播停止掉
    private View.OnTouchListener pageTouchLinstener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    isLoop = true;
                    stopLoop();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isLoop = false;
                    startLoop();
                default:
                    break;
            }
            return false;
        }
    };

    //初始化viewpager
    private void initViewPager() {
        views.clear();
        for (int i = 0; i < count; i++) {
            ImageView view = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
            view.setBackgroundResource(imgIds[i]);
            views.add(view);
        }
        viewPagerAdapter = new PagerAdapter() {

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(container);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                int pos = position % count;
                View view = views.get(pos);
                if (container.getChildCount() == views.size()) {
                    container.removeView(view);
                }
                container.addView(view);
                return view;
            }

            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };
        viewPager.setAdapter(viewPagerAdapter);
        // 计算初始位置
        pos = maxSize / 2 - maxSize / 2 % count; // 计算初始位置
        viewPager.setCurrentItem(pos);
    }

    private boolean hasChild(ViewGroup group, View view) {
        boolean flag = false;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (view == group.getChildAt(i)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 轮播图自动播放
     */

    Handler autoHandler = new Handler();
    Runnable autoRunnable = new Runnable() {
        @Override
        public void run() {
            //item多于1个时
            if (viewPager.getChildCount() > 1) {
                autoHandler.postDelayed(this, autoDelayed);
                pos++;
                viewPager.setCurrentItem(pos, true);
                if (pos == maxSize) {
                    pos = maxSize / 2 - maxSize / 2 % count;
                }
            }
        }
    };

    //开始自动轮播
    private void startLoop() {
        if (!isLoop && viewPager != null) {
            autoHandler.postDelayed(autoRunnable, autoDelayed);// 每两秒执行一次runnable.
            isLoop = true;
        }
    }

    //暂停自动轮播
    public void stopLoop() {
        if (isLoop && viewPager != null) {
            autoHandler.removeCallbacks(autoRunnable);
            isLoop = false;
        }
    }

    /**
     * 获取轮播图
     */
    private void getAdvertIcon() {
        String url = Paths.appUrl;
        //以下是接口需要的参数
        String timestamp = EncryptUtil.getTimestamp();
        String signatureNonce = EncryptUtil.getSignatureNonce();
        String action = Constants.MAIN_VIEWPAGER_ACTION;

        //通过签名算法得到的Signature
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("Timestamp", timestamp);
        paramsMap.put("SignatureNonce", signatureNonce);
        paramsMap.put("Action", action);
        String signature = EncryptUtil.getSignature(paramsMap);

        MainDaoImpl mainDaoImpl = new MainDaoImpl();
        mainDaoImpl.postGetAdvertIcon(url, signature, timestamp, signatureNonce, action, mHandler);
    }

    /**
     * 点击头像
     */
    private void handleHead() {
        Intent settingIntent = new Intent();
        settingIntent.setClass(this, SettingActivity.class);
        startActivity(settingIntent);
    }

    /**
     * 开锁
     */
    private void handleOpenLock() {
        loadingDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loading_dialog_text));
        if (mySharedPreferences.getBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED)) {
            //本地已存储过设备
            //获取打开app时自动连接上的设备
            TreeMap<String, MyDeviceBean> myConnectedDevices = MyApplication.myConnectedDevices;
            ArrayList<String> lockNames = new ArrayList<>();
            if (myConnectedDevices != null && myConnectedDevices.size() > 0) {
                for (String key : myConnectedDevices.keySet()) {
                    lockNames.add(key);
                }
                lockName = lockNames.get(0);//第一个设备的名称
                lockAddress = myConnectedDevices.get(lockName).getAddress();//地址u
                lockSecret = myConnectedDevices.get(lockName).getSerect();//密码
                //连接状态下
                if (mClient.getConnectStatus(lockAddress) == com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED) {
                    //开锁
                    //点击开锁，给个false，表示未开，开锁成功后，赋值成true，表示已开，防止重复开锁
                    isLockOpened = false;
                    isOpeningClosed = false;
                    queryLock(Constants.OPEN_LOCK);
                } else {
                    //设备未连接
                    HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT, getResources().getString(R.string.main_no_connected), mHandler);
                }
            } else {
                HandlerUtil.sendFailureMessage(Constants.MAIN_NO_CONNECTED, getResources().getString(R.string.main_no_connected), mHandler);
            }
        } else {
            //本地未存储过设备
            HandlerUtil.sendFailureMessage(Constants.MAIN_NO_DEVICE, getResources().getString(R.string.main_no_device), mHandler);
        }
    }

    //开关锁前的查询动作，这个不是查询状态时调用的方法,
    //因为开锁和关锁都要用这个方法查询状态，所以isOpen用于判断是开锁还是关锁，true表示开锁，false表示关锁
    private void queryLock(final boolean isOpen) {
        receiveData = new byte[0];
        times = 0;
        sendData = EncryptManager.inquireLockStatusEncrypt(lockSecret, lockSecret);
        sendHandler = new Handler();
        sendRunnable = new Runnable() {
            @Override
            public void run() {
                times = times + 1;
                if (times < sendTimes) {
                    sendHandler.postDelayed(sendRunnable, sendDelayed);
                    final byte[] subData = SubpackageManager.sendSubData(sendData, times);
                    mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if (code == com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS) {
                                sendDataSuccess(subData);
                            } else if (code == com.inuker.bluetooth.library.Constants.REQUEST_FAILED) {
                                Log.i("Miparing--SendData", "开锁前查询，发送数据第" + times + "包失败");
                                sendHandler.removeCallbacks(sendRunnable);
                                if (isOpen) {
                                    HandlerUtil.sendFailureMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                } else {
                                    HandlerUtil.sendFailureMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                }
                            }
                        }
                    });
                } else {
                    //只要发送4包，第五次就移除计时器
                    sendHandler.removeCallbacks(sendRunnable);
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                    //打开并接收通知，里面有返回的数据,只有第一次的时候才开启notify，不然会有很多
                    if (!isNotified) {

                        mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                            @Override
                            public void onNotify(UUID service, UUID character, byte[] value) {
                                //拼接包，共返回四个包，把每个包拼接起来
                                receiveData = ArrayMergeUtil.byteArrayMerge(receiveData, value);


                                //以下是为了查看log而打印出来，与逻辑无关
                                String logData = "";
                                for (int i = 0; i < value.length; i++) {
                                    int intData = value[i] & 0xFF;
                                    logData = logData + intData + " ";
                                }
                                Log.i("Miparing--SendData", "收到通知为：" + logData);
                            }

                            @Override
                            public void onResponse(int code) {
                                if (code == com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS) {
                                    Log.i("Miparing--SendData", "打开通知成功！");
                                    isNotified = true;
                                } else if (code == com.inuker.bluetooth.library.Constants.REQUEST_FAILED) {
                                    Log.i("Miparing--SendData", "打开通知失败！");
                                    isNotified = false;
                                    if (isOpen) {
                                        HandlerUtil.sendFailureMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                    } else {
                                        HandlerUtil.sendFailureMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                    }
                                }
                            }
                        });

                    }
                    //receiveData=new byte[0];
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    receiveHandler = new Handler();
                    receiveRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //开关锁前的查询完后就解析数据，不扔出去了

                            String logData = "";
                            for (int i = 0; i < receiveData.length; i++) {
                                int intData = receiveData[i] & 0xFF;
                                logData = logData + intData + " ";
                            }
                            Log.i("Miparing--SendData", "收到返回包为：" + logData);

                            if (receiveData != null && receiveData.length == 10) {
                                byte[] requestArray = Arrays.copyOfRange(receiveData, 4, 8);
                                String requestStr = new String(requestArray);//请求是否失败，SUCC表示成功

                                if (requestStr.equals("SUCC")) {
                                    //查询成功

                                    byte statusByte = receiveData[2];
                                    int statusInt = statusByte;//锁的状态，第3位是锁的状态，2开启，1关闭，0异常，3上升，4下降,比较的时候按照十进制比较
                                    Log.i("Miparing--SendData", "锁的状态为：" + statusInt);
                                    //锁的状态
                                    switch (statusInt) {
                                        case 48:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                            }
                                            break;
                                        case 49:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_open_isclosed), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_close_isclosed), mHandler);
                                            }
                                            break;
                                        case 50:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISOPENED, getResources().getString(R.string.lockdetails_open_isopened), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISOPENED, getResources().getString(R.string.lockdetails_close_isopened), mHandler);
                                            }
                                            break;
                                        case 51:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_RISING, getResources().getString(R.string.lockdetails_open_rising), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_RISING, getResources().getString(R.string.lockdetails_close_rising), mHandler);
                                            }
                                            break;
                                        case 52:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_DROPING, getResources().getString(R.string.lockdetails_open_droping), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_DROPING, getResources().getString(R.string.lockdetails_close_droping), mHandler);
                                            }
                                            break;
                                        default:
                                            if (isOpen) {
                                                HandlerUtil.sendMessage(Constants.OPEN_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_open_isclosed), mHandler);
                                            } else {
                                                HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ISCLOSED, getResources().getString(R.string.lockdetails_close_isclosed), mHandler);
                                            }
                                            break;
                                    }
                                } else {
                                    //查询失败
                                    if (isOpen) {
                                        HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                    } else {
                                        HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                    }
                                }

                            } else {
                                //接收的包不全，认定为查询失败
                                if (isOpen) {
                                    HandlerUtil.sendMessage(Constants.OPEN_STATUS_ERROR, getResources().getString(R.string.lockdetails_open_error), mHandler);
                                } else {
                                    HandlerUtil.sendMessage(Constants.CLOSE_STATUS_ERROR, getResources().getString(R.string.lockdetails_close_error), mHandler);
                                }
                            }
                        }
                    };
                    receiveHandler.postDelayed(receiveRunnable, receiveDelayed);
                }

            }
        };
        sendHandler.postDelayed(sendRunnable, sendDelayed);
    }

    //开关锁的动作方法
    private void openOrCloseLock(final boolean isOpen) {

        receiveData = new byte[0];
        String lockSecret = mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_SECRET);
        lockAddress = mySharedPreferences.getStringValue(Constants.CURRENT_DEVICE_MAC);
        times = 0;
        sendData = EncryptManager.unlockImmediatelyEncrypt(lockSecret);
        sendHandler = new Handler();
        sendRunnable = new Runnable() {
            @Override
            public void run() {
                times = times + 1;
                if (times < sendTimes) {
                    sendHandler.postDelayed(sendRunnable, sendDelayed);
                    final byte[] subData = SubpackageManager.sendSubData(sendData, times);
                    mClient.write(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_CONFIG, subData, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if (code == com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS) {
                                sendDataSuccess(subData);
                            } else if (code == com.inuker.bluetooth.library.Constants.REQUEST_FAILED) {
                                Log.i("Miparing--SendData", "发送数据第" + times + "包失败");
                                sendHandler.removeCallbacks(sendRunnable);
                                if (isOpen) {
                                    HandlerUtil.sendFailureMessage(Constants.OPEN_SEND_DATA_FAILD, "发送数据第" + times + "包失败", mHandler);
                                } else {
                                    HandlerUtil.sendFailureMessage(Constants.CLOSE_SEND_DATA_FAILD, "发送数据第" + times + "包失败", mHandler);
                                }
                            }
                        }
                    });
                } else {
                    //只要发送4包，第五次就移除计时器
                    sendHandler.removeCallbacks(sendRunnable);
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    //因为是64个字节，所以times=5就代表前面4包发完了，就能打开并接收通知了
                    //打开并接收通知，里面有返回的数据,只有第一次的时候才开启notify，不然会有很多
                    if (!isNotified) {

                        mClient.notify(lockAddress, BleUuids.UUID_CLIENT_SERVICE_CONFIG, BleUuids.UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG, new BleNotifyResponse() {
                            @Override
                            public void onNotify(UUID service, UUID character, byte[] value) {
                                //拼接包，共返回四个包，把每个包拼接起来·
                                receiveData = ArrayMergeUtil.byteArrayMerge(receiveData, value);


                                //以下是为了查看log而打印出来，与逻辑无关
                                String logData = "";
                                for (int i = 0; i < value.length; i++) {
                                    int intData = value[i] & 0xFF;
                                    logData = logData + intData + " ";
                                }
                                Log.i("Miparing--SendData", "收到通知为：" + logData);
                            }

                            @Override
                            public void onResponse(int code) {
                                if (code == com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS) {
                                    Log.i("Miparing--SendData", "打开通知成功！");
                                    isNotified = true;
                                } else if (code == com.inuker.bluetooth.library.Constants.REQUEST_FAILED) {
                                    Log.i("Miparing--SendData", "打开通知失败！");
                                    isNotified = false;
                                    if (isOpen) {
                                        HandlerUtil.sendFailureMessage(Constants.OPEN_RECEIVE_DATA_FAILED, "打开通知失败", mHandler);
                                    } else {
                                        HandlerUtil.sendFailureMessage(Constants.CLOSE_RECEIVE_DATA_FAILED, "打开通知失败", mHandler);
                                    }
                                }
                            }
                        });

                    }
                    //打开通知成功后就去开启一个500毫秒的计时器，满这个时间就当接收数据完成,然后把数据扔给上层解析
                    receiveHandler = new Handler();
                    receiveRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (isOpen) {
                                HandlerUtil.sendMessage(Constants.OPEN_RECEIVE_DATA_SUCCESS, receiveData, mHandler);
                            } else {
                                HandlerUtil.sendMessage(Constants.CLOSE_RECEIVE_DATA_SUCCESS, receiveData, mHandler);
                            }
                        }
                    };
                    receiveHandler.postDelayed(receiveRunnable, receiveDelayed);
                }

            }
        };
        sendHandler.postDelayed(sendRunnable, sendDelayed);
    }

    /**
     * 空方法，用于发送一个包成功后调用
     */
    private void sendDataSuccess(byte[] sendData) {
        String logData = "";
        for (int i = 0; i < sendData.length; i++) {
            int intData = sendData[i] & 0xFF;
            logData = logData + intData + " ";
        }
        Log.i("Miparing--SendData", "发送的数据为：" + logData);
    }

    /**
     * 关锁
     */
    private void handleCloseLock() {
        //需要加一个确认框，告知用户确认车是否还在车位上
        //弹出确认框
        new AlertDialog.Builder(MainActivity.this).setTitle("请检查汽车是否还在车位上")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        doClose();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                        dialog.dismiss();
                    }
                }).show();
    }

    //点击关锁按钮后的操作，单独封装出来，因为handleCloseLock里面要放确认框，而直接把这个方法放到确认按钮里
    private void doClose() {
        loadingDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loading_dialog_text));
        if (mySharedPreferences.getBooleanValue(Constants.LOCK_MANAGER_ISCONNECTED)) {
            //本地已存储过设备
            //获取打开app时自动连接上的设备
            TreeMap<String, MyDeviceBean> myConnectedDevices = MyApplication.myConnectedDevices;
            ArrayList<String> lockNames = new ArrayList<>();
            if (myConnectedDevices != null && myConnectedDevices.size() > 0) {
                for (String key : myConnectedDevices.keySet()) {
                    lockNames.add(key);
                }
                lockName = lockNames.get(0);//第一个设备的名称
                lockAddress = myConnectedDevices.get(lockName).getAddress();//地址
                lockSecret = myConnectedDevices.get(lockName).getSerect();//密码
                //连接状态下
                if (mClient.getConnectStatus(lockAddress) == com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED) {
                    //关锁
                    //点击关锁，给个false，表示未关，关锁成功后，赋值成true，表示已关，防止重复关锁
                    isLockClosed = false;
                    isClosingClosed = false;
                    queryLock(Constants.CLOSE_LOCK);
                } else {
                    //设备未连接
                    HandlerUtil.sendFailureMessage(Constants.LOCK_NOT_CONNECT, getResources().getString(R.string.main_no_connected), mHandler);
                }
            } else {
                HandlerUtil.sendFailureMessage(Constants.MAIN_NO_CONNECTED, getResources().getString(R.string.main_no_connected), mHandler);
            }
        } else {
            //本地未存储过设备，即没有已经配对的
            HandlerUtil.sendFailureMessage(Constants.MAIN_NO_DEVICE, getResources().getString(R.string.main_no_device), mHandler);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainHeadIV:
                handleHead();
                break;

            case R.id.mainLockIV:
                handleCloseLock();
                break;

            case R.id.mainOpenIV:
                handleOpenLock();
                break;
        }
    }

    /**
     * 开锁时的转圈框，表示上升或者下降中
     * 一定时间后，关闭上升或下降的转圈框
     *
     * @param content 显示的内容
     */
    private void showOpenDialog(String content) {
        if (openDialog == null || !openDialog.isShowing()) {
            openDialog = DialogUtil.createLoadingDialog(MainActivity.this, content);
            Handler disMissHandler = new Handler();
            Runnable disMissRunnable = new Runnable() {
                @Override
                public void run() {
                    DialogUtil.closeDialog(openDialog);
                    isOpeningClosed = true;
                }
            };
            disMissHandler.postDelayed(disMissRunnable, dismissDelayed);
        }

    }

    /**
     * 关锁时的转圈框，表示上升或者下降中
     * 一定时间后，关闭上升或下降的转圈框
     *
     * @param content 显示的内容
     */
    private void showCloseDialog(String content) {
        if (closeDialog == null || !closeDialog.isShowing()) {
            closeDialog = DialogUtil.createLoadingDialog(MainActivity.this, content);
            Handler disMissHandler = new Handler();
            Runnable disMissRunnable = new Runnable() {
                @Override
                public void run() {
                    DialogUtil.closeDialog(closeDialog);
                    isClosingClosed = true;
                }
            };
            disMissHandler.postDelayed(disMissRunnable, dismissDelayed);
        }

    }

}
