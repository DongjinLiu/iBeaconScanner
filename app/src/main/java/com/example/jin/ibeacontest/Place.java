package com.example.jin.ibeacontest;

import android.text.Editable;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by jin on 2017/7/16.
 *
 * 最后将该类的数据存入数据库
 * 将该类的信息上传到服务器后台
 *
 * mIBeaconList保存数据失败
 * 读取时为Null
 *
 * 取消数据库
 * Sqlite数据库不能保存List对象
 * Place对象不保存到本地数据库中，直接发送给服务器数据库
 */

public class Place implements Serializable{

    private String bulidingNumber;
    private String floor;
    private String coordinateX;
    private String coordinateY;
    private Date date;
    private List<iBeacon> mIBeaconList;//SqLite数据库不能保存List对象

    public String getBulidingNumber() {
        return bulidingNumber;
    }

    public void setBulidingNumber(String bulidingNumber) {
        this.bulidingNumber = bulidingNumber;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(String coordinateX) {
        this.coordinateX = coordinateX;
    }

    public String getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(String coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<iBeacon> getIBeaconList() {
        return mIBeaconList;
    }

    public void setIBeaconList(List<iBeacon> IBeaconList) {
        mIBeaconList = IBeaconList;
    }

    /**
     * 重载toString()方法
     * 为数据传送做准备
     */
    @Override
    public  String toString(){
        String placeStr;
        placeStr = bulidingNumber+"*";
        placeStr += floor+"*";
        placeStr += coordinateX+"*";
        placeStr += coordinateY+"*";
        placeStr += date.getTime()+"*";
        if (!mIBeaconList.isEmpty()) {
            for (iBeacon ibeacon : mIBeaconList) {
                placeStr += ibeacon.toString() + "*";
            }
        }
        return  placeStr;
    }
}
