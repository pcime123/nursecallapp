package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.APagingBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.Arrays;
import java.util.List;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class PagingActivity extends AppCompatActivity {
    private static final String TAG = PagingActivity.class.getSimpleName();
    private APagingBinding mBinding;
    private TinyDB tinyDB;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.a_paging);
        tinyDB = new TinyDB(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mBinding.pagingList.setHasFixedSize(true);
        mBinding.pagingList.setLayoutManager(layoutManager);

        mBinding.btnPagingAdd.setOnClickListener(view -> {
            TaskLogin(new goLogin(), "addList");
        });

        mBinding.btnPlayMusic.setOnClickListener(view -> {
            TaskLogin(new goLogin(), "playMusic");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        TaskLogin(new goLogin(), "getList");
    }

    private void TaskLogin(goLogin asyncTask, String mode) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mode);
    }

    private class goLogin extends AsyncTask<String, Void, String> {
        private String mode;

        protected String doInBackground(String... strings) {
            mode = strings[0];

            String ip = tinyDB.getString(KeyList.PBX_IP);
            String port = tinyDB.getString(KeyList.PBX_PORT);
            String id = tinyDB.getString(KeyList.PBX_ID);
            String str_pw = "Sscctv12";

            OpenApi mApi = new OpenApi();
            OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
            try {
                mLoginApi.username = id;
                mLoginApi.password = NurseCallUtils.getEncMD5(str_pw);
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
                Log.d(TAG, "Success: " + result);
                JsonElement element = new JsonParser().parse(result);
                String result1 = element.getAsJsonObject().get("status").getAsString();
                if (result1.equals("Success")) {
                    PbxData.token = element.getAsJsonObject().get("token").getAsString();
                    switch (mode) {
                        case "getList":
                            TaskExtension(new getExtension());
                            break;
                        case "addList":
                            TaskAddPaging(new addPagingTask());
                            break;
                        case "playMusic":
                            TaskPlayMusic(new PlayMusicTask());
                            break;
                    }
                } else {
                    PbxData.token = null;
                    NurseCallUtils.printShort(getApplicationContext(), "비밀번호를 다시 확인해주세요.");
                }
            }
        }
    }

    private void TaskExtension(getExtension asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class getExtension extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(PagingActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("가져오는중...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.QUERY_PAGING_GROUP_LIST + "?token=" + PbxData.token;

            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    JsonElement js = element.getAsJsonObject().get("paginggrouplist");
                    OpenApi.PagingGroupList[] pagingGroupLists = new Gson().fromJson(js, OpenApi.PagingGroupList[].class);

                    if (pagingGroupLists != null) {
                        List<OpenApi.PagingGroupList> items = Arrays.asList(pagingGroupLists);


                        for (int i = 0; i < items.size(); i++) {
                            Log.d(TAG, "Number: " + items.get(i).number + " Name: " + items.get(i).name);
                        }
                        PagingAdapter adapter = new PagingAdapter(getApplicationContext(), items);
                        mBinding.pagingList.setAdapter(adapter);
                    }
                }
                progressDialog.dismiss();
                TaskLogout(new goLogOut());
            }
        }
    }

    private void TaskAddPaging(addPagingTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class addPagingTask extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(PagingActivity.this);
        String number, name, type, member, membergroup, enabledialtoanswer;

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("가져오는중...");
            progressDialog.show();

            number = mBinding.txtPagingNum1.getText().toString();
            name = mBinding.txtPagingName1.getText().toString();
            type = mBinding.txtPagingDuplex.getText().toString();
            member = mBinding.txtPagingMember.getText().toString();
            membergroup = mBinding.txtPagingGroup.getText().toString();
            enabledialtoanswer = mBinding.txtPagingAnswer.getText().toString();
        }

        protected String doInBackground(Void... strings) {

            OpenApi mApi = new OpenApi();
            OpenApi.PagingGroupAdd pagingGroupAdd = mApi.new PagingGroupAdd();
            try {
                pagingGroupAdd.number = number;
                pagingGroupAdd.name = name;
                pagingGroupAdd.duplex = type;
                pagingGroupAdd.allowexten = member;
                pagingGroupAdd.allowextengroup = membergroup;
                pagingGroupAdd.enablekeyhanup = enabledialtoanswer;
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.ADD_PAGING_GROUP + "?token=" + PbxData.token;
            String json = new Gson().toJson(pagingGroupAdd, OpenApi.PagingGroupAdd.class);
            Log.d(TAG, "Json: " + json);
            return postSync(url, json);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String str = element.getAsJsonObject().get("status").getAsString();

                Log.w(TAG, "addPagingTask Result: " + str);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    NurseCallUtils.printShort(getApplicationContext(), "추가가 완료되었습니다.");
                    TaskLogin(new goLogin(), "getList");
                } else {
                    NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                }
                progressDialog.dismiss();
                TaskLogout(new goLogOut());
            }
        }
    }

    private void TaskPlayMusic(PlayMusicTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PlayMusicTask extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(PagingActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("가져오는중...");
            progressDialog.show();

        }

        protected String doInBackground(Void... strings) {

            OpenApi mApi = new OpenApi();
            OpenApi.PagingPlayMusic pagingPlayMusic = mApi.new PagingPlayMusic();
            try {
                pagingPlayMusic.callee = "6300";
                pagingPlayMusic.volume = "10";
                pagingPlayMusic.prompt = "music1";
                pagingPlayMusic.count = "3";
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.START_PAGING_GROUP_MUSIC + "?token=" + PbxData.token;
            String json = new Gson().toJson(pagingPlayMusic, OpenApi.PagingPlayMusic.class);
            Log.d(TAG, "Json: " + json);
            return postSync(url, json);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String str = element.getAsJsonObject().get("status").getAsString();

                Log.w(TAG, "PlayMusicTask Result: " + str);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    NurseCallUtils.printShort(getApplicationContext(), "실행되었습니다.");
                } else {
                    NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                }
                progressDialog.dismiss();
                TaskLogout(new goLogOut());
            }
        }
    }

    private void TaskLogout(AsyncTask<Void, Void, String> asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class goLogOut extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(PagingActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
        }

        protected String doInBackground(Void... params) {
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.LOGOUT + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String str = element.getAsJsonObject().get("status").getAsString();
                Log.w(TAG, "LogOut Result: " + str);
                if (str.equals("Success")) {
                    progressDialog.dismiss();
                } else {
                    NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                    progressDialog.dismiss();

                }
            }
        }
    }
}
