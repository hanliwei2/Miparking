package com.sziton.miparking.bluetoothkit;

import java.util.Arrays;

/**
 * Created by fwj on 2017/12/5.
 */

public class SubpackageManager {

    /**
     * 分包发送数据
     * @param sendData 分包后的数据
     * @param times 第几个包，总共是4个包，分别是1，2，3，4
     * @return
     */
    public static byte[] sendSubData(byte[] sendData,int times){
        byte[] subData = new byte[20];
        switch(times){
            case 1:
                subData= Arrays.copyOfRange(sendData,0,20);
                break;
            case 2:
                subData=Arrays.copyOfRange(sendData,20,40);
                break;
            case 3:
                subData=Arrays.copyOfRange(sendData,40,60);
                break;
            case 4:
                for(int i=0;i<20;i++){
                    if(i<4){
                        subData[i]=sendData[60+i];
                    }else {
                        subData[i]=0;
                    }
                }
                break;
        }
        return subData;
    }
/*    public static int tmpLen;
    public static int start;
    public static int end;
    public static int everyLength=20;

    //分包发送数据,发送的第一个包
    public static byte[] sendDataFirst(final byte[] data){
        byte[] sendData = new byte[everyLength+1];
        tmpLen = data.length;
        start=0;
        end=0;
        //tmpLen
        Log.i("tag","tmpLen-->>"+tmpLen+"");
        if(tmpLen>0){
            if(tmpLen>=everyLength){
                end+=everyLength;
                sendData= Arrays.copyOfRange(data,start,end);
                start+=everyLength;
                tmpLen-=everyLength;
                //
                Log.i("tag","end-->>"+end+"");
                String logData="";
                for(int i=0;i<sendData.length;i++){
                    int intData=sendData[i]&0xFF;
                    logData=logData+intData+" ";
                }
                Log.i("tag","sendDataMore1 sendData-->>"+logData);
                Log.i("tag","start-->>"+start+"");
                Log.i("tag","tmpLen-->>"+tmpLen+"");
            }else{
                end+=tmpLen;
                sendData=Arrays.copyOfRange(data,start,end);
                tmpLen=0;
            }
        }
        return sendData;
    }

    //分包发送数据,发送的后续的包
    public void sendDataMore(final byte[] data){
        Log.i("tag","sendDataMore tmpLen-->>"+tmpLen+"");
        Log.i("tag","sendDataMore everyLength-->>"+everyLength+"");
        if(tmpLen>0){
            byte[] sendData = new byte[everyLength+1];
            if(tmpLen>=everyLength){
                end+=everyLength;
                sendData= Arrays.copyOfRange(data,start,end);
                start+=everyLength;
                tmpLen-=everyLength;

                Log.i("tag","sendDataMore end-->>"+end+"");
                String logData="";
                for(int i=0;i<sendData.length;i++){
                    int intData=sendData[i]&0xFF;
                    logData=logData+intData+" ";
                }
                Log.i("tag","sendDataMore1 sendData-->>"+logData);
                Log.i("tag","sendDataMore start-->>"+start+"");
                Log.i("tag","sendDataMore tmpLen-->>"+tmpLen+"");

            }else{
                end+=tmpLen;
                sendData=Arrays.copyOfRange(data,start,end);
                tmpLen=0;
                Log.i("tag","sendDataMore1 end-->>"+end+"");
                String logData="";
                for(int i=0;i<sendData.length;i++){
                    int intData=sendData[i]&0xFF;
                    logData=logData+intData+" ";
                }
                Log.i("tag","sendDataMore1 sendData-->>"+logData);
                Log.i("tag","sendDataMore1 start-->>"+start+"");
                Log.i("tag","sendDataMore1 tmpLen-->>"+tmpLen+"");
            }

        }else{

        }
    }*/
}
