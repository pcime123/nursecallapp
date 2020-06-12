package com.sscctv.nursecallapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.activity.CallActivity;
import com.sscctv.nursecallapp.ui.adapter.CallLogItem;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.PresenceBasicStatus;
import org.linphone.core.PresenceModel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class MainCallService extends Service {
    private static final String TAG = MainCallService.class.getSimpleName();
    private Core mCore;

    private static MainCallService sInstance;
    private Handler callHandler, hookHandler;
    private Timer callTimer, hookTimer;
    private CoreListenerStub mCoreListener;
    private AudioManager mAudioManager;
    private MainPreferences mPrefs;
    private boolean mCallGsmON;
    private MediaPlayer mRingerPlayer, mHookPlayer;
    private Call mRingingCall;
    private TinyDB tinyDB;
    private boolean mIsRinging;
    private boolean mAudioFocused;
    private boolean callStat, callMode;
    private DataOutputStream opt;
    private boolean isLed;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static MainCallService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    public Context getApplicationContext() {
        return this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String mBasePath = this.getFilesDir().getAbsolutePath();
        tinyDB = new TinyDB(this);
        gpioPortSet();
//        mRingSoundFile = mBasePath + "/share/sounds/linphone/rings/notes_of_the_optimistic.mkv";
//        mUserCertsPath = mBasePath + "/user-certs";
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));

//        Factory.instance().setLogCollectionPath(mBasePath);
//        Factory.instance().enableLogCollection(LogCollectionState.Disabled);
//        Factory.instance().setDebugMode(false, getString(R.string.app_name));
//        Log.i(START_LINPHONE_LOGS);
//        dumpDeviceInformation();
//        dumpInstalledLinphoneInformation();
        callHandler = new Handler();
        hookHandler = new Handler();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.w(TAG, "onCallStateChanged: " + state);

                switch (state) {
                    case IncomingReceived:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 1);

                        if (core.getCallsNb() == 1) {
                            requestAudioFocus(STREAM_RING);
                            mRingingCall = call;
                            stopMedia();
                            startRinging("call");

                            callStat = true;
                            Intent intent = new Intent(MainCallService.this, CallActivity.class);
                            intent.putExtra("call", "incoming");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        break;

                    case Connected:
                        if (core.getCallsNb() == 1) {
                            Log.d(TAG, "Call Dir: " + call.getDir());
                            if (call.getDir() == Call.Dir.Incoming) {
                                stopRinging();
                                setAudioManagerInCallMode();
                                requestAudioFocus(STREAM_VOICE_CALL);

                            }

                            callStat = true;
                        }

                        break;

                    case Error:
                    case End:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 0);
                        stopRinging();
                        if (core.getCallsNb() == 0) {
                            if (mAudioFocused) {
                                int res = mAudioManager.abandonAudioFocus(null);
                                Log.d(TAG, "[Audio Manager] Audio focus released a bit later: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                                mAudioFocused = false;
                            }
                            callStat = false;
                        }
                        break;

                    case OutgoingRinging:
                        break;

                    case OutgoingInit:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 1);

                        stopMedia();
                        setAudioManagerInCallMode();
                        requestAudioFocus(STREAM_VOICE_CALL);

                        outSpeakerMode(!tinyDB.getBoolean(KeyList.CALL_MODE));
//                        startRinging("handset");
                        callStat = true;
                        Intent intent = new Intent(MainCallService.this, CallActivity.class);
                        intent.putExtra("call", "outgoing");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;

                    case StreamsRunning:
//                        setAudioManagerInCallMode();
                        break;


