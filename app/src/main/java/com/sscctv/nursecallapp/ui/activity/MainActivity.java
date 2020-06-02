package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityMain1Binding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.service.PersistentService;
import com.sscctv.nursecallapp.service.RestartService;
import com.sscctv.nursecallapp.ui.fragment.BedCallFragment;
import com.sscctv.nursecallapp.ui.fragment.NormalCallFragment;
import com.sscctv.nursecallapp.ui.settings.SettingsActivity;
import com.sscctv.nursecallapp.ui.settings.SettingsSipAccount;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private Timer mTimer;
    private RestartService restartService;
    private ActivityMain1Binding mBinding;
    private AudioManager mAudioManager;
    private FragmentManager manager;
    private BedCallFragment bedCallFragment;
    private NormalCallFragment normalCallFragment;
    private DataOutputStream opt;

    // HeadSet Hook Sound Setting
    private AudioTrack audioTrack;
    private Handler soundHandler = new Handler();
    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double[] sample = new double[numSamples];
    private final byte[] generatedSnd = new byte[2 * numSamples];

    private int mSampleCount;
    private int bufferSize = sampleRate;
    byte[] samples;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private MediaPlayer mRingerPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main1);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        tinyDB = new TinyDB(this);
        context = this;
        core = MainCallService.getCore();
        gpioPortSet();

        bedCallFragment = new BedCallFragment();
        normalCallFragment = new NormalCallFragment();
        manager = getSupportFragmentManager();

        mBinding.btnDashDashboard.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, PbxLoginActivity.class);
        });

        mBinding.statNumber.setOnClickListener(view -> NurseCallUtils.startIntent(this, SettingsSipAccount.class));

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_frame, bedCallFragment).commitAllowingStateLoss();

        mBinding.btnDashCall.setOnClickListener(view -> {
            FragmentTransaction dashCall = manager.beginTransaction();
            dashCall.replace(R.id.main_frame, bedCallFragment).commitAllowingStateLoss();
        });

        mBinding.btnDashNormal.setOnClickListener(view -> {
            FragmentTransaction dashNormal = manager.beginTransaction();
            dashNormal.replace(R.id.main_frame, normalCallFragment).commitAllowingStateLoss();


        });
        mBinding.btnDashList.setOnClickListener(view -> {
//            playMedia();
//            mAudioManager.setSpeakerphoneOn(false);
        });
        mBinding.btnDashNotice.setOnClickListener(view -> {
//            mAudioManager.setSpeakerphoneOn(true);
//            stopMedia();
        });

        mBinding.btnDashSetup.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SettingsActivity.class);
//            if (audioTrack != null) {
//                if (audioTrack.getPlayState() != 3) {
//                    Log.d(TAG, "AudioTrack0: " + audioTrack.getPlayState());
//
//                } else {
//                    Log.d(TAG, "AudioTrack3: " + audioTrack.getPlayState());
//                }
//            }
        });

