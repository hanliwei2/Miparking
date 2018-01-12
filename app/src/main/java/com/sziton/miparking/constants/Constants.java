package com.sziton.miparking.constants;

/**
 * Created by fwj on 2017/11/15.
 */

public class Constants {
    /**
     * Signature算法签名时传入的固定Secret,
     */
    public static final String SECRET_KEY="Secret";
    public static final String SECRET_VALUE="C1CE9569-9481-4559-8EED-6B228FC7B23A";

    public static final String COOKIE="cookie";

    /**
     * 设置区号
     */
    public static final String COUNTRY_ID="countryId";//区号，例如中国是86

    /**
     * 注册
     */
    public static final String REGISTER_OR_FORGETPWD="registerOrForgetpwd";//用于判断是注册还是忘记密码，因为两个操作请求网络的参数不同，这是sp里的key
    public static final int REGISTER_TAG=1;//用于判断是注册还是忘记密码，因为两个操作请求网络的参数不同，这是sp里的value
    public static final int REGISTER_REQUEST_SUCCESS=1;//网络请求成功
    public static final int REGISTER_REQUEST_FAILURE=2;//网络请求失败
    public static final String REGISTER_ACTION="RegistUser";//固定RegistUser
    public static final String REGISTER_PHONE="registerPhone";//接收验证码时的手机号，这个带+86的，注意
    public static final String REGISTER_NAME_DEFAULT_KEY="registerDefaultName";//注册时的默认用户名key
    public static final String REGISTER_NAME_DEFAULT_VALUE="默认用户名";//注册时的默认用户名value
    public static final String REGISTER_SEX_DEFAULT_KEY="registerDefaultSex";//注册时的默认性别key
    public static final String REGISTER_SEX_DEFAULT_VALUE="男";//注册时的默认性别value
    public static final String REGISTER_ICON_DEFAULT_KEY="registerDefaultIcon";//注册时的默认头像key
    public static final String REGISTER_ICON_DEFAULT_VALUE="";//注册时的默认头像value(Base64编码)
    public static final String REGISTER_BIRTHDAY_DEFAULT_KEY="registerDefaultBirthday";//注册时的默认生日key
    public static final String REGISTER_BIRTHDAY_DEFAULT_VALUE="2017-01-01";//注册时的默认生日value


    /**
     * 登录
     */
    public static final int LOGIN_REQUEST_SUCCESS=1;
    public static final int LOGIN_REQUEST_FAILURE=2;
    public static final String LOGIN_ACTION="LoginUser";
    public static final String LOGIN_BALANCE_DEFAULT_KEY="loginDefaultBalance";//余额
    public static final String LOGIN_ISLOGIN="isLogin";//true表示已登录，false表示未登录

    /**
     * 忘记密码
     */
    public static final int FORGETPWD_TAG=2;//用于判断是注册还是忘记密码，因为两个操作请求网络的参数不同
    public static final int FORGETPWD_REQUEST_SUCCESS=3;
    public static final int FORGETPWD_REQUEST_FAILURE=4;
    public static final String FORGETPWD_ACTION="ResetPassword";//固定ResetPassword

    /**
     * 主页
     */
    public static final int MAIN_VIEWPAGER_SUCCESS=1;
    public static final int MAIN_VIEWPAGER_FAILURE=2;
    public static final String MAIN_VIEWPAGER_ACTION="GetAdvertIcon";
    public static final int MAIN_NO_DEVICE=11;
    public static final int MAIN_NO_CONNECTED=12;

    //主页的开锁值直接用地锁详情里面的值

    /**
     * 搜索设备
     */
    public static final String SCAN_DEVICE_LIST="lockArraylist";

    /**
     * 地锁管理
     */
    public static final String LOCK_MANAGER_ISCONNECTED="lockManagerIsconnected";
    public static final String HASCONNECTED_LOCK_ARRAYLIST="hasConnectedLockArraylist";//之前连接过的地锁,暂时用treemap,因为Arraylist的device对象第一次取不到name
    public static final String HASCONNECTED_LOCK_TREEMAP="hasConnectedLockTreeMap";//之前连接过的地锁
    public static final String CURRENT_DEVICE_MAC="currentDeviceMac";//当前点击的设备mac地址
    public static final String CURRENT_DEVICE_SECRET="currentDeviceSecret";//当前点击的设备密码

