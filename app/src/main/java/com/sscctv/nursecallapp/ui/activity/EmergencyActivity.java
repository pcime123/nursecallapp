package com.sscctv.nursecallapp.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityEmergencyBinding;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class EmergencyActivity extends AppCompatActivity {

    private static final String TAG = EmergencyActivity.class.getSimpleName();
    private ActivityEmergencyBinding mBinding;

    private Handler handler;

    private Socket socket;
    private BufferedReader networkReader;
    private BufferedWriter networkWriter;
    private String html = "";
    private String ip;
//    private int port = 59009;
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        try {
//            socket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_emergency);
        Log.d(TAG, "Go ");

        Intent intent = getIntent();
        String call = intent.getStringExtra("call");
        String type = intent.getStringExtra("type");

        if(type != null && call != null) {
            if(type.equals("01") && call.equals("01")) {
                mBinding.btn.setVisibility(View.INVISIBLE);
                mBinding.title.setText("위급 상황 발생");
                mBinding.content.setText("10병동 103호실 화장실");
                mBinding.content1.setText("확인 후 재중스위치를 눌러주세요.");
            } else if(type.equals("01") && call.equals("00")) {
                mBinding.btn.setVisibility(View.VISIBLE);
                mBinding.title.setText("위급 상황 발생 취소");
                mBinding.content.setText("10병동 103호실 화장실");
                mBinding.content1.setText("위급 상황 취소가 발생했습니다. 확인 후 닫기를 눌러주세요.");
            }else if(type.equals("02") && call.equals("01")) {
                mBinding.btn.setVisibility(View.VISIBLE);
                mBinding.background.setBackgroundColor(getResources().getColor(R.color.DarkGreen));
                mBinding.title.setText("위급 상황 종료");
                mBinding.content.setText("10병동 103호실 화장실");
                mBinding.content1.setText("위급 상황이 종료되었습니다. 확인 후 닫기를 눌러주세요.");
            } else {
                finish();
            }

        }

        mBinding.btn.setOnClickListener(view -> {
            finish();
        });
//        handler = new Handler();
//
//        try {
//            setSocket("175.195.153.115", port);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        checkUpdate.start();
//
//        mBinding.btn.setOnClickListener(view -> {
//            PrintWriter out = new PrintWriter(networkWriter, true);
//            String return_msg = "175.195.153.234 Send -> Hi";
//            out.println(return_msg);
//        });
//
//        mBinding.connect.setOnClickListener(view -> {
//
//        });

    }

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                Log.w(TAG, "Update Running..");
                while ((line = networkReader.readLine()) != null) {
                    html = line;
                    handler.post(showUpdate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = () -> NurseCallUtils.printShort(getApplicationContext(), "Text: " + html);

    private void setSocket(String ip, int port) throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();


    }
}