//                if (call == mRingingCall && mIsRinging) {
//                    Log.d(TAG, "GOGO Stop");
//                    stopRinging();
//                }
                }

                ledCallBtn(callStat);

            }
        };

        try {
            copyIfNotExist(mBasePath + "/.rc");
            copyFromPackage(R.raw.rc_factory, "rc");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

//        mCore = Factory.instance().createCore(mBasePath + "/.rc", mBasePath + "/rc", this);
        mPrefs = MainPreferences.instance();
        mPrefs.setContext(getApplication());
        configureCore();
//        TelephonyManager mTelephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        isLed = false;
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w(TAG, "[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        Log.d(TAG, "[Audio Manager] Mode: MODE_IN_COMMUNICATION");

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void startRinging(String mode) {

        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);
        Log.d(TAG, "Start RingRing");

        try {
            requestAudioFocus(STREAM_RING);
            AssetFileDescriptor afd;
            if (mode.equals("call")) {
                afd = getAssets().openFd("basic_ring.mp3");
                if (mRingerPlayer == null) {
                    mRingerPlayer = new MediaPlayer();
                    mRingerPlayer.reset();
                    mRingerPlayer.setAudioStreamType(STREAM_RING);
                    mRingerPlayer.setLooping(true);
                    try {
                        mRingerPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mRingerPlayer.setOnPreparedListener(MediaPlayer::start);
                    mRingerPlayer.prepareAsync();

                    mRingerPlayer.setOnCompletionListener(mediaPlayer -> {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    });
                }
            }

        } catch (Exception e) {
            org.linphone.core.tools.Log.e(e, "[Audio Manager] Cannot handle incoming call");
        }
        mIsRinging = true;
    }

    private void stopRinging() {
        Log.d(TAG, "StopRingRing~");
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }

        mIsRinging = false;
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            org.linphone.core.tools.Log.d("[Audio Manager] Audio focus requested: "
                    + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                    ? "Granted"
                    : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    public boolean getCallGsmON() {
        return mCallGsmON;
    }

    public void setCallGsmON(boolean on) {
        mCallGsmON = on;
        if (on && mCore != null) {
            mCore.pauseAllCalls();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (sInstance != null) {
            return START_STICKY;
        }
        sInstance = this;
        mCore = Factory.instance().createCore(
                mPrefs.getDefaultConfig(),
                mPrefs.getFactoryConfig(),
                this);
        mCore.addListener(mCoreListener);

        mCore.setRing(null);
//        mCore.setRingback(null);
//        mCore.setRingDuringIncomingEarlyMedia(false);
//        mCore.setRingerDevice(null);
//        mCore.setRemoteRingbackTone(null);

        mCore.start();
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        callHandler.post(
                                () -> {
                                    if (mCore != null) {
                                        mCore.iterate();
                                    }
                                });
                    }
                };
        callTimer = new Timer("NurseCall scheduler");
        callTimer.schedule(lTask, 0, 20);

        TimerTask hookTask = new TimerTask() {
            @Override
            public void run() {
                hookHandler.post(
                        () -> {
//                            Log.d(TAG, getAudioMode(mAudioManager.getMode()) + " | CallMode: " + tinyDB.getBoolean(KeyList.CALL_MODE) + " | Call: " + callStat + " | isSpeaker: " + mAudioManager.isSpeakerphoneOn());
                            callMode = tinyDB.getBoolean(KeyList.CALL_MODE);
                            if (!callStat) {
                                if (callMode) {
                                    playMedia();
                                } else {
                                    stopMedia();
                                    ledCallBtn(callStat);
                                }

                                if (mAudioManager.getMode() == MODE_NORMAL) {
                                    outSpeakerMode(!callMode);
                                }

                            } else {

//                                if (mAudioManager.getMode() == MODE_NORMAL) {
//                                    outSpeakerMode(!callMode);
//                                }
                            }



//                            if (mAudioManager.getMode() == MODE_NORMAL) {
//                                if (callStat) {
//                                    if (tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) && !tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//                                        outSpeakerMode(false);
//                                    } else if (!tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) && tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//                                        outSpeakerMode(true);
//                                    }
//                                } else {
//                                    if (!tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) && !tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//                                        outSpeakerMode(false);
//                                    }
//                                }
//                            }
//
//
//                            if (mAudioManager.getMode() == MODE_IN_COMMUNICATION) {
//                                if (tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS) && !tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//                                    outSpeakerMode(false);
////                                    tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, false);
//                                    if(CallActivity.mContext != null) {
//                                        ((CallActivity) CallActivity.mContext).changeSpeakerMode(false);
//                                    }
//                                }
//                            }
                        });
            }
        };
        hookTimer = new Timer("NurseCall Hook");
        hookTimer.schedule(hookTask, 0, 500);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        callTimer.cancel();
        hookTimer.cancel();
        mCore.stop();
        mCore = null;
        sInstance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(TAG, userCerts + " can't be created.");
            }
        }


//        mCore.setUserCertificatesPath(userCerts);
//        mCore.setCallLogsDatabasePath(mCallLogDatabaseFile);
//        enableDeviceRingtone(mPrefs.isDeviceRingtoneEnabled());
    }

    private String getAudioMode(int mode) {
        switch (mode) {
            case 0:
                return "MODE_NORMAL";
            case 1:
                return "MODE_RINGTONE";
            case 2:
                return "MODE_IN_CALL";
            case 3:
                return "MODE_IN_COMMUNICATION";
        }
        return "Null: " + mode;
    }

