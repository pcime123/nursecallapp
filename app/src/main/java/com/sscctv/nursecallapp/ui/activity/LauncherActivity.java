package com.sscctv.nursecallapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.service.ServiceWaitThread;
import com.sscctv.nursecallapp.service.ServiceWaitThreadListener;
import com.sscctv.nursecallapp.ui.setup.SetupStepSplash;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;

public class LauncherActivity extends AppCompatActivity implements ServiceWaitThreadListener {
    private static final String TAG = "LauncherActivity";
    private TinyDB tinyDB;

    private DataOutputStream opt;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        Log.d(TAG, "onCreate");
        tinyDB = new TinyDB(this);
        mAudioManager = ((AudioManager) Objects.requireNonNull(getApplicationContext()).getSystemService(Context.AUDIO_SERVICE));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!doFirst()) {
            if (MainCallService.isReady()) {
                Log.i(TAG, "MainCallService isReady()");
                onServiceReady();
            } else {
                Log.e(TAG, "MainCallService is Not Ready()");
                startService(new Intent().setClass(this, MainCallService.class));
                new ServiceWaitThread(this).start();
            }
        }

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");


    }

    @Override
    protected void onResume() {
        super.onResume();
        GpioPortSet();
        setEchoCanceller();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        NurseCallUtils.sendRefreshTimer(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

    }

    @Override
    public void onServiceReady() {
        Intent intent = new Intent();
        intent.setClass(LauncherActivity.this, MainActivity.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(Objects.requireNonNull(getIntent()).getAction());
        intent.setType(getIntent().getType());
        startActivity(intent);

        MainCallService.getInstance().changeStatusToOnline();
    }

    private boolean doFirst() {
        TinyDB tinyDB = new TinyDB(this);
        boolean val = tinyDB.getBoolean(KeyList.FIRST_KEY);

        if (!val) {
            defaultSettings();
            Intent intent = new Intent(this, SetupStepSplash.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void defaultSettings() {

        tinyDB.putBoolean(KeyList.CALL_SPEAKER_MODE, getResources().getBoolean(R.bool.first_call_mode));
        tinyDB.putInt(KeyList.SCREEN_CHANGE_TIME, 0);
        tinyDB.putInt(KeyList.SCREEN_CHANGE_POS, 0);
        tinyDB.putBoolean(KeyList.MISSED_CALL, getResources().getBoolean(R.bool.missed_call_mode));
        tinyDB.putBoolean(KeyList.FIRST_BED_FRAG, getResources().getBoolean(R.bool.first_bed_frag));
        tinyDB.putBoolean(KeyList.FIRST_SIP_SETTINGS, getResources().getBoolean(R.bool.first_sip_settings));

        tinyDB.putString(KeyList.KEY_MULTICAST_IP, getResources().getString(R.string.first_multicast_ip));
        tinyDB.putString(KeyList.KEY_MULTICAST_PORT, getResources().getString(R.string.first_multicast_port));
        tinyDB.putString(KeyList.KEY_TCP_PORT, getResources().getString(R.string.first_tcp_port));

        tinyDB.putInt(KeyList.CALL_LOG_MAX, getResources().getInteger(R.integer.default_log_size));

        tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, getResources().getInteger(R.integer.default_speaker_volume));
        tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, getResources().getInteger(R.integer.default_headset_volume));
        tinyDB.putInt(KeyList.KEY_RING_VOLUME, getResources().getInteger(R.integer.default_ring_volume));

        getSaveFolder();
        createLogoFile();
        setDefaultVolume();
    }

    private void getSaveFolder() {
        File logoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE);
        if(!logoFile.exists()) {
            logoFile.mkdirs();
        }
    }

    private void createLogoFile() {
        AssetManager manager = getAssets();

        Bitmap bitmap;
        try {
            InputStream is = manager.open("logo.png");
            bitmap = BitmapFactory.decodeStream(is);
            saveImage(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImage(Bitmap finalBitmap) {


        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE + File.separator + "logo.png");
        try {
            FileOutputStream out = new FileOutputStream(myDir);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEchoCanceller() {
        setEchoBypass();
        setSpeakerGain(tinyDB.getString(KeyList.ECHO_FRONT_SPEAKER));
        setMicGain(tinyDB.getString(KeyList.ECHO_FRONT_MIC));
        resetEcho();

    }

    private void GpioPortSet() {
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

    private void setEchoBypass() {
        if (tinyDB.getBoolean(KeyList.ECHO_FRONT_BYPASS)) {
            try {
                opt.writeBytes("echo 0300 c00a > /proc/hbi/dev_145/write_reg\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, true);
        } else {
            try {
                opt.writeBytes("echo 0300 c008 > /proc/hbi/dev_145/write_reg\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, false);
        }
    }

    private void setSpeakerGain(String val) {

        try {
            opt.writeBytes("echo " + NurseCallUtils.ECHO_SPEAKER_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setMicGain(String val) {

        try {
            Log.d(TAG, "echo " + NurseCallUtils.ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
            opt.writeBytes("echo " + NurseCallUtils.ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void resetEcho() {
        try {
            opt.writeBytes("echo " + NurseCallUtils.ECHO_RESET + " 0002 > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultVolume() {

        mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);
        mAudioManager.setStreamVolume(STREAM_RING, tinyDB.getInt(KeyList.KEY_RING_VOLUME), 0);

    }


}
