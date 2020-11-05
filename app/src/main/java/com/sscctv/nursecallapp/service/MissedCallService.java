package com.sscctv.nursecallapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class MissedCallService extends Service {

    private static final String TAG = MissedCallService.class.getSimpleName();
    private View mView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private String message;
    private TinyDB tinyDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tinyDB = new TinyDB(this);
        mView = mInflater.inflate(R.layout.dialog_missed_call, null);

        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        message = intent.getStringExtra("msg");

        if (Objects.equals(message, NurseCallUtils.CALL_INCOMING)) {
            if (mView.isShown()) {
                mWindowManager.removeViewImmediate(mView);
                mWindowManager = null;
            }

        } else {
            if (tinyDB.getBoolean(KeyList.MISSED_CALL)) {
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                TextView num = mView.findViewById(R.id.miss_num);
                num.setText(message);

                TextView location = mView.findViewById(R.id.miss_location);
                if (message.equals(NurseCallUtils.putDeviceName(getNameExtension(message)))) {
                    location.setText("등록 안됨");
                } else {
                    location.setText(putDeviceName(getNameExtension(message)));
                }
                Objects.requireNonNull(mWindowManager).addView(mView, mParams);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void sendMessage() {
        Intent intent = new Intent("missed_call");
        intent.putExtra("msg", "change");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onMove(View view) {
        mWindowManager.removeViewImmediate(mView);
        mWindowManager = null;
        sendMessage();
        tinyDB.putBoolean(KeyList.MISSED_CALL, false);

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

    public static String putDeviceName(String device) {

        if (!device.contains("-")) {
            return device;
        }

        String model = null;
        String index1 = null;
        String index2 = null;
        String index3 = null;

        String[] callerId = device.split("-");
        if (callerId.length == 4) {
            model = callerId[0];
            index1 = callerId[1];
            index2 = callerId[2];
            index3 = callerId[3];

            if (device.contains(KeyList.MODEL_TELEPHONE_MASTER)) {
                // 기기모델명 + 병동 + 0 + 일련번호 (2자리)
                return index1 + "병동 간호사 스테이션 - " + index3;
            } else if (device.contains(KeyList.MODEL_TELEPHONE_SECURITY)) {
                return index1 + "병동 보안 스테이션 - " + index3;
            } else if (device.contains(KeyList.MODEL_TELEPHONE_PUBLIC)) {
                return index1 + "병동 병리실 - " + index3;
            } else if (device.contains(KeyList.MODEL_PAGER_BASIC) || device.contains(KeyList.MODEL_PAGER_EXTENTION)
                    || device.contains(KeyList.MODEL_PAGER_BASIC_WALL) || device.contains(KeyList.MODEL_PAGER_BASIC_STAND)) {
                // 기기모델명 + 병동 + 병실 + 병상
                return index1 + "병동 " + index2.replaceAll("M", "") + "호 " + index3 + "번 간호사 호출기";
            } else if (device.contains(KeyList.MODEL_PAGER_PUBLIC_STAND) || device.contains(KeyList.MODEL_PAGER_PUBLIC_WALL)) {
                return index1 + "병동 공용통화자기 - " + index3;

            } else if (device.contains(KeyList.MODEL_PAGER_OPERATING_01) || device.contains(KeyList.MODEL_PAGER_OPERATING_02) ||
                    device.contains(KeyList.MODEL_PAGER_OPERATING_03)) {
                return index2 + " 수술실 호출기 - " + index3;

            }
        } else {
            return device;
        }
        return "등록 안됨: " + device;
    }
}
