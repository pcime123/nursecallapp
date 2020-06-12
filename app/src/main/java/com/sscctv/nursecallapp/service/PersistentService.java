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
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

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
import java.util.Timer;
import java.util.TimerTask;

public class PersistentService extends Service implements SensorEventListener {
    private static final String TAG = "PersistentService";
    public static final String BROADCAST_BUFFER_SEND_CODE = "com.sscctv.nursecallapp.utils.NurseCallUtils";

    private int MILLISINFUTURE = 100 * 100;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private static PersistentService sInstance;
    private CountDownTimer countDownTimer;
    private Context mContext;
    private TinyDB tinyDB;
    private ServerSocket serverSocket;
    private boolean isRunning;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private String light;

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
        TcpSockServer(59009);
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
            }
        }
    };


    private ServerSocket serversock;

    private void TcpSockServer(int iport) {
        //서버소켓 생성
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    System.out.println("[서버실행]");
                    serversock = new ServerSocket(iport);
                    while (true) {
                        System.out.println("[클라이언트 접속을 위한 대기중...]");
                        Socket sock = serversock.accept();
                        System.out.println("[클라이언트 IP '" + sock.getInetAddress() + "' 접속됨 ]");

                        if (sock.getInputStream() != null) {
                            TcpSockServer_read read = new TcpSockServer_read(sock);
                            read.start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }



    public class TcpSockServer_read extends Thread {
        Socket sock = null;
        DataOutputStream dos;
        DataInputStream dis; //읽기버퍼
        byte[] rbuff = new byte[1024];
        int rbuff_cnt = 0; //연결된 클라이언트 아이피
        private String sip = null;

        TcpSockServer_read(Socket sock) {
            this.sock = sock;
            sip = sock.getInetAddress().toString();
        }

        public void run() {
            try {
                try {
                    // 클라이언트와 문자열 통신을 위한 스트림 생성
                    dos = new DataOutputStream(sock.getOutputStream());
                    dis = new DataInputStream(sock.getInputStream());
                    while (true) {
//                    Thread.sleep(1);
                        rbuff_cnt = dis.read(rbuff);

                        if (rbuff_cnt == 0) {
                            break;
                        }

                        dos.write(0);
                        String s = fn_testDisplay(rbuff, rbuff_cnt);
                        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                        dos.write(bytes);
                    }
                } finally {
                    dis.close();
                    dos.close();
                    sock.close();
                }
            } catch (IOException e) {
                System.out.println("클라이언트 IP '" + sip + "' 접속종료");
            }
        }
    }


    private String fn_testDisplay(byte[] buf, int num) {
        int i;

        StringBuilder stringBuilder = new StringBuilder();
        for (i = 0; i < num; i++) {
            stringBuilder.append(String.format("%02X ", buf[i]));
        }

        Log.d("Builder", "Data: " + stringBuilder.toString().length());

        tinyDB.putString(KeyList.EM_TYPE, String.format("%02X", buf[2]));
        tinyDB.putString(KeyList.EM_CALL, String.format("%02X", buf[11]));

        Log.d(TAG, "Type: " + tinyDB.getString(KeyList.EM_TYPE) + " Call: " + tinyDB.getString(KeyList.EM_CALL));
        if (tinyDB.getString(KeyList.EM_TYPE).equals("01") && tinyDB.getString(KeyList.EM_CALL).equals("01")) {
            Intent intent = new Intent(getApplicationContext(), EmergencyActivity.class);
            intent.putExtra("call", "01");
            intent.putExtra("type", "01");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if(tinyDB.getString(KeyList.EM_TYPE).equals("01") && tinyDB.getString(KeyList.EM_CALL).equals("00")) {
            Intent intent = new Intent(getApplicationContext(), EmergencyActivity.class);
            intent.putExtra("call", "00");
            intent.putExtra("type", "01");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if(tinyDB.getString(KeyList.EM_TYPE).equals("02") && tinyDB.getString(KeyList.EM_CALL).equals("01")) {
            Intent intent = new Intent(getApplicationContext(), EmergencyActivity.class);
            intent.putExtra("call", "01");
            intent.putExtra("type", "02");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if(tinyDB.getString(KeyList.EM_TYPE).equals("02") && tinyDB.getString(KeyList.EM_CALL).equals("00")) {
            Intent intent = new Intent(getApplicationContext(), EmergencyActivity.class);
            intent.putExtra("call", "00");
            intent.putExtra("type", "02");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return stringBuilder.toString();
    }

    public static void sendStatus(Context context, String msg) {
        Intent intent = new Intent("activity_emergency");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            light = String.valueOf(sensorEvent.values[0]);
//            Log.d(TAG, "Light: " + light);
            tinyDB.putString(KeyList.SENSOR_LIGHT, light);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}

