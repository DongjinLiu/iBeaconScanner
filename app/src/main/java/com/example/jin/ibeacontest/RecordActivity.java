/**
 * RecordActivity
 *
 * 扫描某个位置的iBeacon指纹信息
 * 取一段时间内各个iBeacon的RSSI的平均值
 * 将该位置的坐标信息和该点的指纹信息发送到服务器后台
 *
 * @author jin
 * Data: 2017/7/16
 */
package com.example.jin.ibeacontest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.example.jin.ibeacontest.MainActivity.mBluetoothAdapter;
import static com.example.jin.ibeacontest.iBeaconClass.CalculateDistance;

public class RecordActivity extends AppCompatActivity implements Runnable{

    final private String TAG="RecordActivity";

    private Handler mHandler=new Handler();//扫描时使用
    private boolean isScaned=false;
    // Stops scanning after 5 s.
    //默认扫描时间
    private static long SCAN_PERIOD = 5000;

    private List<iBeacon> mIBeaconList=new ArrayList<>();//所有扫描到的iBeacon信息，一个iBeacon对应多条数据
    private List<iBeacon> answerIBeaconList=new ArrayList<>();//处理后的iBeacon信息，一个iBeacon对应一条数据

    //该点的位置信息
    EditText mBulidingNumberEditText;
    EditText mFloorEditText;
    EditText mCoordinateXEditText;
    EditText mCoordinateYEditText;
    EditText mScanTime;



    Button mSaveButton;
    Button mClearButton;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        //Connector.getDatabase();//创建数据库

        mBulidingNumberEditText=(EditText)findViewById(R.id.edit_building_number);
        mFloorEditText=(EditText)findViewById(R.id.edit_floor);
        mCoordinateXEditText=(EditText)findViewById(R.id.edit_coordinate_x);
        mCoordinateYEditText=(EditText)findViewById(R.id.edit_coordinate_y);
        mScanTime=(EditText)findViewById(R.id.edit_scan_time);

        mSaveButton=(Button)findViewById(R.id.button_save);
        mClearButton=(Button)findViewById(R.id.button_clear);

        //保存位置数据，并开始扫描
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {

                CloseInputMethodManager();//隐藏键盘

                SCAN_PERIOD=Integer.valueOf(mScanTime.getText().toString())*1000;
                Log.d(TAG, "onClick: Start Scan.");
                scanLeDevice(true);//开始扫描iBeacon，持续 一段时间.
                //Toast.makeText(RecordActivity.this,"Wait "+SCAN_PERIOD/1000+" second./n采集数据中······",Toast.LENGTH_SHORT).show();

                progressDialog=new ProgressDialog(RecordActivity.this);
                progressDialog.setTitle("Sacnning......");
                progressDialog.setMessage("Wait "+SCAN_PERIOD/1000+" second");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Thread thread = new Thread(RecordActivity.this);//开启一个线程来延时
                thread.start();//启动线程

            }
        });

        //清空列表和EditText数据，为下一个点的记录做准备
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CloseInputMethodManager();//隐藏键盘

                mIBeaconList.clear();
                answerIBeaconList.clear();

