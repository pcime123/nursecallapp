package com.sscctv.nursecallapp.pbx;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

public class PbxHttpClient {
    private static final String TAG = "PbxHttpClient";
    private String message;

//    public String postSync(String requestURL, String jsonMessage) {
//
//        message = null;
//
//        try {
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(requestURL)
//                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonMessage))
//                    .build();
//
//            Response response = client.newCall(request).execute();
//            JsonElement element = new JsonParser().parse(Objects.requireNonNull(response.body()).string());
//            String result = element.getAsJsonObject().get("status").getAsString();
//            Log.w(TAG, "Result: " + result);
//            if (result.equals("Success")) {
//                PbxData.token = element.getAsJsonObject().get("token").getAsString();
//                return Objects.requireNonNull(response.body()).toString();
//            } else {
//                PbxData.token = null;
//                return "Failed";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "null";
//    }

    public void postSync(String requestURL, String jsonMessage) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(new Request.Builder()
                .url(requestURL)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonMessage))
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "Success: " + response);
                JsonElement element = new JsonParser().parse(Objects.requireNonNull(response.body()).string());
                String result = element.getAsJsonObject().get("status").getAsString();
                Log.w(TAG, "Result: " + result);
                if (result.equals("Success")) {
                    PbxData.token = element.getAsJsonObject().get("token").getAsString();
                } else {
                    PbxData.token = null;
                }
            }
        });


    }

}



