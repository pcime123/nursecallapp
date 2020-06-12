package com.sscctv.nursecallapp.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSetAccountBinding;
import com.sscctv.nursecallapp.databinding.FragSetInfoBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.security.Key;


public class SetInfoFragment extends Fragment {

    private static final String TAG = SetInfoFragment.class.getSimpleName();
    private Core core;
    private TinyDB tinyDB;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    private EditText id, pw, domain;

    static SetInfoFragment newInstance() {
        return new SetInfoFragment();
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
        FragSetInfoBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_set_info, container, false);

        tinyDB = new TinyDB(getContext());

        core = MainCallService.getCore();

        layout.setupLocation.setText(tinyDB.getString(KeyList.DEVICE_WARD));

        layout.btnSetup.setOnClickListener(view -> {
            tinyDB.putString(KeyList.DEVICE_WARD, layout.setupLocation.getText().toString());
        });

        layout.btnClear.setOnClickListener(view -> {
        });

        layout.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return layout.getRoot();
    }
//
//    private void getSelectMode() {
//        if(mBinding.btnNurse.isChecked()){
//            mode = 0;
//        } else if (mBinding.btnSecurity.isChecked()) {
//            mode = 1;
//        } else if (mBinding.btnPath.isChecked()) {
//            mode = 2;
//        } else if (mBinding.btnEtc.isChecked()) {
//            mode = 3;
//        }
//
//        tinyDB.putInt(KeyList.DEVICE_TYPE, mode);
//        tinyDB.putString(KeyList.DEVICE_WARD,mBinding.inputUseWard.getText().toString());
//    }

}
