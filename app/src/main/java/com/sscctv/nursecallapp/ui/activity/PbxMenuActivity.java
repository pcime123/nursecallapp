package com.sscctv.nursecallapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.pbx.EndPoint;
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

public class PbxMenuActivity  extends AppCompatActivity {
    private static final String TAG = "PbxMenuActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbx_menu);

        findViewById(R.id.btn_pbx_info).setOnClickListener(view -> {
            Intent intent = new Intent(this, PbxInfoActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_pbx_extension).setOnClickListener(view -> {
            Intent intent = new Intent(this, PbxExtActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_pbx_paging).setOnClickListener(view -> {

        });

        findViewById(R.id.btn_pbx_logout).setOnClickListener(view ->  {
            setLogout();
        });
    }

    public void setLogout() {
        String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.LOGOUT + "?token=" + PbxData.token;
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
                        Log.d(TAG, "Success: " + response);
                        JsonElement element = new JsonParser().parse(Objects.requireNonNull(response.body()).string());
                        String result = element.getAsJsonObject().get("status").getAsString();
                        Log.w(TAG, "Result: " + result);
                        if (result.equals("Success")) {
                            finish();
                        } else {
                            NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        });
    }

}
