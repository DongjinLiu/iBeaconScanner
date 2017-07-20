package com.example.jin.ibeacontest;

import java.util.List;

/**
 * Created by jin on 2017/7/19.
 *
 * 滤波算法
 * 处理一段时间内某个iBeacon基站在某点的RSSI值
 */

public class FitRssi {

    public static String FitRssiData(List<String> rssiList){
        String answer="";

        int averageRssi=0;
        for (String string:rssiList){
            averageRssi+=Integer.valueOf(string);//求平均值
        }
        if (rssiList.size()!=0){
            answer=String.valueOf(averageRssi/rssiList.size());
        }
        return answer;
    }
}
