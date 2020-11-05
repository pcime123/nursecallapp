package com.sscctv.nursecallapp.ui.setup;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupSplashBinding;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class SetupStepSplash extends AppCompatActivity {
    private static final String TAG = "SetupStepType";

    private ActivitySetupSplashBinding mBinding;
    private AlarmManager alarm;
    private Animation animFadeIn, animFadeOut;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_splash);

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        mBinding.btnNext.setOnClickListener(view -> {
            ctrlHandler("next", 0);

        });
//        changeDefaultPassword();

        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("su -c ime enable com.google.android.inputmethod.korean/.KoreanIme");
        } catch (IOException e) {
            e.printStackTrace();
        }

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);

        mHandler = new Handler();


    }

    @Override
    protected void onResume() {
        switchLanguage(new Locale("ko"));
        alarm.setTimeZone("GMT+9:00");
        ctrlHandler("step1", 0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void ctrlHandler(String mode, int time) {
        mHandler.postDelayed(() -> {

            switch (mode) {
                case "step1":
                    mBinding.splashTitle.startAnimation(animFadeIn);
                    mBinding.splashTitle.setVisibility(View.VISIBLE);
                    mBinding.splashContent.setVisibility(View.GONE);
                    mBinding.btnNext.setVisibility(View.GONE);
                    ctrlHandler("step2", 3000);
                    break;

                case "step2":
                    mBinding.splashTitle.setAnimation(animFadeOut);
                    ctrlHandler("step3", 2000);
                    break;

                case "step3":
                    mBinding.splashTitle.setVisibility(View.GONE);
                    ctrlHandler("step4", 100);
                    break;

                case "step4":
                    mBinding.splashContent.startAnimation(animFadeIn);
                    mBinding.btnNext.startAnimation(animFadeIn);
                    mBinding.splashContent.setVisibility(View.VISIBLE);
                    mBinding.btnNext.setVisibility(View.VISIBLE);
                    break;

                case "next":
                    mBinding.splashContent.startAnimation(animFadeOut);
                    mBinding.btnNext.startAnimation(animFadeOut);
                    ctrlHandler("step5", 2000);
                    break;

                case "step5":
                    mBinding.splashContent.setVisibility(View.GONE);
                    mBinding.btnNext.setVisibility(View.GONE);
                    ctrlHandler("setFrag", 100);
                    break;

                case "setFrag":
                    startActivity(new Intent(this, SetupStepType.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("su -c ime set com.google.android.inputmethod.korean/.KoreanIme");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                    break;
            }

        }, time);
    }


    private void switchLanguage(Locale language) {

        try {
            Locale locale = language;
            Class amnClass = Class.forName("android.app.ActivityManagerNative");
            Object amn = null;
            Configuration config = null;

            Method methodGetDefault = amnClass.getMethod("getDefault");
            methodGetDefault.setAccessible(true);
            amn = methodGetDefault.invoke(amnClass);

            Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
            methodGetConfiguration.setAccessible(true);
            config = (Configuration) methodGetConfiguration.invoke(amn);

            Class configClass = config.getClass();
            Field f = configClass.getField("userSetLocale");
            f.setBoolean(config, true);

            config.locale = locale;

            Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration", Configuration.class);
            methodUpdateConfiguration.setAccessible(true);
            methodUpdateConfiguration.invoke(amn, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeDefaultPassword() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if (!dir.exists()) {
            dir.mkdir();
            return;
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PASSWORD);
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            Log.i(TAG, "Start Write: " + "1234");
            bufferedWriter.write(EncryptionUtil.encode("1234"));
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
