package com.sscctv.nursecallapp.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.animation.ObjectAnimator;
import android.util.Property;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityBackgroundBinding;
import com.sscctv.nursecallapp.databinding.TestPageBinding;
import com.sscctv.nursecallapp.service.MainPreferences;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundActivity extends AppCompatActivity {
    private static final String TAG = "BackgroundActivity";
    private Core core;
    private int curView;
    private MainPreferences mPrefs;
    private TinyDB tinyDB;
    private ActivityBackgroundBinding mBinding;
    private long dateTime;
    private boolean isChange = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_background);
        tinyDB = new TinyDB(this);

        mBinding.backLayout.setOnTouchListener((view, motionEvent) -> {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                finish();
                NurseCallUtils.sendRefreshTimer(this, 0);
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
                handler.post(mUpdateTimeTask);
                timer_sec++;
                handler.post(() -> {
                    if(!isChange) {
                        mBinding.colon.setVisibility(View.INVISIBLE);
                        isChange = true;
                    } else {
                        mBinding.colon.setVisibility(View.VISIBLE);
                        isChange = false;
                    }
                });

            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 500);
    }

    private Runnable mUpdateTimeTask = new Runnable() {

        public void run() {
            dateTime = System.currentTimeMillis();
            Date date = new Date(dateTime);
            SimpleDateFormat fHour = new SimpleDateFormat("HH", Locale.KOREA);
            SimpleDateFormat fMinute = new SimpleDateFormat("mm", Locale.KOREA);

            String strHour = fHour.format(date);
            String strMinute = fMinute.format(date);

            mBinding.hour.setText(strHour);
            mBinding.minute.setText(strMinute);
        }
    };
}
