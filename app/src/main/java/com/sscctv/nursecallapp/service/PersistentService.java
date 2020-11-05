package com.sscctv.nursecallapp.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.ui.activity.BackgroundActivity;
import com.sscctv.nursecallapp.ui.activity.EmergencyActivity;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.seeeyes.nursecallapp.calldisplay.Reply;
import io.seeeyes.nursecallapp.calldisplay.SystemInfo;

public class PersistentService extends Service implements SensorEventListener {
    private static final String TAG = "PersistentService";
    public static final String BROADCAST_BUFFER_SEND_CODE = "com.sscctv.nursecallapp.utils.NurseCallUtils";
    private static final int grpc_port = 50054;
    private static final int em_port = 59009;
    private int MILLISINFUTURE = 100 * 100;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private static PersistentService sInstance;
    private CountDownTimer countDownTimer;
    private TinyDB tinyDB;


    private SensorManager sensorManager;
    private Sensor lightSensor;
    private String light;

    private View topView;
    private WindowManager wManager;
    private WindowManager.LayoutParams params;
    private EditText getText;

    private SystemInfo systemInfo;
    private Reply reply;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isReady() {
        return sInstance != null;
    }

    public static PersistentService getInstance() {
        return sInstance;
    }

    public Context getApplicationContext() {
        return this;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        tinyDB = new TinyDB(this);
        unregisterRestartAlarm();
        initData();
        registerReceiver(broadcastBufferReceiver, new IntentFilter(BROADCAST_BUFFER_SEND_CODE));

        initSensor();
//        TcpSockServer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("")
                .setContentText("")
                .build();

        assert nm != null;
        nm.notify(startId, notification);
        nm.cancel(startId);

        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("PersistentService", "onDestroy");
        countDownTimer.cancel();
        registerRestartAlarm();
        unregisterReceiver(broadcastBufferReceiver);
    }

    private void initSensor() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void initData() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME) != 0) {
            MILLISINFUTURE = tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME);
            countDownTimer();
            countDownTimer.start();
        }
    }

    private void startTimer() {
        if (tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME) != 0) {
            MILLISINFUTURE = tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME);
            countDownTimer();
            countDownTimer.start();
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                Log.i("PersistentService", "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
                NurseCallUtils.startNewIntent(PersistentService.this, BackgroundActivity.class);
            }
        };
    }

    private void registerRestartAlarm() {

        Log.i("000 PersistentService", "registerRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 1000, sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm() {

        Log.i(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.cancel(sender);


    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            int val = Objects.requireNonNull(bufferIntent.getExtras()).getInt("mode");

            switch (val) {
                case 0:
                    initData();
                    break;
                case 1:
                    stopTimer();
                    break;
                case 2:
                    startTimer();
                    break;
                case 3:
                    tinyDB.putInt(KeyList.BACK_DISPLAY_STAT, 0);
                    break;
                case 4:
                    tinyDB.putInt(KeyList.BACK_DISPLAY_STAT, 1);
                    break;
            }
        }
    };

    private void sendMessage(String message) {
        Intent intent = new Intent("call_start");
        intent.putExtra("msg", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            light = String.valueOf(sensorEvent.values[0]);
//            Log.d(TAG, "Light: " + light);
            tinyDB.putString(KeyList.SENSOR_LIGHT, light);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}

