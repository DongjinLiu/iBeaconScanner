package com.example.jin.ibeacontest;

import android.content.Context;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by jin on 2017/7/17.
 *
 * 用来持久化保存服务器的IP信息
 *
 * 包含两个方法：
 * 读取配置文档
 * 在Setting中修改后，保存在配置文档中
 */

class FileUtilIp {

    static File dir;

    /**
     * 将输入的IP保存在这个应用的某个文件中
     * @param ip
     */
    public static void saveToFile(String ip) {
        //File dir = context.getFilesDir(); //查找这个应用下的所有文件所在的目录
        FileWriter writer;
        try {
            writer = new FileWriter(dir.getAbsolutePath() + "/userinfo.txt");
            writer.append(ip);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create MainActivity 时，从配置文档中读取出来IP
     * @param context Activity的上面的某一层是Context,所以传值过来的是一个Activity,此处可以写成Context
     */
    public static String readFromFile(Context context) {
        dir=context.getFilesDir();//目录为：/data/data/com.etc.login/files
        FileReader reader;
        String ip="";
        try {
            reader = new FileReader(dir.getAbsolutePath() + "/userinfo.txt");
            BufferedReader breader = new BufferedReader(reader);
            ip = breader.readLine();
            breader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
}
