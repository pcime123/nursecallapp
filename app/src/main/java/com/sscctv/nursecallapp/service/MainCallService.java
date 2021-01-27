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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.activity.BackgroundActivity;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_SYSTEM;
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
    private TinyDB tinyDB;
    private boolean mAudioFocused;
    private boolean callStat, isHook;
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
        ledCallBtn(false);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        callHandler = new Handler();
        hookHandler = new Handler();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.w(TAG, "onCallStateChanged: " + state);

                switch (state) {
                    case IncomingReceived:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 1);

                        Intent missedIntent = new Intent(getApplicationContext(), MissedCallService.class);
                        missedIntent.putExtra("msg", NurseCallUtils.CALL_INCOMING);
                        startService(missedIntent);

                        if (core.getCallsNb() == 1) {
                            callStat = true;

                            requestAudioFocus(STREAM_RING);
                            stopMedia();
                            startRingTone();
                            sendMessage(NurseCallUtils.CALL_INCOMING);

                        } else if (1 < core.getCallsNb() && core.getCallsNb() < 6) {
                            sendMessage(NurseCallUtils.CALL_NEW_INCOMING + core.getCallsNb());
                        }

                        break;
                    case Connected:
                        if (core.getCallsNb() == 1) {
                            Log.d(TAG, "Call Dir: " + call.getDir());
                            if (call.getDir() == Call.Dir.Incoming) {
                                stopRingTone();
                                setAudioManagerInCallMode();
                                requestAudioFocus(STREAM_VOICE_CALL);
                                ledCallBtn(true);

                            } else if (call.getDir() == Call.Dir.Outgoing) {
                                sendMessage(NurseCallUtils.CALL_NORMAL);
                            }
                            callStat = true;
                            sendMessage(NurseCallUtils.CALL_CONNECTED);
                        } else {
                            Log.d(TAG, "Connected Call Nb: " + core.getCallsNb());
                            Log.d(TAG, "Connected Call: " + call.getRemoteAddress().getUsername());
                        }

                        break;

                    case Error:
                    case End:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 0);
                        for(int i = 0; i< mCore.getCalls().length; i++) {
                            Log.d(TAG, "Call: " + mCore.getCalls()[i].getRemoteAddress().getDisplayName() + " Status: " + mCore.getCalls()[i].getState());
                        }

                        if (core.getCallsNb() == 0) {
                            ledCallBtn(false);
                            stopRingTone();
                            if (mAudioFocused) {
                                int res = mAudioManager.abandonAudioFocus(null);
                                mAudioFocused = false;
                            }
                            tinyDB.putBoolean(KeyList.CALL_SPEAKER_MODE, false);
                            sendMessage(NurseCallUtils.CALL_DECLINE);
                            callStat = false;

                        } else if(core.getCallsNb() > 1){
                            sendMessage(NurseCallUtils.CALL_NEW_DECLINE);
                        } else if(core.getCallsNb() == 1) {
                            sendMessage(NurseCallUtils.CALL_CALL_END);
                        }
                        break;
                    case Released:
                        if (core.getCallsNb() == 0) {
                            if ((core.getCallLogs()[0].getDir() == Call.Dir.Incoming && core.getCallLogs()[0].getStatus() == Call.Status.Aborted) || tinyDB.getBoolean(KeyList.MISSED_CALL)) {
                                Intent intent = new Intent(getApplicationContext(), MissedCallService.class);
                                intent.putExtra("msg", core.getCallLogs()[0].getRemoteAddress().getUsername());
                                tinyDB.putBoolean(KeyList.MISSED_CALL, true);
                                startService(intent);
                            }
                        } else {
                            Log.d(TAG, "Released Call Nb: " + core.getCallsNb());
                            Log.d(TAG, "Released Call: " + call.getRemoteAddress().getUsername());

                        }
                        break;
                    case OutgoingProgress:
                    case OutgoingRinging:
                        sendMessage(NurseCallUtils.CALL_OUTGOING);
                        break;

                    case OutgoingInit:
                        NurseCallUtils.sendRefreshTimer(MainCallService.this, 1);
                        stopMedia();
                        setAudioManagerInCallMode();
                        requestAudioFocus(STREAM_VOICE_CALL);
                        callStat = true;
                        break;
                    case StreamsRunning:
                        break;
                }
            }
        };

        try {
            copyIfNotExist(mBasePath + "/.rc");
            copyFromPackage(R.raw.rc_factory, "rc");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        mPrefs = MainPreferences.instance();
        mPrefs.setContext(getApplication());
        configureCore();

        isLed = false;
    }

    private void startRingTone() {

        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);
        Log.d(TAG, "Start RingRing");
        requestAudioFocus(STREAM_RING);
        try {
            AssetFileDescriptor afd;
            afd = getApplicationContext().getAssets().openFd("basic_ring.mp3");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopRingTone() {
        Log.d(TAG, "StopRingRing~");
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            return;
        }
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            if (mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocused = true;
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


        if (mCore == null) {

            mCore = Factory.instance().createCore(
                    mPrefs.getDefaultConfig(),
                    mPrefs.getFactoryConfig(),
                    sInstance);
        }

        mCore.addListener(mCoreListener);
        mCore.setRing(null);
        mCore.setMaxCalls(2);
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
//                            Log.d(TAG,  "isHook: " + tinyDB.getBoolean(KeyList.CALL_HOOK) + " | Call: " + callStat + " | isSpeaker: " + mAudioManager.isSpeakerphoneOn());
                            isHook = tinyDB.getBoolean(KeyList.CALL_HOOK);
                            if (!callStat) {
                                if (mAudioManager.getMode() == MODE_NORMAL) {
                                    outSpeakerMode(!isHook);
                                }

                                if (isHook) {
                                    if (mHookPlayer != null && !mHookPlayer.isPlaying()) {
                                        mHookPlayer.start();
//                                        Log.d(TAG, "Start!!");
                                    } else {
                                        playMedia();
//                                        Log.d(TAG, "First Start!!");
                                        ledCallBtn(true);
                                    }
                                } else {
//                                    stopMedia();
                                    if (mHookPlayer != null && mHookPlayer.isPlaying()) {
                                        mHookPlayer.pause();
//                                        Log.d(TAG, "Pause!!");
                                        ledCallBtn(false);
                                    }
                                }


                            }

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
//        mCore = null;
        sInstance = null;
        stopMedia();
        stopRingTone();
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
    }


    private void copyIfNotExist(String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(R.raw.rc_default, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int resourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(resourceId);
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

        try {
            if (mode) {
//                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG2/data\n");
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG3/data\n");
            } else {
//                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG2/data\n");
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG3/data\n");
            }
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
//            Log.w(TAG, "PlayMedia()");
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setMode(STREAM_MUSIC);
            requestAudioFocus(STREAM_MUSIC);

            try {
                AssetFileDescriptor afd = getAssets().openFd("freq_440hz_1sec.wav");
                mHookPlayer = new MediaPlayer();
                mHookPlayer.setAudioStreamType(STREAM_MUSIC);
                mHookPlayer.setLooping(true);
                try {
                    mHookPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mHookPlayer.prepare();
                    mHookPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void stopMedia() {
        if (mHookPlayer != null) {
//            Log.w(TAG, "stopMedia " + mHookPlayer);
            mHookPlayer.stop();
            mHookPlayer.reset();
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

    private void sendMessage(String message) {
//        Log.d(TAG, "Get Call Service Message: " + message);
        Intent intent = new Intent("call_start");
        intent.putExtra("msg", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
