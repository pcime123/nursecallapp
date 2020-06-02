package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityPbxInfoBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.pbx.PbxHttpClient;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PbxInfoActivity extends AppCompatActivity {
    private static final String TAG = "PbxInfoActivity";

    private ActivityPbxInfoBinding mBinding;

    OpenApi.DeviceInfo deviceInfo = null;
    String response;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pbx_info);
        response = null;

        setLogout();
    }

    public void setLogout() {
        String url = EndPoint.PROTOCOL+ PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_PBX_INFO + "?token=" + PbxData.token;

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                .build()).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                new Handler(getMainLooper()).post(() -> {
                    try {
                        JsonElement element = new JsonParser().parse(Objects.requireNonNull(response.body()).string());

                        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                            JsonElement result = element.getAsJsonObject().get("deviceinfo");


                            deviceInfo = new Gson().fromJson(result, OpenApi.DeviceInfo.class);

                            if(deviceInfo != null) {
                                mBinding.editDevice.setText(deviceInfo.devicename);
                                mBinding.editSerial.setText(deviceInfo.sn);
                                mBinding.editHwVersion.setText(deviceInfo.hardwarever);
                                mBinding.editPw.setText(deviceInfo.firmwarever);
                                mBinding.editSysTime.setText(deviceInfo.systemtime);
                                mBinding.editActTime.setText(deviceInfo.uptime);
                                mBinding.editExtList.setText(deviceInfo.extensionstatus);
                            }
                        }
                        Log.d(TAG, "Success: " + response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        });
    }
    public void queryPbxInfo() {
        String url = EndPoint.PROTOCOL+ PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_PBX_INFO + "?token=" + PbxData.token;
        Log.d(TAG, "url: " + url);
        if (response == null) {
            return;
        }
//
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                response = new PbxHttpClient().postSync(url, "");
//            }
//        };
//        thread.start();

        Log.d(TAG, "response: " + response);
        JsonElement element = new JsonParser().parse(response);
        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
            JsonElement result = element.getAsJsonObject().get("deviceinfo");

            new Gson().fromJson(result, OpenApi.DeviceInfo.class);
        }
    }
}