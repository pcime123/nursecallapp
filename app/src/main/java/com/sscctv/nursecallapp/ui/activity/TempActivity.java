package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityMainBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;


public class TempActivity extends AppCompatActivity {

    private static final String TAG = "TempActivity";
    private Core core;
    private TinyDB tinyDB;
    private ActivityMainBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        tinyDB = new TinyDB(this);
        core = MainCallService.getCore();

        ProxyConfig[] proxyConfigs = core.getProxyConfigList();
        for (ProxyConfig proxyConfig : proxyConfigs) {
            Log.d(TAG, "Number: " + NurseCallUtils.getDisplayableAddress(proxyConfig.getIdentityAddress()));
        }

        mBinding.btnAccount.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, AccountActivity.class);
        });
        mBinding.btnSettings.setOnClickListener(view -> {
//            NurseCallUtils.startIntent(this, _SettingsActivity.class);
        });

        mBinding.btnPbxEnter.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, PbxLoginActivity.class);
        });

        mBinding.btnCsv.setOnClickListener(view -> NurseCallUtils.startIntent(this, CSVScanActivity.class));
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

}