package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityPbxExtBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.adapter.PbxExtAdapter;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class PbxExtActivity extends AppCompatActivity {
    private static final String TAG = "PbxExtActivity";

    private ActivityPbxExtBinding mBinding;
    private LinearLayoutManager layoutManager;
    private PbxExtAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pbx_ext);
        mBinding.listPbxExt.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(getApplication());
        layoutManager.setAutoMeasureEnabled(false);
        mBinding.listPbxExt.setHasFixedSize(true);
        mBinding.listPbxExt.setLayoutManager(layoutManager);

        new SampleTask().execute();


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class SampleTask extends AsyncTask < Void, Void, String > {
        protected String doInBackground(Void...params) {
            String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;
            return postSync(url, "");
        }


        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result != null) {
                JsonElement element = new JsonParser().parse(result);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    JsonElement result1 = element.getAsJsonObject().get("extlist");
                    OpenApi.ExtList[] mExtListArray = new Gson().fromJson(result1, OpenApi.ExtList[].class);
                    if (mExtListArray != null) {
                        List<OpenApi.ExtList> items = Arrays.asList(mExtListArray);
                        mBinding.listPbxExt.setAdapter(new PbxExtAdapter(items));
                    }
                }
            }
        }
    }




}
