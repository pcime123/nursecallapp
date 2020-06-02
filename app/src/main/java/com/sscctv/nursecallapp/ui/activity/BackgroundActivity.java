package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityBackgroundBinding;
import com.sscctv.nursecallapp.databinding.TestPageBinding;
import com.sscctv.nursecallapp.service.MainPreferences;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundActivity extends AppCompatActivity {
    private static final String TAG = "BackgroundActivity";
    private Core core;
    private int curView;
    private MainPreferences mPrefs;
    private TinyDB tinyDB;
    private ActivityBackgroundBinding mBinding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_background);
        tinyDB = new TinyDB(this);

        mBinding.backLayout.setOnTouchListener((view, motionEvent) -> {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                NurseCallUtils.printShort(getApplicationContext(), "TOUCH!!");
                finish();
                NurseCallUtils.sendRefreshTimer(this);
                return true;
            }
            return false;

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        testStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (second != null) {
            second.cancel();
            second = null;
        }
    }

    private TimerTask second;
    private final Handler handler = new Handler();
    private int timer_sec;
    public void testStart() {

        timer_sec = 0;
        second = new TimerTask() {
            @Override
            public void run() {
                Update();
                timer_sec++;
            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1000);
    }
    protected void Update() {
        Runnable updater = () -> {
//                if(timer_sec == 5) {
//                    mBinding.backLayout.setBackgroundColor(Color.RED);
//                } else if(timer_sec == 10) {
//                    mBinding.backLayout.setBackgroundColor(Color.CYAN);
//                }
        };
        handler.post(updater);
    }
}
