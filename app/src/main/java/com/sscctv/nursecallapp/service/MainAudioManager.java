package com.sscctv.nursecallapp.service;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.sscctv.nursecallapp.receivers.HeadsetReceiver;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.io.FileInputStream;
import java.io.IOException;

import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class MainAudioManager {
    private static final String TAG = "MainAudioManager";
    private Context mContext;
    private AudioManager mAudioManager;
    private Call mCall;
    private MediaPlayer mPlayer;
    private boolean mHeadsetReceiverRegistered;

    private boolean mIsRinging;
    private boolean mAudioFocused;
    private boolean mEchoTesterIsRunning;
    private boolean mIsBluetoothHeadsetConnected;
    private boolean mIsBluetoothHeadsetScoConnected;

    private CoreListenerStub mListener;

    public MainAudioManager(Context context) {
        mContext = context;
        mAudioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        mEchoTesterIsRunning = false;
        mHeadsetReceiverRegistered = false;

        mListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State cstate, String message) {
                super.onCallStateChanged(core, call, cstate, message);
                Log.d(TAG, "onCallStateChanged: " + cstate);
                if(cstate == Call.State.IncomingReceived) {
                    if (core.getCallsNb() == 1) {
                        requestAudioFocus(AudioManager.STREAM_RING);
                        mCall = call;
                        startRinging(call.getRemoteAddress());
                    }
                } else if(call == mCall && mIsRinging) {
                    stopRinging();
                }

                if(cstate == Call.State.Connected) {
                    if(core.getCallsNb() == 1) {
                        if(call.getDir() == Call.Dir.Incoming) {
                            setAudioManagerInCallMode();
                            requestAudioFocus(STREAM_VOICE_CALL);
                        }
//                        if()
                    }
                }
            }
        };
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res =
                    mAudioManager.requestAudioFocus(
                            null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d(TAG,
                    "[Audio Manager] Audio focus requested: "
                            + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                            ? "Granted"
                            : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    private synchronized void startRinging(Address remoteAddress) {
//        if (!LinphonePreferences.instance().isDeviceRingtoneEnabled()) {
            // Enable speaker audio route, linphone library will do the ringing itself automatically
//            routeAudioToSpeaker();
//            return;
//        }

//        boolean doNotDisturbPolicyAllowsRinging =
//                Compatibility.isDoNotDisturbPolicyAllowingRinging(mContext, remoteAddress);
//        if (!doNotDisturbPolicyAllowsRinging) {
//            Log.e("[Audio Manager] Do not ring as Android Do Not Disturb Policy forbids it");
//            return;
//        }

        routeAudioToSpeaker();
        mAudioManager.setMode(MODE_RINGTONE);

        try {
            if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
                    || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
//                    && mVibrator != null
//                    && LinphonePreferences.instance().isIncomingCallVibrationEnabled()
            ) {
//                long[] patern = {0, 1000, 1000};
//                mVibrator.vibrate(patern, 1);
            }
            if (mPlayer == null) {
                requestAudioFocus(STREAM_RING);
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(STREAM_RING);

                String ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
                try {
                    if (ringtone.startsWith("content://")) {
                        mPlayer.setDataSource(mContext, Uri.parse(ringtone));
                    } else {
                        FileInputStream fis = new FileInputStream(ringtone);
                        mPlayer.setDataSource(fis.getFD());
                        fis.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "[Audio Manager] Cannot set ringtone");
                }

                mPlayer.prepare();
                mPlayer.setLooping(true);
                mPlayer.start();
            } else {
                Log.w(TAG, "[Audio Manager] Already ringing");
            }
        } catch (Exception e) {
            Log.e(TAG, "[Audio Manager] Cannot handle incoming call");
        }
        mIsRinging = true;
    }

    private synchronized void stopRinging() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
//        if (mVibrator != null) {
//            mVibrator.cancel();
//        }

        mIsRinging = false;
    }
    public void routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true);
    }

    public void routeAudioToEarPiece() {
        routeAudioToSpeakerHelper(false);
    }

    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        Log.w(TAG, "[Audio Manager] Routing audio to " + (speakerOn ? "speaker" : "earpiece"));
        mAudioManager.setSpeakerphoneOn(speakerOn);
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.e(TAG,"[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        Log.d(TAG,"[Audio Manager] Mode: MODE_IN_COMMUNICATION");

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public boolean onKeyVolumeAdjust(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustVolume(1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            adjustVolume(-1);
            return true;
        }
        return false;
    }

    private void adjustVolume(int i) {
        if (mAudioManager.isVolumeFixed()) {
            Log.e(TAG,"[Audio Manager] Can't adjust volume, device has it fixed...");
            // Keep going just in case...
        }

        int stream = STREAM_VOICE_CALL;
        if (mIsBluetoothHeadsetScoConnected) {
            Log.i(TAG, "[Audio Manager] Bluetooth is connected, try to change the volume on STREAM_BLUETOOTH_SCO");
            stream = 6; // STREAM_BLUETOOTH_SCO, it's hidden...
        }

        mAudioManager.adjustStreamVolume(stream, i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

//
//    private void enableHeadsetReceiver() {
//        mHeadsetReceiver = new HeadsetReceiver();
//
//        org.linphone.core.tools.Log.i("[Audio Manager] Registering headset receiver");
//        mContext.registerReceiver(
//                mHeadsetReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
//        mContext.registerReceiver(
//                mHeadsetReceiver, new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
//        mHeadsetReceiverRegistered = true;
//    }
}
