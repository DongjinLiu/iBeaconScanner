/**
 * SettingActivity
 * 设置
 * 修改传送数据的IP等
 *
 * 需改进的地方：
 * 修改数据后不能存入，不能持久化修改，只能在本次启动中有效
 *
 * @author jin
 * data 2017/7/17
 */
package com.example.jin.ibeacontest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.jin.ibeacontest.FileUtilIp.saveToFile;
import static com.example.jin.ibeacontest.MainActivity.IP;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final EditText mEditTextIp=(EditText)findViewById(R.id.edit_ip);
        Button mButtonSaveSetting=(Button)findViewById(R.id.button_savesetting);
        Button mButtonCancleSaveSetting=(Button)findViewById(R.id.button_canclesavesetting);

        mEditTextIp.setText(IP);

        //保存设置
        mButtonSaveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改IP
                IP=mEditTextIp.getText().toString();
                saveToFile(IP);
                Toast.makeText(SettingActivity.this,"保存设置成功，新IP是："+IP,Toast.LENGTH_SHORT).show();
                //返回MainActivity
                Intent intent=new Intent(SettingActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //取消设置
        mButtonCancleSaveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this,"取消设置",Toast.LENGTH_SHORT).show();
                //返回MainActivity
                Intent intent=new Intent(SettingActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
