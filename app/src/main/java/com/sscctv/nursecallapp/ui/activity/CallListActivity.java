package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityCallListBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.adapter.CallListAdapter1;
import com.sscctv.nursecallapp.ui.adapter.CallListViewHolder;
import com.sscctv.nursecallapp.ui.adapter.SelectableHelper;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.Core;

import java.util.Arrays;
import java.util.List;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.newOutgoingCall;

public class CallListActivity extends AppCompatActivity implements SelectableHelper.DeleteListener, CallListViewHolder.ClickListener {

    private static final String TAG = "MainActivity";
    private Core core;
    private CallListAdapter1 mCallListAdapter;
    private List<CallLog> mLogs;
    private SelectableHelper mSelectionHelper;
    private ActivityCallListBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_call_list);
        core = MainCallService.getCore();

//        mSelectionHelper = new SelectableHelper(mBinding.topEdit, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mBinding.historyList.setLayoutManager(layoutManager);
        mBinding.historyList.setAdapter(mCallListAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mBinding.historyList.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        mBinding.historyList.addItemDecoration(dividerItemDecoration);

        mBinding.editHome.setOnClickListener(view -> {
//            mBinding.topBar.setVisibility(View.GONE);
//            mBinding.topEdit.setVisibility(View.VISIBLE);
//
        });


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
                    newOutgoingCall(this, address.asStringUriOnly());
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
                new CallListAdapter1(getApplicationContext(), mLogs, this, mSelectionHelper);
        mBinding.historyList.setAdapter(mCallListAdapter);
//        mSelectionHelper.setAdapter(mCallListAdapter);
//        mSelectionHelper.setDialogMessage(R.string.app_name);
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
