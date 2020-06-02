package com.sscctv.nursecallapp.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.adapter.HistoryLogAdapter;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class DialogCallListDetail extends Dialog {

    private Context mContext;
    private String mUserName, mSip;
    private Address mAddress;
    private TextView txtUserName, txtSip;
    private ListView mLogsList;
    private Core core;
    private List<CallLog> mLogs;
    public DialogCallListDetail(Context context) {
        super(context);
        this.mContext = context;
    }

    private static final String TAG = "DialogCallListDetail";
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.dialog_list_detail);
        txtUserName = findViewById(R.id.dialog_display_txt);
        txtSip = findViewById(R.id.dialog_sip_txt);
        mLogsList = findViewById(R.id.logs_list);
        btnClose = findViewById(R.id.dialog_detail_close);
        btnClose.setOnClickListener(view -> DialogCallListDetail.this.dismiss());

        core = MainCallService.getCore();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        setInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    public void init(Address address) {
        mAddress = address;
        mUserName = address.getDisplayName();
        if (mUserName == null) {
            mUserName = address.getUsername();
        }
        mSip = address.asStringUriOnly();
        Log.d(TAG, "Init: " + address.getUsername());
    }

    private void setInfo() {

        txtUserName.setText(mUserName);
        txtSip.setText(mSip);

        Address address = Factory.instance().createAddress(mSip);
        Core core = MainCallService.getCore();

        if (address != null && core != null) {
            address.clean();
            ProxyConfig proxyConfig = core.getDefaultProxyConfig();
            Log.d(TAG, "address: " + address.getUsername() + " proxyConfig: " + proxyConfig.getIdentityAddress().asStringUriOnly());

//            CallLog[] logs;
//            logs = core.getCallHistory(address, proxyConfig.getIdentityAddress());
//            List<CallLog> logsList = Arrays.asList(logs);
//            mLogs = Arrays.asList(core.getCallLogs());
//            logs = core.getCallLogs();
//            logs = core.getCallHistoryForAddress(address);
//            Log.d(TAG, "History: " + logsList + " , " + core.getCallLogsDatabasePath());

//            List<CallLog> logsList = Arrays.asList(mLogs);
//            CallLog log = mLogs.get(0);
//            long timestamp = log.getStartDate() * 1000;
//            Calendar logTime = Calendar.getInstance();
//            logTime.setTimeInMillis(timestamp);
//            String callTime = secondsToDisplayableString(log.getDuration());

//            Log.d(TAG, "CallTIme: " + callTime);
//
//            mLogsList.setAdapter(
//                    new HistoryLogAdapter(
//                            getContext(), R.layout.history_detail_cell, logsList));

        }
    }


    private void fileCopy(String str) {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("settings", 0);

        File copyFile = new File(sharedPreferences.getString("path", "/storage/emulated/0") + File.separator + "Packet");

        if (!copyFile.mkdir()) {
            Log.e(TAG, "Copy directory not created");
        } else {
            Log.i(TAG, "Success copy directory");
        }

        try {
            File file1 = new File("data/data/com.sscctv.packetAnalyzer/files/Packet.pcap");

            FileChannel inChannel = new FileInputStream(file1).getChannel();
            FileChannel outChannel = new FileOutputStream(copyFile + File.separator + str + ".pcap").getChannel();
//
            int maxCount = (64 * 1024 * 1024) - (32 * 1024);
            long size = inChannel.size();
            long position = 0;
            while (position < size) {
                position += inChannel.transferTo(position, maxCount, outChannel);
            }

            inChannel.close();
            outChannel.close();
//            finishProcess();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    private String dateName(long dateTaken) {
        Date date = new Date(dateTaken);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        return dateFormat.format(date);
    }
//
//    @SuppressLint("HandlerLeak")
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            if (msg.what == 100) {
//                finishProcess();
//                avi.smoothToHide();
//
//                textTitle.setVisibility(View.INVISIBLE);
//                textCom.setVisibility(View.VISIBLE);
//
//                txtName.setVisibility(View.VISIBLE);
//                txtPath.setVisibility(View.VISIBLE);
//                txtMsg.setVisibility(View.VISIBLE);
//
////                btnStop.setVisibility(View.INVISIBLE);
//                btnOpen.setVisibility(View.VISIBLE);
//                btnClose.setVisibility(View.VISIBLE);
//
//
//            }
////            else {
////                textValue.setText(String.valueOf(msg.what));
////            }
//        }
//    };
//
//    private void finishProcess() {
//        ConstraintLayout layout = findViewById(R.id.dialog_live_save_layout);
//        ViewGroup.LayoutParams params = layout.getLayoutParams();
//        Log.d(TAG, "" + params.height);
////        ConstraintLayout outView = findViewById(R.id.iperf_result);
//        Animation animation = new AlphaAnimation(0, 1);
//        animation.setDuration(500);
////        params.height = 530;
////        layout.setLayoutParams(params);
//        layout.setAnimation(animation);
//
//
////        if (!mVisible) {
////            mVisible = true;
//
//
//        TextView title = findViewById(R.id.dialog_live_title);
//        title.setText(mContext.getResources().getString(R.string.pcap_file_save_complete));
//
//        EditText edit = findViewById(R.id.dialog_live_capture_edit);
//        edit.setVisibility(View.GONE);
//
//        TextView file = findViewById(R.id.dialog_live_fileName);
//        file.setText(mContext.getResources().getString(R.string.file_name) + fileName);
//
//        TextView txt0 = findViewById(R.id.dialog_live_save_msg0);
//        txt0.setText(mContext.getResources().getString(R.string.open_file));
//
//        btnSave.setVisibility(View.INVISIBLE);
//        btnOpen.setVisibility(View.VISIBLE);
//
////        Log.d(TAG, "getFocus: " + getCurrentFocus());
//        DialogLiveSave.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//
//
////            params.height = 450;
////            chartView.setLayoutParams(params);
////
////            outView.setVisibility(View.VISIBLE);
////            outView.setAnimation(animation);
//    }
////    }


}
