package com.sscctv.nursecallapp.ui.settings;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsSipAccountBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;


public class SettingsSipAccount extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private SettingsSipAccountBinding mBinding;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_sip_account);
        tinyDB = new TinyDB(this);
        core = MainCallService.getCore();
        mAccountCreator = core.createAccountCreator(null);

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String msg) {
                super.onRegistrationStateChanged(core, cfg, state, msg);
                if(state == RegistrationState.Ok) {
                    tinyDB.putString(KeyList.SIP_ID, mBinding.editSipId.getText().toString());
                    tinyDB.putString(KeyList.SIP_PW, mBinding.editSipPw.getText().toString());
                    tinyDB.putString(KeyList.SIP_DOMAIN, mBinding.editSipDomain.getText().toString());
                } else {
                    tinyDB.putString(KeyList.SIP_ID, "");
                    tinyDB.putString(KeyList.SIP_PW, "");
                    tinyDB.putString(KeyList.SIP_DOMAIN, "");
                }
                switch (state) {
                    case Ok:

                        mBinding.txtSip.setText(String.format("OK: %s", msg));
                        break;
                    case Failed:
                        mBinding.txtSip.setText(String.format("Failed: %s", msg));
                        break;
                    case Progress:
                        mBinding.txtSip.setText(String.format("Progress: %s", msg));
                        break;
                    case Cleared:
                        mBinding.txtSip.setText(String.format("Cleared: %s", msg));
                        break;
                    case None:
                        mBinding.txtSip.setText(String.format("None: %s", msg));
                        break;

                }

            }
        };

        mBinding.btnSipSetup.setOnClickListener(view -> {
            cfgAccount();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        core.addListener(mCoreListener);
        setTestAccount();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        core.removeListener(mCoreListener);
    }


    private void cfgAccount() {
        mAccountCreator.setUsername(mBinding.editSipId.getText().toString());
        mAccountCreator.setPassword(mBinding.editSipPw.getText().toString());
        mAccountCreator.setDomain(mBinding.editSipDomain.getText().toString());
        mAccountCreator.setDisplayName(mBinding.editSipId.getText().toString());
        mAccountCreator.setTransport(TransportType.Udp);

        core.clearProxyConfig();
        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();
        core.setDefaultProxyConfig(proxyConfig);
    }

    private void setTestAccount() {
        mBinding.editSipId.setText("2000");
        mBinding.editSipPw.setText("Sscctv2000");
        mBinding.editSipDomain.setText("175.195.153.235");
    }
}
