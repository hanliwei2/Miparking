package com.sziton.miparking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sziton.miparking.R;
import com.sziton.miparking.application.MyApplication;
import com.sziton.miparking.constants.Constants;
import com.sziton.miparking.custom.IconPopwindow;
import com.sziton.miparking.daoimpl.PersonalDaoImpl;
import com.sziton.miparking.db.MySharedPreferences;
import com.sziton.miparking.utils.Base64Convert;
import com.sziton.miparking.utils.DialogUtil;
import com.sziton.miparking.utils.EncryptUtil;
import com.sziton.miparking.utils.Paths;
import com.sziton.miparking.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fwj on 2017/11/29.
 */

public class PersonalActivity extends Activity implements View.OnClickListener{
    private RelativeLayout backRL;
    private RelativeLayout headRL;
    private RoundedImageView headIV;
    private RelativeLayout nickNameRL;
    private TextView nickNameContentTV;
    private RelativeLayout sexRL;
    private TextView sexContentTV;
    private RelativeLayout ageRL;
    private TextView ageContentTV;
    private RelativeLayout phoneRL;
    private TextView phoneContentTV;
    private TextView logoutTV;
    private MySharedPreferences mySharedPreferences;
    private IconPopwindow iconPopwindow;
    private String iconValue;
    private Dialog loadingDialog;

    //以下是头像
    /* 头像文件地址 */
    private String cameraPath =Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";

    /* 请求识别码 */
    private static final int CODE_GALLERY_REQUEST = 0xa0;//本地
    private static final int CODE_CAMERA_REQUEST = 0xa1;//拍照
    private static final int CODE_RESULT_REQUEST = 0xa2;//最终裁剪后的结果

