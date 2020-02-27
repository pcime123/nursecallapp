package com.sscctv.nursecallapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.sscctv.nursecallapp.service.MainCallService;

public class LauncherActivity extends AppCompatActivity {
    private Handler mHandler;

    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher);

        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Launcher - " + MainCallService.isReady());
        // Check whether the Service is already running
        if (MainCallService.isReady()) {
            onServiceReady();
        } else {
            Log.d(TAG, "startService");
            // If it's not, let's start it
            startService(
                    new Intent().setClass(this, MainCallService.class));
            // And wait for it to be ready, so we can safely use it afterwards
            new ServiceWaitThread().start();
        }
    }

    private void onServiceReady() {
        Log.d(TAG, "onServiceReady");
        // Once the service is ready, we can move on in the application
        // We'll forward the intent action, type and extras so it can be handled
        // by the next activity if needed, it's not the launcher job to do that
        Intent intent = new Intent();
        intent.setClass(LauncherActivity.this, MainActivity.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(getIntent().getAction());
        intent.setType(getIntent().getType());
        startActivity(intent);
    }

    // This thread will periodically check if the Service is ready, and then call onServiceReady
    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!MainCallService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            // As we're in a thread, we can't do UI stuff in it, must post a runnable in UI thread
            mHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            onServiceReady();
                        }
                    });
        }
    }
}
