package com.sscctv.nursecallapp.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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
import java.io.OutputStreamWriter;

public class SetupStepSplash extends AppCompatActivity {
    private static final String TAG = "SetupStepType";

    private ActivitySetupSplashBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_splash);

        mBinding.btnNext.setOnClickListener(view -> {
            NurseCallUtils.startNewIntent(this, SetupStepType.class);
        });
//        changeDefaultPassword();
    }

    @Override
    protected void onResume() {
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