    // 裁剪后图片的宽(X)和高(Y),480 X 480的正方形。
    private static int output_X = 600;
    private static int output_Y = 600;
    private File jpgFile;
    private Uri fileUri;
    private Uri uritempFile;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtil.closeDialog(loadingDialog);
            switch(msg.what){
                case Constants.PERSONAL_REQUEST_SUCCESS:
                    JSONObject jsonObject= (JSONObject) msg.obj;
                    try {
                        String success=jsonObject.getString("Success");
                        if(success.equals("True")){
                            //修改成功
                            mySharedPreferences.setStringValue(Constants.REGISTER_ICON_DEFAULT_KEY,iconValue);//头像
                            headIV.setImageBitmap(Base64Convert.base64ToBitmap(iconValue));

                            ToastUtil.shortToast(PersonalActivity.this,"修改成功！");
                        }else{
                            //修改失败
                            String errorMessage=jsonObject.getString("ErrorMessage");
                            ToastUtil.shortToast(PersonalActivity.this,errorMessage+"！");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.PERSONAL_REQUEST_FAILURE:
                    ToastUtil.shortToast(PersonalActivity.this,PersonalActivity.this.getResources().getString(R.string.internet_error_text));
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        initView();
        initData();
    }

    private void initView(){
        MyApplication.addActivity(this);
        backRL= (RelativeLayout) findViewById(R.id.personalBackRL);
        headRL= (RelativeLayout) findViewById(R.id.personalHeadRL);
        headIV= (RoundedImageView) findViewById(R.id.personalHeadIV);
        nickNameRL= (RelativeLayout) findViewById(R.id.personalNickNameRL);
        nickNameContentTV= (TextView) findViewById(R.id.personalNickNameContentTV);
        sexRL= (RelativeLayout) findViewById(R.id.personalSexRL);
        sexContentTV= (TextView) findViewById(R.id.personalSexContentTV);
        ageRL= (RelativeLayout) findViewById(R.id.personalAgeRL);
        ageContentTV= (TextView) findViewById(R.id.personalAgeContentTV);
        phoneRL= (RelativeLayout) findViewById(R.id.personalPhoneRL);
        phoneContentTV= (TextView) findViewById(R.id.personalPhoneContentTV);
        logoutTV= (TextView) findViewById(R.id.personalLogoutTV);

        backRL.setOnClickListener(this);
        headRL.setOnClickListener(this);
        nickNameRL.setOnClickListener(this);
        sexRL.setOnClickListener(this);
        ageRL.setOnClickListener(this);
        phoneRL.setOnClickListener(this);
        logoutTV.setOnClickListener(this);
    }

    private void initData(){
        mySharedPreferences=MySharedPreferences.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置头像
        String iconStr=mySharedPreferences.getStringValue(Constants.REGISTER_ICON_DEFAULT_KEY);
        if(!TextUtils.isEmpty(iconStr)){
            Bitmap iconBitmap= Base64Convert.base64ToBitmap(iconStr);
            headIV.setImageBitmap(iconBitmap);
        }
        //设置昵称
        String nickName=mySharedPreferences.getStringValue(Constants.REGISTER_NAME_DEFAULT_KEY);
        if(!TextUtils.isEmpty(nickName)){
            nickNameContentTV.setText(nickName);
        }
        //设置性别
        String sex=mySharedPreferences.getStringValue(Constants.REGISTER_SEX_DEFAULT_KEY);
        if(!TextUtils.isEmpty(sex)){
            sexContentTV.setText(sex);
        }
        //设置年龄
        String birthday=mySharedPreferences.getStringValue(Constants.REGISTER_BIRTHDAY_DEFAULT_KEY);
        if(!TextUtils.isEmpty(birthday)){
            ageContentTV.setText(birthday);
        }
        //设置电话
        String phone=mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);//这个带+86的，设置前吧+86去掉
        //暂时不去+86
        if(!TextUtils.isEmpty(phone)){
            phoneContentTV.setText(phone);
        }
/*        if(!TextUtils.isEmpty(phone)&&phone.length()>=11){
            phoneContentTV.setText(phone.substring(phone.length()-11,phone.length()));
        }*/
    }

    /**
     * 返回
     */
    private void handleBack(){
        finish();
    }

    /**
     * 头像
     */
    private void handleHead(){
        iconPopwindow=new IconPopwindow(PersonalActivity.this,itemsOnclick);
        //显示窗口
        iconPopwindow.showAtLocation(PersonalActivity.this.findViewById(R.id.personalLL), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
    }

    //头像popwindow中的打开相机方法
    private void handleCamera(){
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //根据时间创建文件名
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("'Miparking'_yyyyMMdd_HHmmssSSS"); // 格式化时间
        String filename = format.format(date) + ".jpg";

        // 判断存储卡是否可用，存储照片文件
        if (hasSdcard()) {
            File dir = new File(cameraPath);
            if (!dir.exists()) { // 如果目录不存在，则创建一个
                dir.mkdir();
            }
            jpgFile = new File(dir, filename);
            fileUri=Uri.fromFile(jpgFile);
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        }

        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
    }

    //头像popwindow中的打开相册方法
    private void handleAlbum(){
        Intent intentFromGallery = new Intent();
        // 设置文件类型
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        //如果你想在Activity中得到新打开Activity关闭后返回的数据，
        //你需要使用系统提供的startActivityForResult(Intent intent,int requestCode)方法打开新的Activity
        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
    }

    /**
     * 昵称
     */
    private void handleNickName(){
        Intent nickNameIntent=new Intent();
        nickNameIntent.setClass(this,NickNameActivity.class);
        startActivity(nickNameIntent);
    }

    /**
     * 性别
     */
    private void handleSex(){
        Intent sexIntent=new Intent();
        sexIntent.setClass(this,SexActivity.class);
        startActivity(sexIntent);
    }

    /**
     * 年龄
     */
    private void handleAge(){
        Intent ageIntent=new Intent();
        ageIntent.setClass(this,AgeActivity.class);
        startActivity(ageIntent);
    }

    /**
     * 手机号码,暂时不给修改
     */
/*    private void handlePhone(){
        Intent phoneIntent=new Intent();
        phoneIntent.setClass(this,PhoneActivity.class);
        startActivity(phoneIntent);
    }*/

    /**
     * 退出登录
     */
    private void handleLogout(){
        //暂时本地退出就好，服务器端没有提供退出接口
        mySharedPreferences.setBooleanValue(Constants.LOGIN_ISLOGIN,false);
        ToastUtil.shortToast(MyApplication.getInstance(),"退出登录成功！");
        Intent loginIntent=new Intent();
        loginIntent.setClass(this,LoginActivity.class);
        startActivity(loginIntent);
        MyApplication.clearActivity();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.personalBackRL:
                handleBack();
                break;

            case R.id.personalHeadRL:
                handleHead();
                break;

            case R.id.personalNickNameRL:
                handleNickName();
                break;

            case R.id.personalSexRL:
                handleSex();
                break;

            case R.id.personalAgeRL:
                handleAge();
                break;

            case R.id.personalPhoneRL:
                //handlePhone();
                break;

            case R.id.personalLogoutTV:
                handleLogout();
                break;
        }
    }

    //头像弹框的监听
    View.OnClickListener itemsOnclick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            iconPopwindow.dismiss();
            switch (v.getId()){
                case R.id.popwindowCameraTV:
                    handleCamera();
                    break;

                case R.id.popwindowAlbumTV:
                    handleAlbum();
                    break;

                default:
                    break;
            }
        }
    };

    //拍照和相册回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 用户没有进行有效的设置操作，返回
        if (resultCode == RESULT_CANCELED) {//取消
            return;
        }

        switch (requestCode) {
            case CODE_GALLERY_REQUEST://如果是来自本地的，即相册
                cropRawPhoto(data.getData());//直接裁剪图片
                break;

            case CODE_CAMERA_REQUEST://相机
                    /*File tempFile = new File(
                            Environment.getExternalStorageDirectory(),
                            IMAGE_FILE_NAME);*/
                    //拍完照更新相册，不然相册不同步的
                refreshGallery(jpgFile.getPath());
                //裁剪图片
                    cropRawPhoto(fileUri);
                break;

            case CODE_RESULT_REQUEST:
                /*f (data != null) {
                    postIcon(data);//上传图片到服务器，若成功，则设置头像并保存到本地，若失败，则不处理
                }*/
                //将Uri图片转换为Bitmap  
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
                    postIcon(bitmap);//上传图片到服务器，若成功，则设置头像并保存到本地，若失败，则不处理
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 裁剪原始的图片
     */
    public void cropRawPhoto(Uri uri) {
        if(uri.getScheme().equals("file")){
            uri=getFileUri(uri);
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        //把裁剪的数据填入里面

        // 设置裁剪
        intent.putExtra("crop", "true");

        // aspectX , aspectY :宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX , outputY : 裁剪图片宽高
        intent.putExtra("outputX", output_X);
        intent.putExtra("outputY", output_Y);
        //intent.putExtra("return-data", true);
        //uritempFile为全局变量，这里只能用全局变量的方式来传递裁剪过后的图片，因为onActivityResult（）中的intent，当图片较大时，有些手机会有限制，导致传过去的intent未null
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    /**
     *
     * @param uri  一个android文件的Uri地址一般如下：
     *	content://media/external/images/media/62026
     *
     *	但是，小米上是以file开头的，所以直接用data.getData();得到的uri来获取图片路径会获取不到，通过这个方法拼接下开头就好了
     * @return
     */
    public Uri getFileUri(Uri uri){
        if (uri.getScheme().equals("file")) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(")
                        .append(MediaStore.Images.ImageColumns.DATA)
                        .append("=")
                        .append("'" + path + "'")
                        .append(")");
                Cursor cur = cr.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur
                        .moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    //do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    /**
     * 提取保存裁剪之后的图片数据，上传到服务器
     */
    //private void postIcon(Intent intent) {
    private void postIcon(Bitmap photo) {
        /*Bundle extras = intent.getExtras();
        if (extras != null) {*/
            //Bitmap photo = extras.getParcelable("data");
            //裁剪成圆形暂时用不到，直接用roundedimageview，把图片设置进去直接就成圆的了
            //photo=getCircleBitmap(photo);
            String url= Paths.appUrl;
            //以下是接口需要的参数
            String timestamp= EncryptUtil.getTimestamp();
            String signatureNonce=EncryptUtil.getSignatureNonce();
            String action= Constants.PERSONAL_ACTION;

            String uid=mySharedPreferences.getStringValue(Constants.REGISTER_PHONE);
            String iconKey=Constants.PERSONAL_ICON_KEY;//map中头像的key
            iconValue=Base64Convert.Bitmap2StrByBase64(photo);//头像的值

            //通过签名算法得到的Signature
            Map<String,Object> paramsMap=new HashMap<>();
            paramsMap.put("Timestamp",timestamp);
            paramsMap.put("SignatureNonce",signatureNonce);
            paramsMap.put("Action",action);
            paramsMap.put("Uid",uid);
            paramsMap.put(iconKey,iconValue);
            String signature=EncryptUtil.getSignature(paramsMap);

            if(TextUtils.isEmpty(iconValue)){
                //不符合要求,return掉
                ToastUtil.shortToast(this,"头像不能为空！");
                return;
            }else{
                //符合要求，提交服务器
                loadingDialog= DialogUtil.createLoadingDialog(this);
                PersonalDaoImpl personalDaoImpl=new PersonalDaoImpl();
                personalDaoImpl.postPersonal(url,signature,timestamp,signatureNonce,action,uid,iconKey,iconValue,mHandler);
            }

        //}
    }

    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 有存储的SDCard
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存完视频后刷新相册
     */
    private void refreshGallery(String file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(file)));
        this.sendBroadcast(mediaScanIntent);
    }

    /**
    *  裁剪成圆形图片暂时用不到，直接用roundedimageview就好了
     * 生成透明背景的圆形图片,！注意要生成透明背景的圆形，图片一定要png类型的，不能是jpg类型
     *
     * @param bitmap
     * @return
     */
/*    public Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = 0.0f;
            // 以较短的边为标准
            if (bitmap.getWidth() > bitmap.getHeight()) {
                roundPx = bitmap.getHeight() / 2.0f;
            } else {
                roundPx = bitmap.getWidth() / 2.0f;
            }
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }*/

}
