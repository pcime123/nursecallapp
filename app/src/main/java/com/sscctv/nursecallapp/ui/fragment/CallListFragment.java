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
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.fragment.adapter.ListCalldapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.NormalCallAdapter;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.List;

public class CallListFragment extends Fragment implements IOnBackPressed {
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
//        Log.v(TAG, "onDetach()");
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        listCalldapter.notifyDataSetChanged();

//        Log.v(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragCallListBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_call_list, container, false);

        tinyDB = new TinyDB(getContext());

        layout.callLayout.addTab((layout.callLayout.newTab().setText("전체목록")));
        layout.callLayout.addTab((layout.callLayout.newTab().setText("병상호출 목록")));
        layout.callLayout.addTab((layout.callLayout.newTab().setText("일반호출 목록")));
        layout.callLayout.addTab((layout.callLayout.newTab().setText("긴급호출 목록")));
        layout.callLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        assert getFragmentManager() != null;

        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(new CallListViewAll());
        listFragments.add(new NormalViewBasic());
        listFragments.add(new NormalViewSecurity());
        listFragments.add(new NormalViewPathology());

        listCalldapter = new ListCalldapter(getChildFragmentManager(), listFragments);
        pager = layout.viewPager;
        layout.viewPager.setAdapter(listCalldapter);
        layout.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(layout.callLayout));
        layout.callLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                Log.e(TAG, "Select: " + tab.getPosition());

                layout.viewPager.setCurrentItem(tab.getPosition());
//                normalCallAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });




        return layout.getRoot();
    }


    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(getContext(), "Address null");
        }
    }

    public void newOutgoingCall(String to) {
        if (to == null) return;
        Address address = core.interpretUrl(to);
        inviteAddress(address);
    }

    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }

}
