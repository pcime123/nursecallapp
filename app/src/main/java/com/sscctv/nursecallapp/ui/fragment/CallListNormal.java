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

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.data.CallLogItem;
import com.sscctv.nursecallapp.databinding.TabListNormalBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.fragment.adapter.CallListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class CallListNormal extends Fragment {

    private static final String TAG = CallListNormal.class.getSimpleName();
    private TinyDB tinyDB;
    private Core core;
    private CallListAdapter mCallListAdapter;
    private TabListNormalBinding mBinding;
    private ArrayList<CallLogItem> callLogItems;
    private int listSize;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        callLogItems = NurseCallUtils.getCallLog(tinyDB, KeyList.CALL_LOG_NORMAL);
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tab_list_normal, container, false);
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

            for (int i = 0; i < core.getCallLogs().length; i++) {
//                Log.d(TAG, "Dir: " + core.getCallLogs()[i].getDir() + " Status: " + core.getCallLogs()[i].getStatus() + " Start Date: " + core.getCallLogs()[i].getStartDate() +
//                        " Duration: " + core.getCallLogs()[i].getDuration() + " FromAddress: " + core.getCallLogs()[i].getFromAddress().getUsername() + " LocalAddress:" + core.getCallLogs()[i].getLocalAddress().getUsername() +
//                        " RemoteAddress: " + core.getCallLogs()[i].getRemoteAddress().getUsername() + " GetToAddress: " + core.getCallLogs()[i].getToAddress().getUsername());
                long timestamp = core.getCallLogs()[i].getStartDate() * 1000;
                logTime.setTimeInMillis(timestamp);
//                if (callLogItems.size() != 0) {
//                    Log.d(TAG, "prev: " + callLogItems.get(0).getCallTime());
//                }
                callLogItems.add(0, new CallLogItem(core.getCallLogs()[i].getFromAddress().getUsername(), getNameExtension(core.getCallLogs()[i].getRemoteAddress().getUsername()), core.getCallLogs()[i].getDir().toString(),
                        core.getCallLogs()[i].getStatus().toString(), dateFormat.format(logTime.getTime()), secondsToDisplayableString(core.getCallLogs()[i].getDuration()), false));
//                Log.d(TAG, "after: " + callLogItems.get(0).getCallTime());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int delSize;

            if (callLogItems.size() != 0) {
                Collections.sort(callLogItems, (t1, t2) -> {
                    String in1 = t1.getCallTime();
                    String in2 = t2.getCallTime();
                    return in2.compareTo(in1);
                });

                if (callLogItems.size() > tinyDB.getInt(KeyList.CALL_LOG_MAX)) {
                    delSize = callLogItems.size() - tinyDB.getInt(KeyList.CALL_LOG_MAX);

                    Log.d(TAG, "Del Size: " + delSize);
                    for (int i = 0; i < delSize; i++) {
                        callLogItems.remove((callLogItems.size()-1) - i);
                        delSize--;
                        i--;
                    }
                }

                core.clearCallLogs();
                NurseCallUtils.putCallLog(tinyDB, KeyList.CALL_LOG_NORMAL, callLogItems);
                mCallListAdapter = new CallListAdapter(getContext(), callLogItems);
                mBinding.tabListAll.setAdapter(mCallListAdapter);
            }





            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

    private String getNameExtension(String address) {
        ArrayList<AllExtItem> arrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);

        for (int i = 0; i < arrayList.size(); i++) {
            AllExtItem item = arrayList.get(i);
            if (item.getNum().equals(address)) {
                return item.getName();
            }
        }

        return address;
    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    class Descending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

    }

}
