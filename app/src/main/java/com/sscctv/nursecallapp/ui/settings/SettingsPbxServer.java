package com.sscctv.nursecallapp.ui.settings;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsAdminPbxBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SettingsPbxServer extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private SettingsAdminPbxBinding mBinding;
    private String ip, port, id, pw;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_admin_pbx);

        tinyDB = new TinyDB(this);
        ipInput();

        mBinding.btnAdminAccountRefresh.setOnClickListener(view -> {
            getData();
            if (isCheck()) {
                new goLogin().execute();
            } else {
                mBinding.settingsPbxStatus.setText("값 확인하셈");
            }
        });


        mBinding.btnAdminPing.setOnClickListener(view -> {
            if (pingTest()) {
                NurseCallUtils.printShort(this, "핑 테스트 성공. 네트워크가 정상적으로 연결되어 있습니다.");
            } else {
                NurseCallUtils.printShort(this, "핑 테스트 실패!!. 네트워크 연결 상태를 확인하세요.");

            }
        });

        mBinding.btnAdminPwView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBinding.btnAdminPwView.setBackgroundColor(getResources().getColor(R.color.temp));
                    mBinding.editPbxPw.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mBinding.btnAdminPwView.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mBinding.editPbxPw.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    break;

            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        openGetInfo();
        openGetSetup();

        getData();

        if (isCheck()) {
            new goLogin().execute();
        } else {
            mBinding.settingsPbxStatus.setText("값 확인하셈");
        }
    }

    @Override
    protected void onPause() {
        closeSaveInfo();
        closeSaveSetup();
        super.onPause();
    }

    private boolean isCheck() {
        return !mBinding.editPbxIp.getText().toString().isEmpty() && !mBinding.editPbxPort.getText().toString().isEmpty()
                && !mBinding.editPbxId.getText().toString().isEmpty() && !mBinding.editPbxPw.getText().toString().isEmpty();
    }

    private String postSync(String requestURL, String jsonMessage) {

        String message = null;

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestURL)
                    .post(RequestBody.create(jsonMessage, MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Response response = client.newCall(request).execute();
            message = Objects.requireNonNull(response.body()).string();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    private class goLogin extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(SettingsPbxServer.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
        }

        protected String doInBackground(Void... params) {

            OpenApi mApi = new OpenApi();
            OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
            try {
                mLoginApi.username = id;
                mLoginApi.password = NurseCallUtils.getEncMD5(pw);
                mLoginApi.port = "";
                mLoginApi.version = "";
                mLoginApi.url = "";
                mLoginApi.urltag = "0";
            } catch (Exception e) {
                e.printStackTrace();
            }

            String url = EndPoint.PROTOCOL + ip + ":" + port + EndPoint.LOGIN;
            String json = new Gson().toJson(mLoginApi, OpenApi.LoginApi.class);
            return postSync(url, json);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String result1 = element.getAsJsonObject().get("status").getAsString();
                if (result1.equals("Success")) {
                    PbxData.token = element.getAsJsonObject().get("token").getAsString();
                    mBinding.settingsPbxStatus.setText("연결됨");
                } else {
                    PbxData.token = null;
                    mBinding.settingsPbxStatus.setText("연결 안됨!!");
                }
            } else {
                mBinding.settingsPbxStatus.setText("네트워크 안됨..");
            }
            progressDialog.dismiss();

        }
    }

    private boolean pingTest() {
        Runtime runtime = Runtime.getRuntime();

        String host = mBinding.editPbxIp.getText().toString();
        String cmd = "ping -c 1 -W 3 " + host;

        Process process = null;

        try {
            process = runtime.exec(cmd);
        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        try {
            assert process != null;
            process.waitFor();
        } catch (InterruptedException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        int result = process.exitValue();

        return result == 0;
    }

    private void ipInput() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) + destTxt.substring(dend);
                if (!resultingTxt.matches("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (int i = 0; i < splits.length; i++) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        mBinding.editPbxIp.setFilters(filters);

    }

    private void getData() {
        ip = mBinding.editPbxIp.getText().toString();
        port = mBinding.editPbxPort.getText().toString();
        id = mBinding.editPbxId.getText().toString();
        pw = mBinding.editPbxPw.getText().toString();
    }

    private String getPbxData() {
        String flag = "_@#@_";
        ip = mBinding.editPbxIp.getText().toString();
        port = mBinding.editPbxPort.getText().toString();
        id = mBinding.editPbxId.getText().toString();
        pw = mBinding.editPbxPw.getText().toString();

        return ip + flag + port + flag + id + flag + pw;
    }


    private String getPbxSetup() {
        String flag = "_@#@_";
        String time, detail, detail2;

        if(mBinding.pbxTimeSync.isChecked()) {
            time = "true";
        } else {
            time = "false";
        }

        if (mBinding.pbxDetailSearch.isChecked()) {
            detail = "true";
        } else {
            detail = "false";
        }

        if(mBinding.pbxDetailSearch2.isChecked()) {
            detail2 = "true";
        } else {
            detail2 = "false";
        }

        return time + flag + detail + flag + detail2;
    }

    private void openGetInfo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PBX);
        String str;
        FileReader fileReader;
        BufferedReader bufferedReader;
        if (file.exists()) {
            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                while ((str = bufferedReader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        String[] info = str.split("_@#@_");
                        mBinding.editPbxIp.setText(info[0]);
                        mBinding.editPbxPort.setText(info[1]);
                        mBinding.editPbxId.setText(info[2]);
                        mBinding.editPbxPw.setText(info[3]);
                    }
                }
                bufferedReader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSaveInfo() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PBX);
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(getPbxData());
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGetSetup() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PBX_SETUP);
        String str;
        FileReader fileReader;
        BufferedReader bufferedReader;
        if (file.exists()) {
            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                while ((str = bufferedReader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        String[] info = str.split("_@#@_");
                        if(info[0].equals("true")) {
                            mBinding.pbxTimeSync.setChecked(true);
                        } else {
                            mBinding.pbxTimeSync.setChecked(false);
                        }

                        if(info[1].equals("true")) {
                            mBinding.pbxDetailSearch.setChecked(true);
                        } else {
                            mBinding.pbxDetailSearch.setChecked(false);
                        }

                        if(info[2].equals("true")) {
                            mBinding.pbxDetailSearch2.setChecked(true);
                        } else {
                            mBinding.pbxDetailSearch2.setChecked(false);
                        }

                    }
                }
                bufferedReader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSaveSetup() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PBX_SETUP);
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(getPbxSetup());
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
