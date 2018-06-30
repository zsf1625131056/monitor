package com.example.zhongshifeng.monitor;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class Realize extends Service {

    private SensorManager sensorManager;
    private Vibrator vibrator;
    private static final int SENSOR_SHAKE = 10;

    private MediaRecorder recorder;
    private boolean record = false;
    private File audioFile;

    public Realize() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取电话管理器对象
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //设置电话监听器，监听电话状态
        telephonyManager.listen(new MyTelephoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sensorManager != null) {// 注册监听器
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {// 取消监听器
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    /**
     * 重力感应监听
     */
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 传感器信息改变时执行该方法
            float f = event.values[0];
            record = f == 0.0;//距离传感器为0时开启
            Message msg = new Message();
            msg.what = SENSOR_SHAKE;
            handler.sendMessage(msg);
//            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENSOR_SHAKE:
                    Log.e(TAG, "执行操作！");
                    if(record) {
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//从麦克风采集声音
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //内容输出格式
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        //输出到缓存目录，此处可以添加上传录音的功能，也可以存到其他位置
                        audioFile = new File(getExternalCacheDir(), "recoder" + "_" + System.currentTimeMillis() + ".3gp");
                        Log.e("msg", audioFile.toString());
                        recorder.setOutputFile(audioFile.getAbsolutePath());
                        try {
                            recorder.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        recorder.start();
                    }else {
                        if(recorder!=null) {
                            recorder.stop(); //停止刻录
                            recorder.release(); //释放资源
                        }
                    }
                    break;
            }
        }
    };

    //定义监听内部类实现监听录音
    class MyTelephoneListener extends PhoneStateListener {
        private MediaRecorder recorder;
        private boolean record;
        private File audioFile;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//从麦克风采集声音
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //内容输出格式
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    //输出到缓存目录，此处可以添加上传录音的功能，也可以存到其他位置
                    audioFile = new File(getCacheDir(), "recoder" + "_" + System.currentTimeMillis() + ".3gp");
                    Log.d("msg",audioFile.toString());
                    recorder.setOutputFile(audioFile.getAbsolutePath());
                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recorder.start();
                    record = true;
                    Log.i("msg", "电话已经摘机");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    String mobile = incomingNumber;
                    Log.i("msg", "电话已响铃");
                    Log.i("msg", mobile + "来电");
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (record) {
                        recorder.stop(); //停止刻录
                        recorder.release(); //释放资源
                        Log.i("msg", "电话空闲");
                        record = false;
                    }
                    break;
            }
        }

    }
}
