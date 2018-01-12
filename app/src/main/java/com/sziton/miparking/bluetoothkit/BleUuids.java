package com.sziton.miparking.bluetoothkit;

import android.os.ParcelUuid;

import java.util.UUID;

/**
 * Created by fwj on 2017/12/5.
 */

public class BleUuids {
    //string类型
    public static String CLIENT_DEVICE_CONFIG = "0000fee7-0000-1000-8000-00805f9b34fb";//设备的uuid
    public static String CLIENT_SERVICE_CONFIG ="0000ff00-0000-1000-8000-00805f9b34fb";//service的uuid
    public static String CLIENT_CHARACTERISTIC_CONFIG = "0000ff01-0000-1000-8000-00805f9b34fb";//Characteristic的uuid
    public static String CLIENT_CHARACTERISTIC_READ_CONFIG = "0000ff02-0000-1000-8000-00805f9b34fb";//读的Characteristic的uuid
    public static String CLIENT_DESCRIPTION_CONFIG="0000ff02-0000-1000-8000-00805f9b34fb";//开启通道的uuid
    //uuid类型
    public static ParcelUuid UUID_DEVICE=new ParcelUuid(UUID.fromString(CLIENT_DEVICE_CONFIG));//设备的uuid
    public static UUID[] deviceUuid=new UUID[]{UUID.fromString(CLIENT_DEVICE_CONFIG)};//设备的uuid
    public static UUID UUID_CLIENT_SERVICE_CONFIG = UUID.fromString(CLIENT_SERVICE_CONFIG);//service的uuid
    public static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG);//CHARACTERISTIC的uuid
    public static UUID UUID_CLIENT_CHARACTERISTIC_READ_CONFIG = UUID.fromString(CLIENT_CHARACTERISTIC_READ_CONFIG);//读的CHARACTERISTIC的uuid
    public static UUID UUID_CLIENT_CHARACTERISTIC_NOTIFICATION_CONFIG = UUID.fromString(CLIENT_DESCRIPTION_CONFIG);//开启通知通道的uuid
}