//                mBulidingNumberEditText.setText("");//不清空BuilderNumber和Floor信息
//                mFloorEditText.setText("");

                mCoordinateXEditText.setText("");
                mCoordinateYEditText.setText("");

                mSaveButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.GONE);

                //清空适配器的内容
                iBeaconAdapter adapterClear=new iBeaconAdapter(RecordActivity.this,R.layout.ibeacon_item,answerIBeaconList);
                ListView mListView=(ListView)findViewById(R.id.list_view);
                mListView.setAdapter(adapterClear);
            }
        });
    }

    /**
     * 若键盘显示则隐藏虚拟键盘
     *
     * @author jin
     * Data:2017/7/18
     */
    public void CloseInputMethodManager(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (isOpen){
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(RecordActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        }
    }

    /**
     * run的实现
     */
    public void run() {
        try {
            Thread.sleep(SCAN_PERIOD);//睡一段时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(0);//睡醒来了，传送消息，扫描完成
    }

    //定义处理消息的对象
    private Handler handler = new Handler(){
        /**
         * 处理消息
         * @param msg
         */
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0://扫描完成
                    progressDialog.setTitle("Upload data...");
                    progressDialog.setMessage("Wait some seconds.");
                    //Toast.makeText(RecordActivity.this, "对话框就消失了", Toast.LENGTH_SHORT).show();
                    break;
                case 1://扫描结束，更新iBeaconAdapter
                    //Toast.makeText(RecordActivity.this,String.valueOf(answerIBeaconList.size()),Toast.LENGTH_SHORT).show();
                    Display(answerIBeaconList);
                    break;
                case 2://上传数据成功
                    progressDialog.dismiss();
                    Toast.makeText(RecordActivity.this,"Upload Success!",Toast.LENGTH_SHORT).show();//广播上传成功
                    break;
                case 3://上传数据失败
                    progressDialog.dismiss();
                    Toast.makeText(RecordActivity.this,"Upload Fail! "+msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                    isScaned=true;
                    Log.d(TAG, "run: Scaned Over."+isScaned);

                    //采集完成：隐藏Save按钮，显示Cancel按钮
                    mSaveButton.setVisibility(View.GONE);
                    mClearButton.setVisibility(View.VISIBLE);

                    //处理、保存、上传扫描信息
                    ProcessData();

                }
            }, SCAN_PERIOD);
            isScaned=false;
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
                    if(ibeacon != null&&ibeacon.minor!=0) {
                        mIBeaconList.add(ibeacon);
                        Log.d(TAG, "run: Add iBeacon UUID:"+ibeacon.getProximityUuid());
                    }
                }
            });
        }
    };

    /**
     * 将iBeacon的List显示在List列表中
     * @author jin
     * Data:2017/7/15
     */
    public void Display(List<iBeacon> mArrayiBeacon) {
        if (mArrayiBeacon.size() != 0) {
            //排序
            //将iBeacon列表中按UUID从小到大排序
            iBeacon temp = new iBeacon();
            for (int i = 0; i < mArrayiBeacon.size() - 1; i++) {
                for (int j = 0; j < mArrayiBeacon.size() - i - 1; j++) {
                    if (mArrayiBeacon.get(j).getProximityUuid().compareTo(mArrayiBeacon.get(j + 1).getProximityUuid()) > 0) {
                        temp = mArrayiBeacon.get(j);
                        mArrayiBeacon.set(j, mArrayiBeacon.get(j + 1));
                        mArrayiBeacon.set(j + 1, temp);
                    }
                }
            }
            Log.d(TAG, "Display: iBeacon Count:" + mArrayiBeacon.size());

            //显示
            iBeaconAdapter adapter = new iBeaconAdapter(RecordActivity.this, R.layout.ibeacon_item, mArrayiBeacon);
            ListView mListView = (ListView) findViewById(R.id.list_view);
            mListView.setAdapter(adapter);
        }
    }

    /**
     * 处理、保存、上传扫描信息
     *
     * @author jin
     * Data:2017/7/17
     */
    private void ProcessData(){
        int count=0;//记录扫描到iBeacon的数量

        //扫描到几个iBeacon（MAC地址不同），就置入几个
        // iBeacons类继承于iBeacon类，保存了一个iBeacon在一段时间内所有的RSSI信息
        List<iBeacons> iBeaconsList=new ArrayList<>();

        //所有扫描到的iBeacon信号的MAC地址都置入该List，扫描到几次就存入几次
        List<String> iBeaconMacList=new ArrayList<>();

        //遍历所有接收到的信号信息
        for(iBeacon item:mIBeaconList){
            Log.d(TAG, "run: item mac:"+item.getBluetoothAddress());
            //统计iBeacon个数
            if (iBeaconMacList.indexOf(item.getBluetoothAddress())==-1){
                //首次在该位置接收到某iBeacon信号
                count++;
                iBeaconMacList.add(item.getBluetoothAddress());
                iBeaconsList.add(new iBeacons(item));
            }else{//非首次在该位置接收到某iBeacon信号
                //遍历iBeacons类的List
                for(int i=0;i<iBeaconsList.size();i++){
                    //找到已存在的对象
                    if (item.getBluetoothAddress().equals(iBeaconsList.get(i).getBluetoothAddress())){
                        //向已存在的对象中添加RSSI数据
                        iBeaconsList.get(i).getRssiList().add(String.valueOf(item.getRssi()));
                    }
                }
            }
        }
        Log.d(TAG, "run: count="+count);
        Log.d(TAG, "run: iBeaconsList size="+iBeaconsList.size());

        //处理iBeacons类，将处理结果置入answerIBeaconList
        int averageRssi=0;
        for (iBeacons ibeacons:iBeaconsList){
            Log.d(TAG, "run: iBeacons MAC"+ibeacons.getBluetoothAddress());
            for(String string:ibeacons.getRssiList()){
                Log.d(TAG, "run: iBeacons Rssi"+string);
                //对接收到某iBeacon在一段时间内在该点的RSSI信号的平均值
                averageRssi+=Integer.valueOf(string);
            }
            //除数为0检查
            if (ibeacons.getRssiList().size()!=0){
                //处理完成，置入answerIBeaconList
                Log.d(TAG, "run: iBeacons RssiList size:"+ibeacons.getRssiList().size());
                answerIBeaconList.add(new iBeacon(ibeacons,averageRssi/ibeacons.getRssiList().size()));
            }else {
                Toast.makeText(RecordActivity.this,"Nothing be scaned.",Toast.LENGTH_SHORT).show();
            }
            averageRssi=0;
        }

        //异步更新UI
        //在后台线程中直接修改适配器是不允许的（ListView不能接受到通知），必须在UI线程中修改适配器
        handler.sendEmptyMessage(1);

        //将处理后的结果置入Place对象，发送给服务器数据库
        Place place=new Place();
        place.setBulidingNumber(mBulidingNumberEditText.getText().toString());
        place.setFloor(mFloorEditText.getText().toString());
        place.setCoordinateX(mCoordinateXEditText.getText().toString());
        place.setCoordinateY(mCoordinateYEditText.getText().toString());
        place.setIBeaconList(answerIBeaconList);
        place.setDate(new Date(System.currentTimeMillis()));

        Log.d("SendDatabase", "run: "+place.toString());
        try {
            //这里Place先不存数据库，直接上传
            //存数据库发生了IBeaconList丢失的情况，
            // 在数据库取出的Place中再次获取IBeaconList的时候返回了null
            SendDatebase(place);
        } catch (Exception e) {
            Toast.makeText(RecordActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //place.save();
        //Toast.makeText(RecordActivity.this,"Save Successful",Toast.LENGTH_SHORT).show();

        //Log.d(TAG, "TraversalDatebase: List size is "+answerIBeaconList.size());
        Log.d(TAG, "TraversalDatebase: Place List size is "+place.getIBeaconList().size());
    }


    /**
     * 将Place数据上传至服务器后台
     * @param place
     * @author fang
     * Data:2017/7/16
     */
    protected  static  String IP = "192.168.1.107";//服务器IP
    private static final String URL = "http://"+ IP +":80/post/addPlace";

    public void SendDatebase(final Place place) {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //执行耗时操作
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("recordData", place.toString())
                            .build();
                    Request request = new Request.Builder()
                            .url(URL)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    Log.d("SendDatabase", "run: fuuuuuuuuuck   " + response);
                    if (response.isSuccessful()){
                        handler.sendEmptyMessage(2);//广播上传成功消息
                        //Toast.makeText(RecordActivity.this,"Success!",Toast.LENGTH_SHORT).show();//广播上传成功
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg=new Message();
                    msg.what=3;
                    msg.obj="Can't connect to the sever.";
                    handler.sendMessage(msg);//广播上传失败消息以及错误信息
                    Log.d("SendDatabase Fail ", e.getMessage());
                }
            }
        };
        new Thread() {
            public void run() {
                Looper.prepare();
                new Handler().post(runnable);//在子线程中直接去new 一个handler
                Looper.loop();//这种情况下，Runnable对象是运行在子线程中的，可以进行联网操作，但是不能更新UI
            }
        }.start();
    }
}