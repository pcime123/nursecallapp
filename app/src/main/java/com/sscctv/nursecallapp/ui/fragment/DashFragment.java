package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragCallListBinding;
import com.sscctv.nursecallapp.databinding.FragDashBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.fragment.adapter.ListCalldapter;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.List;

public class DashFragment extends Fragment implements IOnBackPressed {
    private static final String TAG = "CallListFragment";
    private MainActivity activity;
    private TinyDB tinyDB;
    private ListCalldapter listCalldapter;
    private Core core;
    private Dialog dialog;
    private ArrayList<AllExtItem> allExtItems;
    private ViewPager pager;
    private ToneGenerator toneGenerator;
    private AudioManager mAudioManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Log.v(TAG, "onAttach()");
        activity = (MainActivity) getActivity();
        core = MainCallService.getCore();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragDashBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_dash, container, false);

        tinyDB = new TinyDB(getContext());


        return layout.getRoot();
    }

    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }

}
