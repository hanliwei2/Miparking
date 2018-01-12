package com.sziton.miparking.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.search.SearchResult;
import com.sziton.miparking.bean.MyDeviceBean;
import com.sziton.miparking.constants.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by fwj on 2017/8/14.
 */

public class MySharedPreferences {
    private static MySharedPreferences instance;
    private static SharedPreferences sharedPreferences;
    private Context mcontext;
    private static final String SP_NAME="locstar";
    private static SharedPreferences.Editor spEdit;
    private String defaultString="";
    private boolean defaultBoolean=false;
    private int defaultInt=0;
    private float defaultFloat=0F;
    private Set<String> defaultSet=null;
    private TreeSet<Object> defaultTreeSet=null;
    private ArrayList<SearchResult> defaultArrayList=null;
    private TreeMap<String,MyDeviceBean> defaultTreeMap=null;

    /**
     *构造方法
     */
    public MySharedPreferences(Context mcontext){
        this.mcontext=mcontext;
        sharedPreferences=mcontext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        spEdit=sharedPreferences.edit();
    }

    /**
     *获取单例
     */
    public static MySharedPreferences getInstance(Context mcontext){
        if(instance==null){
            instance=new MySharedPreferences(mcontext);
        }
        return instance;
    }

    /**
     * String
     */
    public void setStringValue(String key,String value){
        if(spEdit!=null){
            spEdit.putString(key,value);
            spEdit.commit();
        }
    }

    public String getStringValue(String key){
        if(sharedPreferences!=null){
            return sharedPreferences.getString(key,defaultString);
        }
        return defaultString;
    }

    /**
     *boolean
     */
    public void setBooleanValue(String key,boolean value){
        if(spEdit!=null){
            spEdit.putBoolean(key,value);
            spEdit.commit();
        }
    }

    public boolean getBooleanValue(String key){
        if(sharedPreferences!=null){
            return sharedPreferences.getBoolean(key,defaultBoolean);
        }
        return defaultBoolean;
    }

    /**
     * int
     */
    public void setIntValue(String key,int value){
        if(spEdit!=null){
            spEdit.putInt(key,value);
            spEdit.commit();
        }
    }


    public int getIntValue(String key){
        if(sharedPreferences!=null){
            return sharedPreferences.getInt(key,defaultInt);
        }
        return defaultInt;
    }

    /**
     * float
     */
    public void setFloatValue(String key,float value){
        if(spEdit!=null){
            spEdit.putFloat(key,value);
            spEdit.commit();
        }
    }

    public float getFloatValue(String key){
        if(sharedPreferences!=null){
            return sharedPreferences.getFloat(key,defaultFloat);
        }
        return defaultFloat;
    }

    /**
     * TreeSet
     * 有序，不可重复
     */
    public void setTreeSet(String key, TreeSet<Object> value){
        if(spEdit!=null&&value!=null&&value.size()>0){
            TreeSet<String> strTreeSet=new TreeSet<>();
            for(Object object:value){
                String objectStr=object2String(object);
                strTreeSet.add(objectStr);
            }
            spEdit.putStringSet(key,strTreeSet);
            spEdit.commit();
        }
    }

    public TreeSet<Object> getTreeSet(String key){
        if(sharedPreferences!=null){
            TreeSet<Object> objectTreeSet=new TreeSet<>();
            Set<String> strSet= sharedPreferences.getStringSet(key,defaultSet);
            if(strSet!=null&&strSet.size()>0){
                //String类型的set转object类型的treeset
                for(String str:strSet){
                    objectTreeSet.add(string2Object(str));
                }
                return objectTreeSet;
            }else{
                return defaultTreeSet;
            }
        }
        return defaultTreeSet;
    }

    /**
     * SearchResult类型的
     * arraylist
     * 转成Json保存
     */
    public void setArraylist(String key, ArrayList<SearchResult> value){
        if(spEdit!=null){
            Gson gson=new Gson();
            String jsonStr=gson.toJson(value);
            spEdit.putString(key,jsonStr);
            spEdit.commit();
        }
    }

    public ArrayList<SearchResult> getArraylist(String key){
        if(sharedPreferences!=null){
            String jsonStr=sharedPreferences.getString(key,defaultString);
            if(jsonStr!=null){
                Gson gson=new Gson();
                Type type=new TypeToken<ArrayList<SearchResult>>(){}.getType();
                ArrayList<SearchResult> arraylist=gson.fromJson(jsonStr,type);
                return arraylist;
            }
        }
        return defaultArrayList;
    }

    /**
     * SearchResult类型的
     * treemap,key是device的name,value是device的address
     * 转成Json保存
     */
    public void setTreeMap(String key, TreeMap<String,MyDeviceBean> value){
        if(spEdit!=null){
            Gson gson=new Gson();
            String jsonStr=gson.toJson(value);
            spEdit.putString(key,jsonStr);
            spEdit.commit();
        }
    }

    public TreeMap<String,MyDeviceBean> getTreeMap(String key){
        if(sharedPreferences!=null){
            String jsonStr=sharedPreferences.getString(key,defaultString);
            if(jsonStr!=null){
                Gson gson=new Gson();
                Type type=new TypeToken<TreeMap<String,MyDeviceBean>>(){}.getType();
                TreeMap<String,MyDeviceBean> treeMap=gson.fromJson(jsonStr,type);
                return treeMap;
            }
        }
        return defaultTreeMap;
    }

    /**
     * 清空sp
     */
    public void clearSp(){
        if(spEdit!=null){
            spEdit.clear();
            spEdit.commit();
        }
    }

    /**
     * 清除区号
     */
    public void removeCountry(){
        if(spEdit!=null){
            spEdit.remove(Constants.COUNTRY_ID);
            spEdit.commit();
        }
    }

    /**
     * sp中存储的set,里面的类型只能是String,不能是object，可以用这个方法转成String（该object类必须implements Serializable ，否则会报错）
     * writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
     * 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
     *
     * @param object 待加密的转换为String的对象
     * @return String   加密后的String
     */
    private String object2String(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            String string = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            objectOutputStream.close();
            return string;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * sp中取set,因为set中只能存string，所以要把string转成object再取出来
     * 使用Base64解密String，返回Object对象
     *
     * @param objectString 待解密的String
     * @return object      解密后的object
     */
    private Object string2Object(String objectString) {
        byte[] mobileBytes = Base64.decode(objectString.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
