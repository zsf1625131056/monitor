package com.example.zhongshifeng.monitor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
    }

    private static final int READ_PHONE_STATE = 1;
    private static String[] CALLS_STATE = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };


    public static void verifyStoragePermissions(Activity activity) {

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, CALLS_STATE, READ_PHONE_STATE);
        }

    }

    /**
     * 开启后台监听来电录音和摇一摇录音
     * @param v
     */
    public void startService(View v){
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, com.example.zhongshifeng.monitor.Realize.class);
        startService(intent);
        this.finish();
    }

}
