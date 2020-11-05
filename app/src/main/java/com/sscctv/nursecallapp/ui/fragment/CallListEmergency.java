package com.sscctv.nursecallapp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.data.EmCallLogItem;
import com.sscctv.nursecallapp.data.EmListItem;
import com.sscctv.nursecallapp.databinding.TabListEmergencyBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.fragment.adapter.CallListAdapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.EmCallListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.CallLog;
import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CallListEmergency extends Fragment {

    private static final String TAG = CallListEmergency.class.getSimpleName();
    private TinyDB tinyDB;
    private RecyclerView allList;
    private List<CallLog> mLogs;
    private ArrayList<EmCallLogItem> emLogItems;
    private Core core;
    private EmCallListAdapter mCallListAdapter;
    private ArrayList<AllExtItem> getArrayList;
    private TabListEmergencyBinding mBinding;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        emLogItems = NurseCallUtils.getEmLog(tinyDB, KeyList.CALL_LOG_EMERGENCY);
        TaskGetList(new getListTask());

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tab_list_emergency, container, false);
        core = MainCallService.getCore();
        tinyDB = new TinyDB(getContext());

        mBinding.tabListAll.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mBinding.tabListAll.setLayoutManager(manager);

        return mBinding.getRoot();
    }
    private void TaskGetList(AsyncTask<Void, Void, Void> asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    private class getListTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        Calendar logTime;
        SimpleDateFormat dateFormat;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "getListTask onPreExecute");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
            logTime = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.getDefault());
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int delSize;

            if (emLogItems.size() != 0) {
                Collections.sort(emLogItems, (t1, t2) -> {
                    String in1 = String.valueOf(t1.getCallDate());
                    String in2 = String.valueOf(t2.getCallDate());
                    return in2.compareTo(in1);
                });

                if (emLogItems.size() > tinyDB.getInt(KeyList.CALL_LOG_MAX)) {
                    delSize = emLogItems.size() - tinyDB.getInt(KeyList.CALL_LOG_MAX);

                    Log.d(TAG, "Item Size: " + emLogItems.size() + " Del Size: " + delSize);
                    for (int i = 0; i < delSize; i++) {
                        emLogItems.remove((emLogItems.size()-1) - i);
                        delSize--;
                        i--;
                    }
                }

                NurseCallUtils.putEmLog(tinyDB, KeyList.CALL_LOG_EMERGENCY, emLogItems);
                mCallListAdapter = new EmCallListAdapter(getContext(), emLogItems);
                mBinding.tabListAll.setAdapter(mCallListAdapter);
            }

            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }


}
