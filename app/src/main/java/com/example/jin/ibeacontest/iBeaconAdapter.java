package com.example.jin.ibeacontest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by jin on 2017/7/15.
 * 自定义的iBeacon内容适配器
 * 为ListView显示做准备
 */

public class iBeaconAdapter extends ArrayAdapter<iBeacon> {

    private int resourceId;

    public iBeaconAdapter(Context context,int textViewResourceId,List<iBeacon> objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        iBeacon ibeacon=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

        TextView iBeaconName=(TextView)view.findViewById(R.id.ibeacon_name);
        TextView iBeaconAddress=(TextView)view.findViewById(R.id.ibeacon_address);
        TextView iBeaconUuid=(TextView)view.findViewById(R.id.ibeacon_uuid);
        TextView iBeaconMajor=(TextView)view.findViewById(R.id.ibeacon_major);
        TextView iBeaconMinor=(TextView)view.findViewById(R.id.ibeacon_minor);
        TextView iBeaconTxpower=(TextView)view.findViewById(R.id.ibeacon_txpower);
        TextView iBeaconRssi=(TextView)view.findViewById(R.id.ibeacon_rssi);
        TextView iBeaconDistance=(TextView)view.findViewById(R.id.ibeacon_distance);

        iBeaconName.setText("Name:"+ibeacon.getName());
        iBeaconAddress.setText("Mac:"+ibeacon.getBluetoothAddress());
        iBeaconUuid.setText("UUID:"+ibeacon.getProximityUuid());
        //注意这里必须是String类型，纯为int会报错，参考：http://blog.csdn.net/jason0539/article/details/11699647
        iBeaconMajor.setText("Major："+ibeacon.getMajor());
        iBeaconMinor.setText("Minor："+ibeacon.getMinor());
        iBeaconTxpower.setText("TXPower:"+ibeacon.getTxPower());
        iBeaconRssi.setText("RSSI:"+ibeacon.getRssi());
        iBeaconDistance.setText("Distance:"+ibeacon.getDistance());

        return view;
    }
}
