package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityAccountBinding;
import com.sscctv.nursecallapp.service.MainCallService;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private ActivityAccountBinding mBinding;
    private Core core;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_account);
        core = MainCallService.getCore();
        mAccountCreator = core.createAccountCreator(null);

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String msg) {
                super.onRegistrationStateChanged(core, cfg, state, msg);
                switch (state) {
                    case Ok:
                        mBinding.txtResult.setText(String.format("OK: %s", msg));
                        break;
                    case Failed:
                        mBinding.txtResult.setText(String.format("Failed: %s", msg));
                        break;
                    case Progress:
                        mBinding.txtResult.setText(String.format("Progress: %s", msg));
                        break;
                    case Cleared:
                        mBinding.txtResult.setText(String.format("Cleared: %s", msg));
                        break;
                    case None:
                        mBinding.txtResult.setText(String.format("None: %s", msg));
                        break;

                }

            }
        };

        mBinding.btnClr.setOnClickListener(view -> {
            core.clearProxyConfig();

        });

        mBinding.btnCnt.setOnClickListener(view -> {
            cfgAccount();
        });

        setTestAccount();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        core.addListener(mCoreListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        core.removeListener(mCoreListener);
    }

    private void setTestAccount() {
        mBinding.username.setText("1016");
        mBinding.password.setText("Sscctv1016");
        mBinding.display.setText("Jinseop");
        mBinding.domain.setText("175.195.153.235");
    }

    private void cfgAccount() {
        Log.d(TAG, "CftAccount");
        
        mAccountCreator.setUsername(mBinding.username.getText().toString());
        mAccountCreator.setPassword(mBinding.password.getText().toString());
        mAccountCreator.setDomain(mBinding.domain.getText().toString());
        mAccountCreator.setDisplayName(mBinding.display.getText().toString());


        switch (mBinding.assistantTransports.getCheckedRadioButtonId()) {
            case R.id.transport_tcp:
                mAccountCreator.setTransport(TransportType.Tcp);
                break;
            case R.id.transport_udp:
                mAccountCreator.setTransport(TransportType.Udp);
                break;
            case R.id.transport_tls:
                mAccountCreator.setTransport(TransportType.Tls);
                break;
        }
        core.clearProxyConfig();
        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();
        core.setDefaultProxyConfig(proxyConfig);
    }
}
