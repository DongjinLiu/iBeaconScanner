/**
 * iBeaconTest测试Project
 * ScanActivity
 * 接收并显示所有接收的到iBeacon信息
 *
 * @author jin
 * @date 2017/7/15
 */
package com.example.jin.ibeacontest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.jin.ibeacontest.MainActivity.mBluetoothAdapter;
import static com.example.jin.ibeacontest.iBeaconClass.CalculateDistance;


public class ScanActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private List<iBeacon> mArrayiBeacon=new ArrayList<>();//iBeacon列表
    final String TAG="ScanActivity";

    SwipeRefreshLayout mSwipeLayout;
    iBeaconAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Log.d(TAG, "onCreate: Start Scan!");
        //开始扫描
        //新开一个线程进行扫描，一直扫描知道该Activity的生存周期发生改变
        scanLeDevice(true);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setOnRefreshListener(ScanActivity.this);

    }

    public void onRefresh()
    {
        // Log.e("xxx", Thread.currentThread().getName());
        // UI Thread

        handeler.sendEmptyMessage(2);

    }

    protected Handler handeler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Display(mArrayiBeacon);
                    break;
                case 2:
                    scanLeDevice(false);//停止扫描
                    mArrayiBeacon.clear();//清空列表
                    //Display(mArrayiBeacon);
                    adapter.notifyDataSetChanged();
                    scanLeDevice(true);//重新开始扫描
                    mSwipeLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };

    //region 活动生存周期开始/停止扫描
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onStart() {
        super.onStart();
        mArrayiBeacon.clear();
        scanLeDevice(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        mArrayiBeacon.clear();
        scanLeDevice(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onRestart() {
        super.onRestart();
        mArrayiBeacon.clear();
        scanLeDevice(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onStop() {
        super.onStop();
        scanLeDevice(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
    }
    //endregion

    /**
     * 扫描
     * 新开线程
     * @param enable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable)
    {
        if (enable)//enable =true就是说要开始扫描
        {
            // Stops scanning after a pre-defined scan period.
            // 下边的代码是为了在 SCAN_PERIOD （以毫秒位单位）后，停止扫描的
            // 如果需要不停的扫描，可以注释掉
//            mHandler.postDelayed(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    // 这个是重置menu，将 menu中的停止按钮->扫描按钮
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);//这句就是开始扫描了
        }
        else
        {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//这句就是停止扫描
        }
        // 这个是重置menu，将 menu中的扫描按钮->停止按钮
        invalidateOptionsMenu();
    }

    /**
     * Device scan callback.
     * 回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            final iBeacon ibeacon = iBeaconClass.fromScanData(device,rssi,scanRecord);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: add ibeacon"+mArrayiBeacon.size());
                    if(ibeacon != null&&ibeacon.minor!=0) {//扫描到有效信息
                        mArrayiBeacon.remove(IsExist(ibeacon));
                        ibeacon.setDistance(CalculateDistance(ibeacon.getTxPower(),ibeacon.getRssi()));
                        mArrayiBeacon.add(ibeacon);
                        Log.d(TAG, "run: add ibeacon"+mArrayiBeacon.size());
                        Log.d(TAG, "run: iBeacon MAC:"+ibeacon.getBluetoothAddress()+"RSSI:"+ibeacon.getRssi());
                        //Display(mArrayiBeacon);
                        handeler.sendEmptyMessage(0);
                    }
                }
            });
        }
    };

    /**
     * 显示在List列表中
     * @author jin
     * Data:2017/7/15
     */
    public void Display(List<iBeacon> mArrayiBeacon){
        if (mArrayiBeacon.size()!=0){
            //排序
            //将iBeacon列表中按UUID从小到大排序
            iBeacon temp=new iBeacon();
            for(int i=0;i<mArrayiBeacon.size()-1;i++){
                for(int j=0;j<mArrayiBeacon.size()-i-1;j++){
                    if (mArrayiBeacon.get(j).getProximityUuid().compareTo(mArrayiBeacon.get(j+1).getProximityUuid())>0){
                        temp=mArrayiBeacon.get(j);
                        mArrayiBeacon.set(j,mArrayiBeacon.get(j+1));
                        mArrayiBeacon.set(j+1,temp);
                    }
                }
            }
            Log.d(TAG, "Display: iBeacon Count:"+mArrayiBeacon.size());
        }

        Log.d(TAG, "Display: iBeacon Count:"+mArrayiBeacon.size());
        //显示
        adapter=new iBeaconAdapter(ScanActivity.this,R.layout.ibeacon_item,mArrayiBeacon);
        ListView mListView=(ListView)findViewById(R.id.list_view);
        mListView.setAdapter(adapter);

    }

    /**
     * 判断该iBeacon是否为第一次接收到信息
     * @param ibeacon
     * @return 已存在的object
     * @author jin
     * Data:2017/7/15
     */
    private iBeacon IsExist(iBeacon ibeacon){
        iBeacon sameibeacon=new iBeacon();
        for(iBeacon item:mArrayiBeacon){
            if (item.getProximityUuid().equals(ibeacon.getProximityUuid())&&
                    item.getMajor()==ibeacon.getMajor()&&item.getMinor()==ibeacon.getMinor()){
                sameibeacon=item;
                Log.d(TAG, "IsExist!!!");
                break;
            }
        }
        return sameibeacon;
    }
}
