package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.DpItem;
import com.sscctv.nursecallapp.data.EmCallLogItem;
import com.sscctv.nursecallapp.data.EmListItem;
import com.sscctv.nursecallapp.databinding.ActivityMainBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.service.PersistentService;
import com.sscctv.nursecallapp.service.RestartService;
import com.sscctv.nursecallapp.ui.fragment.BedCallFragment;
import com.sscctv.nursecallapp.ui.fragment.CallListFragment;
import com.sscctv.nursecallapp.ui.fragment.CallMainFragment;
import com.sscctv.nursecallapp.ui.fragment.NormalCallFragment;
import com.sscctv.nursecallapp.ui.fragment.NoticeFragment;
import com.sscctv.nursecallapp.ui.fragment.adapter.EmergencyAdapter;
import com.sscctv.nursecallapp.ui.settings.SetMainFragment;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NetworkStatus;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class MainActivity extends AppCompatActivity implements OnSelectCall {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private Timer mTimer;
    private RestartService restartService;
    private ActivityMainBinding mBinding;
    private AudioManager mAudioManager;
    private FragmentManager manager;
    private DataOutputStream opt;

    private AudioTrack audioTrack;
    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;

    private ConnectivityManager connectivityManager;
    private boolean netStat;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private Fragment prevFragment;
    private int udpPort = 1235;
    private static final int em_port = 59009;
    boolean emBackChange = false;
    private Animation blinkAnim;
    private boolean isRunning;
    private boolean isSleep = false;
    private boolean isDatChange = false;
    private boolean isTouchLock;
    private static final int REQUEST_CODE = 10;
    private EmergencyAdapter emergencyAdapter;
    private MediaPlayer mAlarm;
    private boolean sleepTimeMove = false;

    public MainActivity() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        tinyDB = new TinyDB(this);
        context = this;
        core = MainCallService.getCore();
        gpioPortSet();

        manager = getSupportFragmentManager();
        mBinding.wardStation.setOnClickListener(view -> {
            SetMainFragment fragment = new SetMainFragment();
            fragment.setArguments(setBundle("str", "ward"));
            setGoFragment(fragment, true);
        });

        mBinding.statNumber.setOnClickListener(view -> {
            SetMainFragment fragment = new SetMainFragment();
            fragment.setArguments(setBundle("str", "sip"));
            setGoFragment(fragment, true);
        });

        mBinding.btnDashCall.setOnClickListener(view -> {
            setGoFragment(new BedCallFragment(), false);
        });

        mBinding.btnDashNormal.setOnClickListener(view -> {
            setGoFragment(new NormalCallFragment(), false);
        });
        mBinding.btnDashList.setOnClickListener(view -> {
            setGoFragment(new CallListFragment(), false);
        });
        mBinding.btnDashNotice.setOnClickListener(view -> {
            setGoFragment(new NoticeFragment(), false);
        });
        mBinding.btnDashSetup.setOnClickListener(view -> {
            setGoFragment(new SetMainFragment(), false);
        });

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.emergencyList.setLayoutManager(layoutManager);

        blinkAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_anim);

        mBinding.btnTest.setOnClickListener(view -> {
            sleepMode(true);
        });

        isTouchLock = getResources().getBoolean(R.bool.screen_touch_lock);
        mBinding.btnTouch.setOnClickListener(view -> {
            if (!isTouchLock) {
                mBinding.btnTouch.setImageResource(R.drawable.ic_baseline_lock_36);
                isTouchLock = true;
            } else {
                mBinding.btnTouch.setImageResource(R.drawable.ic_baseline_lock_open_36);
                isTouchLock = false;
            }

        });

        mBinding.mainSleep.setOnTouchListener((view, motionEvent) -> {
            if (isTouchLock) {
                return true;
            } else {
                sleepMode(false);
                return mBinding.mainLayout.getVisibility() == View.VISIBLE;
            }
        });

        mBinding.btnAlarm.setOnClickListener(view -> stopAlarm());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "o" +
                "+nResume");
        MainTimerTask timerTask = new MainTimerTask();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 0, 500);

        deviceConfig();
        initService();

        LocalBroadcastManager.getInstance(this).registerReceiver(nReceiver, new IntentFilter("missed_call"));
        LocalBroadcastManager.getInstance(this).registerReceiver(goCallReceiver, new IntentFilter("call_start"));

        mAudioManager.setMode(MODE_NORMAL);
        netStat = ethStatChk();
        isRunning = true;

        new OnServer().execute();

        getDefaultLogo();
        firstSipSettings();

        setGoFragment(new BedCallFragment(), false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mTimer.cancel();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(nReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(goCallReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        unregisterReceiver(restartService);
        isRunning = false;

    }

    private void sleepMode(boolean mode) {
        isSleep = mode;
        mBinding.mainSleep.setClickable(isSleep);
        if (mode) {
            mBinding.mainSleep.setVisibility(View.VISIBLE);
            sleepTimeMove = true;
        } else {
            mBinding.mainSleep.setVisibility(View.GONE);
            sleepTimeMove = false;
        }


    }

    private void ctrlHandler(String mode) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mBinding.sleepTime.getLayoutParams();
        if (!sleepTimeMove) {
            return;
        }

        mHandler.postDelayed(() -> {

            switch (mode) {
                case "step1":
                    layoutParams.topMargin = 100;
                    layoutParams.leftMargin = 100;
                    ctrlHandler("step2");
                    break;

                case "step2":
                    layoutParams.topMargin = 550;
                    layoutParams.leftMargin = 100;
                    ctrlHandler("step3");
                    break;

                case "step3":
                    layoutParams.topMargin = 550;
                    layoutParams.leftMargin = 850;
                    ctrlHandler("step4");
                    break;

                case "step4":
                    layoutParams.topMargin = 100;
                    layoutParams.leftMargin = 850;
                    ctrlHandler("step1");
                    break;

            }
            mBinding.sleepTime.setLayoutParams(layoutParams);

        }, 10000);
    }


    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(fragment);
        fragmentTransaction.attach(fragment);
        fragmentTransaction.replace(R.id.main_frame, fragment).commit();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            assert imm != null;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }

    private void gpioPortSet() {
        try {
            Runtime command = Runtime.getRuntime();
            Process proc;

            proc = command.exec("su");
            opt = new DataOutputStream(proc.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException();
        }
    }


    public void restartTimer() {
        MainTimerTask timerTask = new MainTimerTask();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 0, 500);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();
//        Log.d(TAG, "Dispatch: " + event);

        if (flag == 40) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_F8:
                if (action == KeyEvent.ACTION_DOWN) {
                    tinyDB.putBoolean(KeyList.CALL_HOOK, true);
                } else if (action == KeyEvent.ACTION_UP) {
                    tinyDB.putBoolean(KeyList.CALL_HOOK, false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    adjustVolume(1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    adjustVolume(-1);
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
    }


    private void adjustVolume(int i) {
        mAudioManager.adjustStreamVolume(
                STREAM_VOICE_CALL,
                i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }


    private void firstSipSettings() {
        if (!tinyDB.getBoolean(KeyList.FIRST_SIP_SETTINGS)) {
            tinyDB.putBoolean(KeyList.FIRST_SIP_SETTINGS, true);

            Dialog dialog = new Dialog(Objects.requireNonNull(MainActivity.this));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_first_sip);

            final Button ok = dialog.findViewById(R.id.dg_btn_sip_ok);
            ok.setOnClickListener(view1 -> {
                dialog.dismiss();

                Bundle bundle = new Bundle(1);
                bundle.putString("str", "sip");

                SetMainFragment fragment = new SetMainFragment();
                fragment.setArguments(bundle);

                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, fragment).commit();
            });

            final Button close = dialog.findViewById(R.id.dg_btn_sip_close);
            close.setOnClickListener(view1 -> {
                dialog.dismiss();
            });
            dialog.show();
        }

    }

    private void getDeviceInfo() {

        ProxyConfig[] proxyConfigs = core.getProxyConfigList();
        if ((proxyConfigs == null) || proxyConfigs.length == 0) {
            mBinding.imgStat.setImageResource(R.drawable.led_error);
            mBinding.txtNumber.setText("설정 안됨");
        } else {
            for (ProxyConfig proxyConfig : proxyConfigs) {
                if (proxyConfig.getIdentityAddress() != null) {
                    if (proxyConfig.getState() == RegistrationState.Ok) {
                        mBinding.imgStat.setImageResource(R.drawable.led_connected);
                        mBinding.imgStat.clearAnimation();
                    } else {
                        mBinding.imgStat.setImageResource(R.drawable.led_error);
                        mBinding.imgStat.startAnimation(blinkAnim);
                    }
                    mBinding.txtNumber.setText(proxyConfig.getIdentityAddress().getUsername());
                }
            }
        }
    }

    public void deviceConfig() {
        String type = "";
        switch (tinyDB.getInt(KeyList.DEVICE_TYPE)) {
            case 0:
                type = "간호사 스테이션";
                break;
            case 1:
                type = "보안용 주수신기";
                break;
            case 2:
                type = "병리실 주수신기";
                break;
            case 3:
                type = "공용 주수신기";
                break;
        }

        mBinding.txtStation.setText(type);
        if (tinyDB.getString(KeyList.DEVICE_WARD).equals("")) {
            mBinding.txtWard.setText("병동 등록 안됨");
            mBinding.txtWard.setTextColor(getResources().getColor(R.color.JRed));
        } else {
            mBinding.txtWard.setText(String.format("%s 병동", tinyDB.getString(KeyList.DEVICE_WARD)));
            mBinding.txtWard.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(getApplicationContext(), "Address null");
        }
    }


    class MainTimerTask extends TimerTask {
        public void run() {
            mHandler.post(mUpdateTimeTask);
            mHandler.post(mUpdateStatus);

            if (netStat != ethStatChk()) {
                ethLedCntl(ethStatChk());
                netStat = ethStatChk();
            }

            if (isSleep) {
                mHandler.post(() -> {
                    if (!isDatChange) {
                        mBinding.colon.setVisibility(View.INVISIBLE);
                        isDatChange = true;
                    } else {
                        mBinding.colon.setVisibility(View.VISIBLE);
                        isDatChange = false;
                    }
                });
            }
        }
    }

    private Handler mHandler = new Handler();

    private Runnable mUpdateTimeTask = new Runnable() {

        public void run() {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            if (!isSleep) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일 E요일", Locale.getDefault());
                String formatTime = timeFormat.format(date);
                String formatDate = dateFormat.format(date);
                mBinding.timeText.setText(formatTime);
                mBinding.dateText.setText(formatDate);
            } else {
                SimpleDateFormat fHour = new SimpleDateFormat("HH", Locale.KOREA);
                SimpleDateFormat fMinute = new SimpleDateFormat("mm", Locale.KOREA);
                String strHour = fHour.format(date);
                String strMinute = fMinute.format(date);
                mBinding.hour.setText(strHour);
                mBinding.minute.setText(strMinute);
            }
        }
    };

    private Runnable mUpdateStatus = this::getDeviceInfo;

    private void initService() {
        if (PersistentService.isReady()) {
            Log.i(TAG, "PersistentService isReady()");
        } else {
            restartService = new RestartService();
            Intent intent = new Intent(this, PersistentService.class);

            IntentFilter intentFilter = new IntentFilter("com.sscctv.nursecallapp.service.PersistentService");
//            Log.i(TAG, "intentFilter: " + intentFilter);

            registerReceiver(restartService, intentFilter);
            startService(intent);

        }
    }

    private BroadcastReceiver nReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("msg");
            Log.d(TAG, "GET MESSAGE!!!");
            setGoFragment(new CallListFragment(), false);
        }
    };

    private BroadcastReceiver goCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            Log.d(TAG, "goCallReceiver: " + msg);
            prevFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
            boolean stat = Objects.equals(msg, NurseCallUtils.CALL_OUTGOING);

            Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);

            switch (Objects.requireNonNull(msg)) {
                case NurseCallUtils.CALL_DECLINE:
                    if (curFragment instanceof CallMainFragment) {
                        CallMainFragment callMainFragment = (CallMainFragment) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                        Objects.requireNonNull(callMainFragment).callDecline();
                        dpEndCall();
                    } else {
                        NurseCallUtils.printShort(getApplicationContext(), "호출에 실패했습니다. 번호를 다시 확인해주세요.");
                        return;
                    }

                    if (!tinyDB.getBoolean(KeyList.CALL_CONNECTED)) {
                        FragmentTransaction fragmentTransaction = manager.beginTransaction();
                        if (prevFragment != null) {
                            if (!stat) {
                                fragmentTransaction.replace(R.id.main_frame, BedCallFragment.newInstance()).commit();
                            } else {
                                fragmentTransaction.replace(R.id.main_frame, prevFragment).commit();
                            }
                        } else {
                            fragmentTransaction.replace(R.id.main_frame, BedCallFragment.newInstance()).commit();
                        }
                    } else {
                        new Handler().postDelayed(() -> {
                            FragmentTransaction fragmentTransaction = manager.beginTransaction();
                            if (prevFragment != null) {
                                if (!stat) {
                                    fragmentTransaction.replace(R.id.main_frame, BedCallFragment.newInstance()).commit();
                                } else {
                                    fragmentTransaction.replace(R.id.main_frame, prevFragment).commit();
                                }
                            } else {
                                fragmentTransaction.replace(R.id.main_frame, BedCallFragment.newInstance()).commit();
                            }
                        }, 1500);
                    }
                    break;
                case NurseCallUtils.CALL_NEW_DECLINE:
                    if (curFragment instanceof CallMainFragment) {
                        CallMainFragment callMainFragment = (CallMainFragment) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                        Objects.requireNonNull(callMainFragment).callNewDecline();
                    }
                    break;
                case NurseCallUtils.CALL_CALL_END:
                    if (curFragment instanceof CallMainFragment) {
                        CallMainFragment callMainFragment = (CallMainFragment) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                        Objects.requireNonNull(callMainFragment).closeDialog();
                    }
                    break;
                case NurseCallUtils.CALL_OUTGOING:
                case NurseCallUtils.CALL_INCOMING:
                case NurseCallUtils.CALL_NORMAL: {
                    if (isSleep) {
                        isSleep = false;
                        mBinding.mainSleep.setVisibility(View.GONE);
                    }
                    Bundle bundle = new Bundle(1);
                    bundle.putString(NurseCallUtils.CALL_NORMAL, msg);

                    CallMainFragment fragment = new CallMainFragment();
                    fragment.setArguments(bundle);

                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, fragment).commit();
                    break;
                }
                case NurseCallUtils.CALL_CONNECTED:
                    dpEndCall();
                    break;
                case NurseCallUtils.CALL_NEW_INCOMING:

                    break;
                default:
                    if (msg.contains(NurseCallUtils.CALL_NEW_INCOMING)) {
                        if (curFragment instanceof CallMainFragment) {
                            CallMainFragment callMainFragment = (CallMainFragment) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                            Objects.requireNonNull(callMainFragment).acceptDialog(msg);
                        }
                    }

                    break;
            }
        }
    };

    private boolean ethStatChk() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                return NetworkStatus.getConnectivityStatus(getApplicationContext()) == 9;
            }
        }
        return false;
    }

    private void ethLedCntl(boolean stat) {
//        Log.d(TAG, "ethLedCntl: " + stat);

        if (stat) {
            try {
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG2/data\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Network Info Null");
            try {
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG2/data\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class OnServer extends AsyncTask<Void, Boolean, String> {

        private DataOutputStream dos;
        private DataInputStream dis;
        byte[] rebuff = new byte[128];
        int rebuff_cnt = 0;

        protected String doInBackground(Void... params) {
            try {
                Log.w(TAG, "<< ...TCP Server running... >>");
                ServerSocket serverSocket = new ServerSocket(em_port);
                while (isRunning) {
                    Log.w(TAG, "<< ...Waiting for client connection... >>");
                    Socket sock = serverSocket.accept();
                    Log.i(TAG, "<< ...Clint IP: " + sock.getInetAddress() + " connected... >>");
                    if (sock.getInputStream() != null) {
                        try {
                            dos = new DataOutputStream(sock.getOutputStream());
                            dis = new DataInputStream(sock.getInputStream());
                            while (true) {
                                Thread.sleep(10);
                                rebuff_cnt = dis.read(rebuff);
//                                    Log.d(TAG, "rebuff_cnt: " + rebuff_cnt);
                                if (rebuff_cnt <= 0) {
                                    break;
                                }
//                                    Log.d(TAG, "rebuff: " + Arrays.toString(rebuff));
                                String s = getDataProcess(rebuff, rebuff_cnt);
                                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
//                                dos.write(bytes);
                                if (emBackChange) {
                                    publishProgress(true);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            dis.close();
                            dos.close();
                            sock.close();
                        }
                        Log.w(TAG, "<< ...Clint IP: " + sock.getInetAddress() + " disconnected... >>");
                    }


                }
                Log.d(TAG, "<< ...TCP Server stop... >>");
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            if (values[0]) {
                emBackChange = false;
                Log.d(TAG, "onProgressUpdate");

//                if (!emBackColor) {
//                    emBackColor = true;
//                    leftMenuBlink(mBinding.mainMenu, true);
//                    ledRedControl(true);
//
//                } else {
//                    emBackColor = false;
//                    leftMenuBlink(mBinding.mainMenu, false);
//                    ledRedControl(false);
//                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.w(TAG, "<< ...Discovery Service End... >>");
        }
    }

    private String dataFormatChange(byte buf) {
        return String.format("%02X", buf);
    }

    private String getDataProcess(byte[] buf, int num) {
        boolean chk = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            stringBuilder.append(String.format("%02X ", buf[i]));
        }
        Log.d(TAG, "getData: " + stringBuilder.toString());

        String protoVer = dataFormatChange(buf[0]);
        String msgType = dataFormatChange(buf[1]);
        String msgId_1 = dataFormatChange(buf[2]);
        String msgId_2 = dataFormatChange(buf[3]);
        String msgLen_1 = dataFormatChange(buf[4]);
        String msgLen_2 = dataFormatChange(buf[5]);
        String devId_1 = dataFormatChange(buf[6]);
        String devId_2 = dataFormatChange(buf[7]);
        String devId_3 = dataFormatChange(buf[8]);
        String devId_4 = dataFormatChange(buf[9]);
        String stat_req_1 = dataFormatChange(buf[10]);
        String stat_req_2 = dataFormatChange(buf[11]);

        StringBuilder devInfo = new StringBuilder();
        devInfo.append(devId_1);
        devInfo.append(devId_2);
        devInfo.append(devId_3);

        ArrayList<EmListItem> emList = NurseCallUtils.getEmAllList(tinyDB, KeyList.EM_ALL_LIST);
        switch (protoVer) {
            case "10":

                if (msgType.equals("F7")) {
                    if (devId_4.equals("01") || devId_4.equals("02")) {
                        if (stat_req_2.equals("01")) {
                            for (int i = 0; i < emList.size(); i++) {
                                chk = emList.get(i).getDeviceInfo().equals(devInfo.toString());
                                Log.d(TAG, "Chk: " + emList.get(i).getDeviceInfo() + ", " + devInfo.toString());
                                if (chk) {
                                    break;
                                }
                            }

                            if (!chk) {
                                emList.add(new EmListItem(devInfo.toString(), devId_4, getHexToDec(devId_1), getHexToDec(devId_2 + devId_3), devId_3, System.currentTimeMillis(), stat_req_2));
                                NurseCallUtils.putEmCallList(tinyDB, KeyList.EM_ALL_LIST, emList);
                                mHandler.post(() -> {
                                    emergencyAdapter = new EmergencyAdapter(getApplicationContext(), NurseCallUtils.getEmAllList(tinyDB, KeyList.EM_ALL_LIST), this);
                                    mBinding.emergencyList.setAdapter(emergencyAdapter);
                                    emergencyAdapter.notifyDataSetChanged();
                                    if (emergencyAdapter.getItemCount() != 0) {
                                        mBinding.emergencyList.smoothScrollToPosition(emergencyAdapter.getItemCount() - 1);
                                    }
                                    startAlarm();
                                    if (sleepTimeMove) {
                                        sleepMode(false);
                                    }
                                });

                                emStartCall(getHexToDec(devId_1), getHexToDec(devId_2 + devId_3));
                            }
                        } else if (stat_req_2.equals("00")) {
                            for (int i = 0; i < emList.size(); i++) {
                                chk = emList.get(i).getDeviceInfo().equals(devInfo.toString());
                                Log.d(TAG, "Chk: " + emList.get(i).getDeviceInfo() + ", " + devInfo.toString());
                                if (chk) {
                                    emList.remove(i);
                                    NurseCallUtils.putEmCallList(tinyDB, KeyList.EM_ALL_LIST, emList);

                                    mHandler.post(() -> {
                                        emergencyAdapter = new EmergencyAdapter(getApplicationContext(), NurseCallUtils.getEmAllList(tinyDB, KeyList.EM_ALL_LIST), this);
                                        mBinding.emergencyList.setAdapter(emergencyAdapter);
                                    });

                                    dpEndCall();
                                    break;
                                }
                            }
                        }
                    } else if (devId_4.equals("03")) {
                        if (stat_req_2.equals("01")) {
                            for (int i = 0; i < emList.size(); i++) {
                                chk = emList.get(i).getDeviceInfo().equals(devInfo.toString());
                                Log.d(TAG, "Chk: " + emList.get(i).getDeviceInfo() + ", " + devInfo.toString());
                                if (chk) {
                                    emList.remove(i);
                                    NurseCallUtils.putEmCallList(tinyDB, KeyList.EM_ALL_LIST, emList);
                                    mHandler.post(() -> {
                                        emergencyAdapter = new EmergencyAdapter(getApplicationContext(), NurseCallUtils.getEmAllList(tinyDB, KeyList.EM_ALL_LIST), this);
                                        mBinding.emergencyList.setAdapter(emergencyAdapter);
                                        if (emergencyAdapter.getItemCount() != 0) {
                                            mBinding.emergencyList.smoothScrollToPosition(emergencyAdapter.getItemCount() - 1);
                                        }
                                        dpEndCall();
                                    });
                                    break;
                                }
                            }
                        } else if (stat_req_2.equals("00")) {
                            // 재중 취소
                        }
                    }
                    ArrayList<EmCallLogItem> emLog = NurseCallUtils.getEmLog(tinyDB, KeyList.CALL_LOG_EMERGENCY);
                    emLog.add(new EmCallLogItem(devId_4, stat_req_2, getHexToDec(devId_1), getHexToDec(devId_2 + devId_3), System.currentTimeMillis()));
                    NurseCallUtils.putEmLog(tinyDB, KeyList.CALL_LOG_EMERGENCY, emLog);

                }

                break;
            case "00":
                //TODO
                break;
        }

        mHandler.post(() -> {
            if (emergencyAdapter == null) {
                return;
            }

            if (emergencyAdapter.getItemCount() == 0) {
                stopAlarm();
                getEventLayoutSetup(false);
            } else {
                getEventLayoutSetup(true);
            }
        });


        return stringBuilder.toString();
    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    public void normalStartCall(String num) {
        new Thread(() -> {
            try {
                ArrayList<DpItem> list = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);
                for (int i = 0; i < list.size(); i++) {
                    if (pingTest(list.get(i).getIpAddr())) {
                        Socket socket = new Socket(list.get(i).getIpAddr(), 59009);

                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        out.println("#normal_call" + NurseCallUtils.STRING_SPLIT + num);
                        out.flush();
                        socket.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void cctvAppCall() {
        Log.d(TAG, "GOGO");
        new Thread(() -> {
            try {
                if (pingTest("192.168.200.250")) {
                    Socket socket = new Socket("192.168.200.250", 59009);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println("#goApp");
                    out.flush();
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void cctvAppClose() {
        new Thread(() -> {
            try {
                if (pingTest("192.168.200.250")) {
                    Socket socket = new Socket("192.168.200.250", 59009);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println("#closeApp");
                    out.flush();
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void emStartCall(String ward, String room) {
        new Thread(() -> {
            try {
                ArrayList<DpItem> list = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);
                for (int i = 0; i < list.size(); i++) {
                    if (pingTest(list.get(i).getIpAddr())) {
                        Socket socket = new Socket(list.get(i).getIpAddr(), 59009);

                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        out.println("#em_call" + NurseCallUtils.STRING_SPLIT + ward + NurseCallUtils.STRING_SPLIT + room);
                        out.flush();
                        socket.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void dpEndCall() {
        new Thread(() -> {
            try {
                ArrayList<DpItem> list = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);
                for (int i = 0; i < list.size(); i++) {
                    if (pingTest(list.get(i).getIpAddr())) {
                        Socket socket = new Socket(list.get(i).getIpAddr(), 59009);

                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        out.println("#end");
                        out.flush();
                        socket.close();
                    }
                }
                cctvAppClose();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean pingTest(String ip) {
        Runtime runtime = Runtime.getRuntime();

        String cmd = "ping -c 1 -W 2 " + ip;

        Process process = null;

        try {
            process = runtime.exec(cmd);
        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        try {
            assert process != null;
            process.waitFor();
        } catch (InterruptedException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        int result = process.exitValue();

        return result == 0;
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public String byteArrayToHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }

        return sb.toString();
    }

    private String getHexToDec(String hex) {
        long v = Long.parseLong(hex, 16);
        return String.valueOf(v);
    }

    public void leftMenuBlock(boolean mode) {
        mBinding.btnDashCall.setEnabled(mode);
        mBinding.btnDashList.setEnabled(mode);
        mBinding.btnDashNormal.setEnabled(mode);
        mBinding.btnDashNotice.setEnabled(mode);
        mBinding.btnDashSetup.setEnabled(mode);
        mBinding.wardStation.setEnabled(mode);
        mBinding.statNumber.setEnabled(mode);
    }

    private void getEventLayoutSetup(boolean mode) {
        FrameLayout.LayoutParams newLayoutParams = (FrameLayout.LayoutParams) mBinding.mainLayout.getLayoutParams();

        if (mode) {
            newLayoutParams.topMargin = 100;
            mBinding.topLayout.setVisibility(View.VISIBLE);
        } else {
            newLayoutParams.topMargin = 0;
            mBinding.topLayout.setVisibility(View.GONE);

        }
        mBinding.mainLayout.setLayoutParams(newLayoutParams);
    }

    private void ledRedControl(boolean mode) {
        try {
            if (mode) {
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG2/data\n");
            } else {
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG2/data\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void leftMenuBlink(LinearLayout layout, boolean mode) {
        if (mode) {
            layout.setBackground(getResources().getDrawable(R.drawable.back_emergency));
        } else {
            layout.setBackground(getResources().getDrawable(R.drawable.main_background));
        }
    }

    @Override
    public void roomSelect(int position) {
        Log.d(TAG, "Position: " + position);
        ArrayList<EmListItem> items = NurseCallUtils.getEmAllList(tinyDB, KeyList.EM_ALL_LIST);
        String ward = items.get(position).getWard();
        String room = items.get(position).getRoom();
        String device = items.get(position).getDevice();


        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(R.layout.dialog_alram_call);

        final TextView con1 = dialog.findViewById(R.id.dg_em_con1);
        final TextView con2 = dialog.findViewById(R.id.dg_em_con2);

        switch (device) {
            case "01":
                device = "위급 호출";
                break;
            case "02":
                device = "응급 호출";
                break;
            case "03":
                device = "재중 호출";
                break;
        }
        con1.setText(ward + "병동 " + room + " 병실에서 " + device + "이 발생했습니다.");
        con2.setText("신속히 확인 바랍니다.");

        final Button close = dialog.findViewById(R.id.dg_em_close);
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void roomAllClear() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void starSelect(String position, boolean chk) {
    }

    private void getDefaultLogo() {
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() +
                KeyList.ADMIN_FILE + KeyList.IMAGE_FILE + File.separator + "logo.png");
        mBinding.mainLogo.setImageBitmap(bitmap);
    }

    public void setNewLogo() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void saveImage(Bitmap finalBitmap) {
        File logoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE);
        if (!logoFile.exists()) {
            if (!logoFile.mkdir()) {
                Log.e(TAG, "Not created file");
            } else {
                Log.d(TAG, "Success create file");
            }
        }

        File myDir = new File(logoFile + File.separator + "logo.png");
        try {
            FileOutputStream out = new FileOutputStream(myDir);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    if (uri == null) {
                        NurseCallUtils.printShort(this, "해당 앱은 로고 변경을 지원하지 않습니다. 다른 방법을 사용하세요.");
                        return;
                    }

                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    saveImage(img);

                    in.close();

                    mBinding.mainLogo.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                NurseCallUtils.printShort(this, "로고 변경이 취소되었습니다.");
            }
        }
    }

    private void startAlarm() {

//        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);
        Log.d(TAG, "startAlarm");
        try {
            AssetFileDescriptor afd;
            afd = getApplicationContext().getAssets().openFd("emergency_alarm.wav");
            if (mAlarm == null) {
                mAlarm = new MediaPlayer();
                mAlarm.reset();
                mAlarm.setAudioStreamType(STREAM_RING);
                mAlarm.setLooping(true);
                try {
                    mAlarm.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAlarm.setOnPreparedListener(MediaPlayer::start);
                mAlarm.prepareAsync();

                mAlarm.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopAlarm() {
        Log.d(TAG, "stopAlarm");
        if (mAlarm != null) {
            mAlarm.stop();
            mAlarm.release();
            mAlarm = null;
        }
    }

    private Bundle setBundle(String tag, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(tag, msg);
        return bundle;
    }

    private void setGoFragment(Fragment fragment, boolean mode) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        if (mode) {
            fragmentTransaction.replace(R.id.main_frame, fragment).commit();
        } else {
            fragmentTransaction.replace(R.id.main_frame, fragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}