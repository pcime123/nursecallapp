package com.sscctv.nursecallapp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.CallLogItem;
import com.sscctv.nursecallapp.databinding.FragCallListBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.CallListTitleAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class CallListFragment extends Fragment {
    private static final String TAG = "CallListFragment";
    private MainActivity activity;
    private TinyDB tinyDB;
    private CallListTitleAdapter callListTitleAdapter;
    private Core core;
    private FragCallListBinding mBinding;
    private ArrayList<CallLogItem> callLogItems = new ArrayList<>();
    private int listSize;
    private List<Fragment> listFragments;

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
//        Log.v(TAG, "onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_call_list, container, false);

        tinyDB = new TinyDB(getContext());

        mBinding.callLayout.addTab((mBinding.callLayout.newTab().setText("일반호출 목록")));
        mBinding.callLayout.addTab((mBinding.callLayout.newTab().setText("긴급호출 목록")));
        mBinding.callLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        assert getFragmentManager() != null;

        listFragments = new ArrayList<>();
        listFragments.add(new CallListNormal());
        listFragments.add(new CallListEmergency());

        callListTitleAdapter = new CallListTitleAdapter(getChildFragmentManager(), listFragments);
        mBinding.viewPager.setAdapter(callListTitleAdapter);
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.callLayout));
        mBinding.callLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mBinding.btnListSetup.setOnClickListener(view -> {
            goListSetup();
        });
        return mBinding.getRoot();
    }

    private void goListSetup() {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_list_setup);
        final EditText max = dialog.findViewById(R.id.dg_list_max);
        final Button btnNormalDel = dialog.findViewById(R.id.dg_list_normal_del);
        final Button btnEmDel = dialog.findViewById(R.id.dg_list_em_del);
        final Button btnClose = dialog.findViewById(R.id.dg_list_close);

        max.setText(String.valueOf(tinyDB.getInt(KeyList.CALL_LOG_MAX)));

        btnClose.setOnClickListener(view -> {
            tinyDB.putInt(KeyList.CALL_LOG_MAX, Integer.parseInt(max.getText().toString()));

            dialog.dismiss();
        });

        btnNormalDel.setOnClickListener(view -> {
            core.clearCallLogs();
            tinyDB.remove(KeyList.CALL_LOG_NORMAL);
            dialog.dismiss();

            callListTitleAdapter = new CallListTitleAdapter(getChildFragmentManager(), listFragments);
            mBinding.viewPager.setAdapter(callListTitleAdapter);
        });


        btnEmDel.setOnClickListener(view -> {
            tinyDB.remove(KeyList.CALL_LOG_EMERGENCY);
            dialog.dismiss();

            callListTitleAdapter = new CallListTitleAdapter(getChildFragmentManager(), listFragments);
            mBinding.viewPager.setAdapter(callListTitleAdapter);
            Log.d(TAG, "getSize: " + core.getLogCollectionMaxFileSize());
        });

        dialog.show();

    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }


}