//        mAudioManager.setMode(MODE_IN_CALL);

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

    @Override
    protected void onResume() {
        super.onResume();
        MainTimerTask timerTask = new MainTimerTask();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 1000, 1000);
        deviceConfig();
        getDeviceInfo();
        initService();
        mAudioManager.setMode(MODE_NORMAL);
    }


    //
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
////        Log.d(TAG, "Handset: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) + " onKeyUp=" + keyCode + " event: " + event);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_F8:
////                if (tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS)) {
////                    stopHandSetMode();
//                    Log.d(TAG, "onKeyUp KEYCODE_F8: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS));
//                    tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, false);
////                }
//                return false;
//
//        }
//        return super.onKeyUp(keyCode, event);
//    }
//
//
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        Log.d(TAG, " onKeyDown=" + keyCode + " event: " + event);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_CALL:
//                if (!tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
////                    startSpeakerMode();
//                    tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, true);
//                } else {
////                    stopSpeakerMode();
//                    tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, false);
//                }
//                return true;
//            case KeyEvent.KEYCODE_F8:
//                Log.d(TAG, "onKeyDown KEYCODE_F8: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS));
////                if (!tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS)) {
////                    startHandSetMode();
//                    tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, true);
////                }
//                return false;
//            case KeyEvent.KEYCODE_VOLUME_UP:
//                adjustVolume(1);
//                return true;
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                adjustVolume(-1);
//                return true;
//
//        }
//        return super.onKeyDown(keyCode, event);
//    }

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
                    if (!tinyDB.getBoolean(KeyList.CALL_MODE)) {
                        tinyDB.putBoolean(KeyList.CALL_MODE, true);
                    }
                } else if (action == KeyEvent.ACTION_UP) {
                    if (tinyDB.getBoolean(KeyList.CALL_MODE)) {
                        tinyDB.putBoolean(KeyList.CALL_MODE, false);
                    }
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
//
//    public void startHandSetMode() {
//        Log.v(TAG, "startHandler // Speaker: " + tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS) + " Handset: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) + " CallStat: " + callStat);
//
//        if (tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//            if (callStat) {
//                mAudioManager.setSpeakerphoneOn(false);
//                tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, false);
//            }
//        } else {
//            if (!callStat) {
//                mAudioManager.setSpeakerphoneOn(false);
//                startWaitTone("handset");
//                goCallPage();
//            } else {
//                stopMedia();
//            }
//        }
//        ledCallBtn(callStat);
//    }
//
//    public void stopHandSetMode() {
//        Log.v(TAG, "stopHandSetMode // Speaker: " + tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS) + " Handset: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) + " CallStat: " + callStat);
//        tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, false);
//        if (!callStat) {
//            if (!mAudioManager.isSpeakerphoneOn()) {
//                mAudioManager.setSpeakerphoneOn(true);
//            }
//        } else {
//            stopSound();
//        }
//        ledCallBtn(callStat);
//    }
//
//    public void startSpeakerMode() {
//        Log.w(TAG, "startSpeakerMode // Speaker: " + tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS) + " Handset: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) + " CallStat: " + callStat);
//
//        if (tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS)) {
//            if (callStat) {
//                mAudioManager.setSpeakerphoneOn(true);
//                tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, false);
//            }
//        } else {
//            if (!callStat) {
//                mAudioManager.setSpeakerphoneOn(true);
//                startWaitTone("speaker");
//                goCallPage();
//            } else {
//                stopMedia();
//            }
//        }
//        ledCallBtn(callStat);
//    }
//
//    public void stopSpeakerMode() {
//        tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, false);
//        if (!callStat) {
//            if (mAudioManager.isSpeakerphoneOn()) {
//                mAudioManager.setSpeakerphoneOn(false);
//            }
//        } else {
//            stopMedia();
//        }
//        ledCallBtn(callStat);
//    }

    private void ledCallBtn(boolean mode) {
        try {
            if (mode) {
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG4/data\n");
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG3/data\n");
            } else {
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG4/data\n");
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG3/data\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goCallPage() {
        FragmentTransaction dashCall = manager.beginTransaction();
        dashCall.replace(R.id.main_frame, bedCallFragment).commitAllowingStateLoss();
    }

    private void adjustVolume(int i) {
        mAudioManager.adjustStreamVolume(
                STREAM_VOICE_CALL,
                i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }


    private void genTone() {
        for (int i = 0; i < numSamples; ++i) {
            // hz
            double freqOfTone = 400;
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }
        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * 32767));
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    public void setFrequency(double frequency) {
        int x = (int) ((double) bufferSize * frequency / sampleRate); // added
        mSampleCount = (int) ((double) x * sampleRate / frequency); // added

        samples = new byte[mSampleCount]; // changed from bufferSize

        for (int i = 0; i != mSampleCount; ++i) { // changed from bufferSize
            double t = (double) i * (1.0 / sampleRate);
            double f = Math.sin(t * 2 * Math.PI * frequency);
            samples[i] = (byte) (f * 127);
        }
    }

    private void requestAudioFocus(int stream) {
        mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    private void playSound() {
        if (audioTrack == null) {
            audioTrack = new AudioTrack(MODE_NORMAL, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            audioTrack.setLoopPoints(0, generatedSnd.length / 2, -1);
            audioTrack.play();
        }
    }

    private void stopSound() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(restartService);

    }

    private void getDeviceInfo() {

        ProxyConfig[] proxyConfigs = core.getProxyConfigList();
        if ((proxyConfigs == null) || proxyConfigs.length == 0) {
            mBinding.imgStat.setImageResource(R.drawable.led_error);
            mBinding.txtNumber.setText("설정 안됨");
            try {
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG2/data\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (ProxyConfig proxyConfig : proxyConfigs) {
                if (proxyConfig.getIdentityAddress() != null) {
                    mBinding.imgStat.setImageResource(R.drawable.led_connected);
                    mBinding.txtNumber.setText(proxyConfig.getIdentityAddress().getUsername());
                    try {
                        opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG2/data\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    private void deviceConfig() {
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
        mBinding.txtWard.setText(String.format("%s 병동", tinyDB.getString(KeyList.DEVICE_WARD)));
//        mBinding.deviceBuilding.setText(tinyDB.getString(KeyList.DEVICE_WARD));
//        mBinding.deviceFloor.setText(tinyDB.getString(KeyList.DEVICE_LOCATION));
//        mBinding.devicePart.setText(tinyDB.getString(KeyList.DEVICE_ETC));


    }

    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(getApplicationContext(), "Address null");
        }
    }

    public void newOutgoingCall(String to) {
        if (to == null) return;
        Address address = core.interpretUrl(to);
        inviteAddress(address);
    }


//    @Override
//    public void onBackPressed() {
////        onFragmentChange(0);
////        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
////        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
////            super.onBackPressed();
////        }
//        if (curView == 0) {
//            if (pressedTime == 0) {
//                NurseCallUtils.printShort(this, "Press again to exit.");
//                pressedTime = System.currentTimeMillis();
//            } else {
//                int seconds = (int) (System.currentTimeMillis() - pressedTime);
//
//                if (seconds > 2000) {
//                    NurseCallUtils.printShort(this, "Press again to exit.");
//                    pressedTime = 0;
//                } else {
//                    super.onBackPressed();
//
//                }
//            }
//
//        }
//    }

    class MainTimerTask extends TimerTask {
        public void run() {
            mHandler.post(mUpdateTimeTask);
//            Log.v(TAG, "Speaker: " + tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS) + " Handset: " + tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) + " CallStat: " + callStat);

        }

    }

    private Handler mHandler = new Handler();

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일 E요일", Locale.getDefault());
            String formatTime = timeFormat.format(date);
            String formatDate = dateFormat.format(date);
            mBinding.timeText.setText(formatTime);
            mBinding.dateText.setText(formatDate);
        }
    };

    private void initService() {
        if (PersistentService.isReady()) {
            Log.i(TAG, "PersistentService isReady()");
        } else {
            restartService = new RestartService();
            Intent intent = new Intent(this, PersistentService.class);

            IntentFilter intentFilter = new IntentFilter("com.sscctv.nursecallapp.service.PersistentService");
            Log.i(TAG, "intentFilter: " + intentFilter);

            registerReceiver(restartService, intentFilter);
            startService(intent);

        }


    }


}