    /**
     * 个人设置
     */
    public static final int PERSONAL_REQUEST_SUCCESS=1;
    public static final int PERSONAL_REQUEST_FAILURE=2;
    public static final String PERSONAL_ACTION="UpdateUserInfo";//固定UpdateUserInfo
    public static final String PERSONAL_ICON_KEY="Icon";//头像
    public static final String PERSONAL_NICKNAME_KEY="Name";//昵称
    public static final String PERSONAL_SEX_KEY="Sex";//性别
    public static final String PERSONAL_AGE_KEY="Birthday";//年龄
    public static final String PERSONAL_PHONE_KEY="Uid";//手机号

    /**
     * 地锁数据收发
     */
    public static final int SEND_DATA_FAILD=1;//发送数据失败
    public static final int RECEIVE_DATA_SUCCESS=2;//接收数据成功
    public static final int RECEIVE_DATA_FAILED=3;//接收数据失败

    /**
     * 地锁详情
     */
    public static final String LOCK_DETAILS_BATTERY="lockDetailsBattery";
    public static final int LOCK_NOT_CONNECT=4;//地锁未连接
    //查询
    public static final int QUERY_SEND_DATA_FAILD=1;//发送数据失败
    public static final int QUERY_RECEIVE_DATA_SUCCESS=2;//接收数据成功
    public static final int QUERY_RECEIVE_DATA_FAILED=3;//接收数据失败
    //修改密码
    public static final int CHANGE_SEND_DATA_FAILD=11;//发送数据失败
    public static final int CHANGE_RECEIVE_DATA_SUCCESS=12;//接收数据成功
    public static final int CHANGE_RECEIVE_DATA_FAILED=13;//接收数据失败
    //开锁
    public static final boolean OPEN_LOCK=true;//用于判断是开锁，因为开关锁命令相同
    public static final int OPEN_SEND_DATA_FAILD=21;//发送数据失败
    public static final int OPEN_RECEIVE_DATA_SUCCESS=22;//接收数据成功
    public static final int OPEN_RECEIVE_DATA_FAILED=23;//接收数据失败

    public static final int OPEN_STATUS_ISOPENED=24;//锁已在开启状态，无需开启
    public static final int OPEN_STATUS_ERROR=25;//锁状态异常
    public static final int OPEN_STATUS_RISING=26;//锁上升中
    public static final int OPEN_STATUS_DROPING=27;//锁下降中
    public static final int OPEN_STATUS_ISCLOSED=28;//锁是关闭状态
    //关锁
    public static final boolean CLOSE_LOCK=false;//用于判断是关锁，因为开关锁命令相同
    public static final int CLOSE_SEND_DATA_FAILD=31;//发送数据失败
    public static final int CLOSE_RECEIVE_DATA_SUCCESS=32;//接收数据成功
    public static final int CLOSE_RECEIVE_DATA_FAILED=33;//接收数据失败

    public static final int CLOSE_STATUS_ISOPENED=34;//锁已在开启状态，无需开启
    public static final int CLOSE_STATUS_ERROR=35;//锁状态异常
    public static final int CLOSE_STATUS_RISING=36;//锁上升中
    public static final int CLOSE_STATUS_DROPING=37;//锁下降中
    public static final int CLOSE_STATUS_ISCLOSED=38;//锁是关闭状态

    /**
     * 用于判断在哪个页面弹出的密码框，因为在设备列表页关闭密码框时要断开设备，而详情页修改密码时关闭不需要断开设备
     */
    public static final String IS_NEED_DISCONNECT="isNeedDisconnect";//
    public static final boolean NEED_DISCONNECT=true;//表示关闭密码框时需要断开设备
    public static final boolean NOT_DISCONNECT=false;//表示关闭密码框时不需要断开设备

    /**
     * 分享
     */
    public static final String SHARE_URL="http://www.pgyer.com/mpandroid";//链接指向的地址
    public static final String SHARE_IMAGE_URL="http://o1wh05aeh.qnssl.com/image/view/app_icons/18835991323ea4de04115eb2b2703947/120";//图标指向的地址，暂时指向蒲公英上的logo
    public static final int SHARE_SUCCESS=1;//分享成功
    public static final int SHARE_ERROR=2;//分享失败
    public static final int SHARE_CANCEL=3;//分享取消

}
