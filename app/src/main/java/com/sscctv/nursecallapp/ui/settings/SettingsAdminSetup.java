package com.sscctv.nursecallapp.ui.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsAdminBinding;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;


public class SettingsAdminSetup extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private SettingsAdminBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_admin);

        mBinding.btnAdminPbx.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SettingsPbxServer.class);
        });

        mBinding.btnAdminBed.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SettingsAdminBed.class);
        });
    }
}
