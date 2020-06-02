package com.sscctv.nursecallapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
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

public class PbxLoginActivity extends AppCompatActivity {
    private static final String TAG = "PbxLoginActivity";

    private EditText ip, port, id, pw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbx_login);

        ip = findViewById(R.id.edit_ip);
        port = findViewById(R.id.edit_port);
        id = findViewById(R.id.edit_user);
        pw = findViewById(R.id.edit_pw);

        ip.setText(PbxData.ipaddress);
        port.setText(PbxData.port);
        id.setText(PbxData.username);
        pw.setText(PbxData.password);
        findViewById(R.id.btnLogin).setOnClickListener(view -> {
            setLogin(id.getText().toString(), pw.getText().toString(), ip.getText().toString(), port.getText().toString());
        });

        test();

    }


    public void setLogin(String username, String password, String addr, String port) {
        OpenApi mApi = new OpenApi();
        OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
        try {
            mLoginApi.username = username;
            mLoginApi.password = NurseCallUtils.getEncMD5(password);
            mLoginApi.port = "";
            mLoginApi.version = "";
            mLoginApi.url = "";
            mLoginApi.urltag = "0";
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String url = EndPoint.PROTOCOL + addr + ":" + port + EndPoint.LOGIN;
        String json = new Gson().toJson(mLoginApi, OpenApi.LoginApi.class);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
                .build()).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                new Handler(getMainLooper()).post(() -> {
                    try {
                        Log.d(TAG, "Success: " + response);
                        JsonElement element = new JsonParser().parse(Objects.requireNonNull(response.body()).string());
                        String result = element.getAsJsonObject().get("status").getAsString();
                        Log.w(TAG, "Result: " + result);
                        if (result.equals("Success")) {
                            PbxData.token = element.getAsJsonObject().get("token").getAsString();
                            Log.d(TAG, "token: " + PbxData.token);

                            test();
                        } else {
                            PbxData.token = null;
                            NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        });
    }

//
////    }
//
//    public void postLogin(String username, String password, String addr, String port) {
//        OpenApi mApi = new OpenApi();
//        OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
//        try {
//            mLoginApi.username = username;
//            mLoginApi.password = NurseCallUtils.getEncMD5(password);
//            mLoginApi.port = "";
//            mLoginApi.version = "";
//            mLoginApi.url = "";
//            mLoginApi.urltag = "0";
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        //AsyncTask
//        String url = PROTOCOL + addr + ":" + port + EndPoint.LOGIN;
//        String json = new Gson().toJson(mLoginApi, OpenApi.LoginApi.class);
//
//        new PbxHttpClient().postSync(url, json);
//
//        if(PbxData.token != null) {
//            test();
//        } else {
//            NurseCallUtils.printShort(getApplicationContext(), "Pbx Login fail!");
//        }
//
//    }

    //
    private void test() {
        if (PbxData.token != null) {
            Intent intent = new Intent(this, PbxMenuActivity.class);
            startActivity(intent);
            finish();
        }
    }


}