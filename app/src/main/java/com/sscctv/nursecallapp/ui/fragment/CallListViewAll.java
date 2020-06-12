package com.sscctv.nursecallapp.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.TabListAllBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.adapter.CallListAdapter1;
import com.sscctv.nursecallapp.ui.adapter.CallLogItem;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.fragment.adapter.CallListAdapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.TabListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.CallLog;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CallListViewAll extends Fragment implements OnSelectCall {

    private static final String TAG = CallListViewAll.class.getSimpleName();
    private TinyDB tinyDB;
    private RecyclerView allList;
    private List<CallLog> mLogs;
    private ArrayList<CallLogItem> callLogItems;
    private Core core;
    private CallListAdapter mCallListAdapter;
    private ArrayList<AllExtItem> getArrayList;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        callLogItems = new ArrayList<>();
        getList();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TabListAllBinding layout = DataBindingUtil.inflate(inflater, R.layout.tab_list_all, container, false);
        core = MainCallService.getCore();

        tinyDB = new TinyDB(getContext());
        allList = layout.tabListAll;
        layout.tabListAll.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        layout.tabListAll.setLayoutManager(manager);


        return layout.getRoot();
    }

    private void getList() {
        callLogItems = NurseCallUtils.getCallLog(tinyDB, KeyList.CALL_LOG);

//        mCallListAdapter = new CallListAdapter(getContext(), callLogItems, this);
//
//        for(int i = 0; i < callLogItems.size(); i++) {
//            Log.d(TAG, "Log: " + callLogItems.get(i).getCallLocation());
//        }

        callLogItems.add(new CallLogItem("간호사 스테이션", "호출 수신", "2층 10병동 간호사 스테이션", "00:42","2020-04-07 오후 05:42",false));
        callLogItems.add(new CallLogItem("간호사 호출기", "호출 송신", "2층 10병동 103호 1번 병상", "00:33","2020-04-08 오후 01:42",false));
        callLogItems.add(new CallLogItem("간호사 호출기", "호출 부재", "2층 10병동 103호 3번 병상", "00:00","2020-04-08 오후 01:47",false));
        mCallListAdapter = new CallListAdapter(getContext(), callLogItems, this);

        allList.setAdapter(mCallListAdapter);
    }

//    private void getList() {
//        getArrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);
//        for (int i = 0; i < getArrayList.size(); i++) {
////            Log.d(TAG, "" + getArrayList.get(i));
//        }
//        adapter = new TabListAdapter(getContext(), getArrayList, true, this);
//        allList.setAdapter(adapter);
//    }



    @Override
    public void roomSelect() {

    }

    @Override
    public void roomAllClear() {

    }

    public void refresh() {
        Log.d(TAG, "Refresh");
        onResume();
    }

    @Override
    public void starSelect(int position, boolean chk) {
    }
}
