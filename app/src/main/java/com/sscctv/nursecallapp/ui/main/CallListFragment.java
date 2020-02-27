package com.sscctv.nursecallapp.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.MainActivity;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.CallListAdapter;
import com.sscctv.nursecallapp.ui.utils.CallListViewHolder;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.SelectableHelper;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.Core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CallListFragment extends Fragment implements SelectableHelper.DeleteListener, CallListViewHolder.ClickListener, IOnBackPressed {

    private static final String TAG = "CallListFragment";
    private MainActivity activity;
    private Core core;
    private RecyclerView mHistoryList;
    private CallListAdapter mCallListAdapter;
    private List<CallLog> mLogs;
    private SelectableHelper mSelectionHelper;
    private LinearLayout mTopBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
        activity = (MainActivity) getActivity();
        core = MainCallService.getCore();

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
        reloadData();
        Log.v(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_calllist, container, false);
        Log.w(TAG, "mContext: " + view);
        mSelectionHelper = new SelectableHelper(view, this);
        mTopBar = view.findViewById(R.id.top_bar);
        Log.w(TAG, "mTopbar: " + mTopBar.getVisibility());
        mHistoryList = view.findViewById(R.id.history_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mHistoryList.setLayoutManager(layoutManager);
        mHistoryList.setAdapter(mCallListAdapter);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(
                        mHistoryList.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        mHistoryList.addItemDecoration(dividerItemDecoration);

        mLogs = Arrays.asList(core.getCallLogs());

        mCallListAdapter =
                new CallListAdapter(getContext(), mLogs, this, mSelectionHelper);
        mHistoryList.setAdapter(mCallListAdapter);
        mSelectionHelper.setAdapter(mCallListAdapter);
        mSelectionHelper.setDialogMessage(R.string.app_name);

        return view;
    }


    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        int size = mCallListAdapter.getSelectedItemCount();
        for (int i = 0; i < size; i++) {
            CallLog log = (CallLog) objectsToDelete[i];
            core.removeCallLog(log);
            onResume();
        }
    }

//    @Override
//    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
//        if (mCallListAdapter.isEditionEnabled()) {
//            CallLog log = mLogs.get(position);
//            core.removeCallLog(log);
//            mLogs = Arrays.asList(core.getCallLogs());
//        }
//    }


    @Override
    public void onItemClicked(int position) {
        if (mCallListAdapter.isEditionEnabled()) {
            mCallListAdapter.toggleSelection(position);
        } else {
            if (position >= 0 && position < mLogs.size()) {
                CallLog log = mLogs.get(position);
                Address address;
                if (log.getDir() == Call.Dir.Incoming) {
                    address = log.getFromAddress();
                } else {
                    address = log.getToAddress();
                }
                if (address != null) {
                    Log.d(TAG, "Click!!!!: " + address.asStringUriOnly());
                    ((MainActivity) Objects.requireNonNull(getActivity())).newOutgoingCall(address.asStringUriOnly());
                }
            }
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!mCallListAdapter.isEditionEnabled()) {
            mSelectionHelper.enterEditionMode();
        }
        mCallListAdapter.toggleSelection(position);
        return true;
    }

    private void reloadData() {
        mLogs = Arrays.asList(core.getCallLogs());
        mCallListAdapter =
                new CallListAdapter(getContext(), mLogs, this, mSelectionHelper);
        mHistoryList.setAdapter(mCallListAdapter);
        mSelectionHelper.setAdapter(mCallListAdapter);
        mSelectionHelper.setDialogMessage(R.string.app_name);
    }

    public void showHistoryDetails(Address address) {

    }


    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }
}