//    public void enableDeviceRingtone(boolean use) {
//        if (use) {
//            Uri inAppsoundUri = Uri.parse("file:///android_asset/basic_ring.mp3");
//            mCore.setRing(inAppsoundUri.toString());
//            mCore.setRemoteRingbackTone(inAppsoundUri.toString());
//        } else {
//            mCore.setRing(mRingSoundFile);
//        }
//    }

//    private void dumpDeviceInformation() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
//        sb.append("MODEL=").append(Build.MODEL).append("\n");
//        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
//        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
//        sb.append("Supported ABIs=");
//        for (String abi : Version.getCpuAbis()) {
//            sb.append(abi).append(", ");
//        }
//        sb.append("\n");
//        Log.i(TAG, sb.toString());
//    }
//
//    private void dumpInstalledLinphoneInformation() {
//        PackageInfo info = null;
//        try {
//            info = getPackageManager().getPackageInfo(getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException nnfe) {
//            nnfe.printStackTrace();
//        }
//
//        if (info != null) {
//            Log.i(TAG,
//                    "[Service] Linphone version is " +
//                            info.versionName + " (" + info.versionCode + ")");
//        } else {
//            Log.i(TAG, "[Service] Linphone version is unknown");
//        }
//    }

    private void copyIfNotExist(String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(R.raw.rc_default, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public void changeStatusToOnline() {
        if (mCore == null) return;
        PresenceModel model = mCore.createPresenceModel();
        model.setBasicStatus(PresenceBasicStatus.Open);
        mCore.setPresenceModel(model);
    }

    private void ledCallBtn(boolean mode) {
        if(isLed == mode) {
            return;
        }

        try {
            if (mode) {
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG4/data\n");
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG3/data\n");
            } else {
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG4/data\n");
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG3/data\n");
            }
            isLed = mode;
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void playMedia() {
        if (mHookPlayer == null) {
            Log.w(TAG, "PlayMedia()");
            mAudioManager.setMode(MODE_NORMAL);
            requestAudioFocus(STREAM_VOICE_CALL);
            try {
                AssetFileDescriptor afd = getAssets().openFd("freq_440hz_1sec.wav");
                mHookPlayer = new MediaPlayer();
                mHookPlayer.reset();
                mHookPlayer.setAudioStreamType(MODE_NORMAL);
                mHookPlayer.setLooping(true);
                try {
                    mHookPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHookPlayer.setOnPreparedListener(MediaPlayer::start);
                mHookPlayer.prepareAsync();

                mHookPlayer.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                });

            } catch (Exception e) {
                org.linphone.core.tools.Log.e(e, "[Audio Manager] Cannot handle incoming call");
            }
            ledCallBtn(true);
        }

    }

    private void stopMedia() {
        if (mHookPlayer != null) {
            Log.w(TAG, "stopMedia " + mHookPlayer);
            mHookPlayer.stop();
            mHookPlayer.release();
            mHookPlayer = null;

            ledCallBtn(false);
        }
    }

    private void outSpeakerMode(boolean mode) {
        if (mode == mAudioManager.isSpeakerphoneOn()) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(mode);
    }

    private void writeCallLog(String type, String name, boolean stat) {

        ArrayList<CallLogItem> items = new ArrayList<>();

        if(stat) {
//            items.add(new CallLogItem("normal", type, name, ))
            NurseCallUtils.putCallLog(tinyDB, KeyList.CALL_LOG, items);
        }
    }

}
