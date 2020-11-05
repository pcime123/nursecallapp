package com.sscctv.nursecallapp.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSettingsBinding;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import java.util.Objects;

public class SetMainFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = SetMainFragment.class.getSimpleName();

    public static SetMainFragment newInstance() {
        SetMainFragment fragment = new SetMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragSettingsBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_settings, container, false);
        EncryptionUtil.getInstance();

        layout.btnEthernet.setOnClickListener(this);
        layout.btnAccount.setOnClickListener(this);
        layout.btnAudio.setOnClickListener(this);
        layout.btnDisplay.setOnClickListener(this);
        layout.btnDatetime.setOnClickListener(this);
        layout.btnAdminAccount.setOnClickListener(this);
        layout.btnAdminSetup.setOnClickListener(this);
        layout.btnInfo.setOnClickListener(this);

        String str;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            str = bundle.getString("str");
            Log.d(TAG, "String: " + str);

            if(Objects.requireNonNull(str).equals("sip")) {
                setChildFragment(SetAccountFragment.newInstance());
            } else if(Objects.requireNonNull(str).equals("ward")) {
                setChildFragment(SetInfoFragment.newInstance());
            }
        }
        return layout.getRoot();
    }

    @Override
    public void onClick(View view) {
        Fragment fragment;
        switch (view.getId()) {
            case R.id.btn_ethernet:
                fragment = SetEthernetFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_account:
                fragment = SetAccountFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_audio:
                fragment = SetAudioFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_display:
                fragment = SetDisplayFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_datetime:
                fragment = SetDatetimeFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_admin_account:
                fragment = SetAdminFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_info:
                fragment = SetInfoFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_admin_setup:
//                NurseCallUtils.startIntent(getContext(), SettingsAdminSetup.class);
                fragment = SetDpBoardFragment.newInstance();
                setChildFragment(fragment);
                break;

        }
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.child_settings_fragment, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(fragment);
        fragmentTransaction.attach(fragment);

        fragmentTransaction.replace(R.id.child_settings_fragment, fragment).commit();

    }
}
