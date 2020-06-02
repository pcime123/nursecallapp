package com.sscctv.nursecallapp.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.sscctv.nursecallapp.ui.activity.BackgroundActivity;
import com.sscctv.nursecallapp.ui.activity.test;

import java.util.Objects;

public class PersistentService extends Service  {
    private static final String TAG = "PersistentService";
    public static final String BROADCAST_BUFFER_SEND_CODE = "com.sscctv.nursecallapp.utils.NurseCallUtils";

    private int MILLISINFUTURE = 1000 * 1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private static PersistentService sInstance;
    private CountDownTimer countDownTimer;
    private Context mContext;
//
//    public class PersistentServiceBinder extends Binder {
//        public PersistentService getService() {
//            return PersistentService.this; //현재 서비스를 반환.
//        }
//    }
//

//    private final IBinder mBinder = new PersistentServiceBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//
//    public interface ICallback {
//        public void receiveData();
//    }
//
//    private ICallback mCallback;

//    public void registerCallback(ICallback cb) {
//        mCallback = cb;
//    }
//
//    public void testService() {
//        Log.d(TAG, "Test!!" );
//    }



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
        unregisterRestartAlarm();
        super.onCreate();
//        initData();

        registerReceiver(broadcastBufferReceiver, new IntentFilter(BROADCAST_BUFFER_SEND_CODE));
//        countDownTimer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCom" + "mand: " + intent);
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

    /**
     * 데이터 초기화
     */
    public void initData() {
        countDownTimer();
        countDownTimer.start();

    }


    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {

                Log.i("PersistentService", "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
                Intent intent = new Intent(PersistentService.this, BackgroundActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.i("PersistentService", "onFinish");
            }


        };
    }


    public void resumeTimer() {
        countDownTimer.start();
    }

    public void cancelTimer() {
        countDownTimer.cancel();
    }


    /**
     * 알람 매니져에 서비스 등록
     */
    private void registerRestartAlarm() {

        Log.i("000 PersistentService", "registerRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /**
         * 알람 등록
         */
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

        /**
         * 알람 취소
         */
        alarmManager.cancel(sender);


    }



    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            mContext = context;
            int time = Objects.requireNonNull(bufferIntent.getExtras()).getInt("time");
            MILLISINFUTURE = time;
            if(countDownTimer != null) {
                countDownTimer.cancel();
            }
            initData();
            Log.d(TAG, "receive: " + time);
        }
    };

}

