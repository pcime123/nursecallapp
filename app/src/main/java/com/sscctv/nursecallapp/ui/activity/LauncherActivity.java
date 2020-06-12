package com.sscctv.nursecallapp.ui.activity;

import android.content.Intent;
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
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.File;
import java.util.Objects;

public class LauncherActivity extends AppCompatActivity implements ServiceWaitThreadListener {
    private static final String TAG = "LauncherActivity";
    private TinyDB tinyDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        Log.d(TAG, "onCreate");
        tinyDB = new TinyDB(this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!doFirst()){
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

        if(!dir.exists()){
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

        Log.d(TAG, "onResume");


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
        Log.d(TAG, "Start doFirst");
        TinyDB tinyDB = new TinyDB(this);
        boolean val = tinyDB.getBoolean(KeyList.FIRST_KEY);
        Log.d(TAG, "Value doFirst: " + val);

        if(!val) {
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

        tinyDB.putBoolean(KeyList.CALL_MODE, getResources().getBoolean(R.bool.first_call_mode));
        tinyDB.putInt(KeyList.SCREEN_CHANGE_TIME, 0);
        tinyDB.putInt(KeyList.SCREEN_CHANGE_POS, 0);
//        tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, getResources().getBoolean(R.bool.first_speaker_button));
//        tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, getResources().getBoolean(R.bool.first_speaker_button));
    }


}
