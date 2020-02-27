package com.sscctv.nursecallapp.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.MainActivity;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

public class AccountFragment extends Fragment implements IOnBackPressed {

    private static final String TAG = "AccountFragment";
    private MainActivity activity;
    private Core core;
    private AccountCreator mAccountCreator;
    private EditText mUsername, mPassword, mDomain, mDisplay;
    private RadioGroup mTransport;
    private CoreListenerStub mCoreListener;
    private MainCallService callService;
    private TextView mTextView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
        activity = (MainActivity) getActivity();
        core = MainCallService.getCore();
        mAccountCreator = core.createAccountCreator(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach()");
        activity = null;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        mUsername = view.findViewById(R.id.username);
        mPassword = view.findViewById(R.id.password);
        mDomain = view.findViewById(R.id.domain);
        mDisplay = view.findViewById(R.id.display);
        mTransport = view.findViewById(R.id.assistant_transports);
        mTextView = view.findViewById(R.id.txt_result);
        Button mConnect = view.findViewById(R.id.btnCnt);
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfgAccount();
            }
        });
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String msg) {
                super.onRegistrationStateChanged(core, cfg, state, msg);
                switch (state) {
                    case Ok:
                        mTextView.setText(String.format("OK: %s", msg));
//                        activity.setTxtResult("OK", msg);
                        break;
                    case Failed:
                        mTextView.setText(String.format("Failed: %s", msg));
//                        activity.setTxtResult("Failed", msg);
                        break;
                    case Progress:
                        mTextView.setText(String.format("Progress: %s", msg));
//                        activity.setTxtResult("Progress", msg);
                        break;
                    case Cleared:
                        mTextView.setText(String.format("Cleared: %s", msg));
//                        activity.setTxtResult("Cleared", msg);
                        break;
                    case None:
                        mTextView.setText(String.format("None: %s", msg));
//                        activity.setTxtResult("None", msg);
                        break;

                }

            }
        };
        Button mClear = view.findViewById(R.id.btnClr);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "getConfig: " + core.getDefaultProxyConfig());
                core.clearProxyConfig();
                Log.d(TAG, "getConfig1: " + core.getDefaultProxyConfig());
//                ProxyConfig proxyConfig = MainCallService.getCore().getDefaultProxyConfig();
//                updateStatus(proxyConfig.getState());
            }
        });


        setTestAccount();
        return view;
    }

    private void cfgAccount() {
        mAccountCreator.setUsername(mUsername.getText().toString());
        mAccountCreator.setPassword(mPassword.getText().toString());
        mAccountCreator.setDomain(mDomain.getText().toString());
        mAccountCreator.setDisplayName(mDisplay.getText().toString());
        ;

        switch (mTransport.getCheckedRadioButtonId()) {
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

        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();
        core.setDefaultProxyConfig(proxyConfig);
    }

    private void setTestAccount() {
        mUsername.setText("2002");
        mPassword.setText("2002");
        mDisplay.setText("Jinseop");
    }


    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }
}
