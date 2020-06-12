package com.sscctv.nursecallapp.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSetAccountBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;


public class SetAccountFragment extends Fragment {

    private static final String TAG = SetAccountFragment.class.getSimpleName();
    private Core core;
    private TinyDB tinyDB;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    private EditText id, pw, domain;

    static SetAccountFragment newInstance() {
        return new SetAccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragSetAccountBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_set_account, container, false);

        tinyDB = new TinyDB(getContext());

        core = MainCallService.getCore();
        mAccountCreator = core.createAccountCreator(null);

        id = layout.editSipId; pw = layout.editSipPw; domain = layout.editSipDomain;

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String msg) {
                super.onRegistrationStateChanged(core, cfg, state, msg);
                if(state == RegistrationState.Ok) {
                    tinyDB.putString(KeyList.SIP_ID, layout.editSipId.getText().toString());
                    tinyDB.putString(KeyList.SIP_PW, layout.editSipPw.getText().toString());
                    tinyDB.putString(KeyList.SIP_DOMAIN, layout.editSipDomain.getText().toString());
                } else {
                    tinyDB.putString(KeyList.SIP_ID, "");
                    tinyDB.putString(KeyList.SIP_PW, "");
                    tinyDB.putString(KeyList.SIP_DOMAIN, "");
                }
//                Log.d(TAG, "State: " + state);

                switch (state) {
                    case Ok:

                        layout.txtResult.setText(String.format("OK: %s", msg));
                        break;
                    case Failed:
                        layout.txtResult.setText(String.format("Failed: %s", msg));
                        break;
                    case Progress:
                        layout.txtResult.setText(String.format("Progress: %s", msg));
                        break;
                    case Cleared:
                        layout.txtResult.setText(String.format("Cleared: %s", msg));
                        break;
                    case None:
                        layout.txtResult.setText(String.format("None: %s", msg));
                        break;

                }

            }
        };

        layout.btnSetup.setOnClickListener(view -> {
            cfgAccount();
        });

        layout.btnClear.setOnClickListener(view -> {
            id.setText("");
            pw.setText("");
            domain.setText("");
        });


        return layout.getRoot();
    }

    private void cfgAccount() {
        mAccountCreator.setUsername(id.getText().toString());
        mAccountCreator.setPassword(pw.getText().toString());
        mAccountCreator.setDomain(domain.getText().toString());
        mAccountCreator.setDisplayName(id.getText().toString());
        mAccountCreator.setTransport(TransportType.Udp);

        core.clearProxyConfig();
        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();
        core.setDefaultProxyConfig(proxyConfig);
    }

    private void setTestAccount() {
        id.setText("2000");
        pw.setText("Sscctv2000");
        domain.setText("175.195.153.235");
    }
}
