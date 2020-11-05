package com.sscctv.nursecallapp.ui.settings;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.databinding.FragSetAccountBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;


public class SetAccountFragment extends Fragment {

    private static final String TAG = SetAccountFragment.class.getSimpleName();
    private Core core;
    private TinyDB tinyDB;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    private FragSetAccountBinding mBinding;
    private Dialog dialog;
    private InputMethodManager imm;
    public static SetAccountFragment newInstance() {
        SetAccountFragment fragment = new SetAccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        String str = args.getString("str");
        Log.d(TAG, "String: " + str);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        core.addListener(mCoreListener);
//        setTestAccount();
        getPbxInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        core.removeListener(mCoreListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_set_account, container, false);

        tinyDB = new TinyDB(getContext());
        core = MainCallService.getCore();
        mAccountCreator = core.createAccountCreator(null);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String msg) {
                super.onRegistrationStateChanged(core, cfg, state, msg);
                if (state == RegistrationState.Ok) {
                    tinyDB.putString(KeyList.SIP_ID, mBinding.editSipId.getText().toString());
                    tinyDB.putString(KeyList.SIP_PW, mBinding.editSipPw.getText().toString());
                    tinyDB.putString(KeyList.PBX_IP, mBinding.editSetPbxIp.getText().toString());
                } else {
                    tinyDB.putString(KeyList.SIP_ID, "");
                    tinyDB.putString(KeyList.SIP_PW, "");
                    tinyDB.putString(KeyList.PBX_IP, "");
                }

                switch (state) {
                    case Ok:
//                        mBinding.txtResult.setText(String.format("OK: %s", msg));
                        mBinding.txtResult.setTextColor(getResources().getColor(R.color.blue));
                        mBinding.txtResult.setText("계정 설정이 완료되었습니다.");
                        break;
                    case Failed:
//                        mBinding.txtResult.setText(String.format("Failed: %s", msg));
                        mBinding.txtResult.setTextColor(getResources().getColor(R.color.red));
                        mBinding.txtResult.setText("계정 설정에 실패했습니다. 입력한 값을 확인해주세요");
                        break;
                    case Progress:
//                        mBinding.txtResult.setText(String.format("Progress: %s", msg));
                        mBinding.txtResult.setTextColor(getResources().getColor(R.color.text_hotpink));
                        mBinding.txtResult.setText("계정 설정 진행중입니다. 완료되지 않을경우 다시 실행해주세요");
                        break;
                    case Cleared:
                        mBinding.txtResult.setText(String.format("Cleared: %s", msg));
                        break;
                    case None:
                        mBinding.txtResult.setText(String.format("None: %s", msg));
                        break;

                }

            }
        };

        mBinding.btnSipSetup.setOnClickListener(view -> {
//            if (Objects.requireNonNull(imm).isActive()) {
//                Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getCurrentFocus()).getWindowToken(), 0);
//            }
            mBinding.editSipPw.requestFocus();
            imm.hideSoftInputFromWindow(mBinding.editSipPw.getWindowToken(), 0);
            cfgAccount();
        });

        mBinding.btnPbxSetup.setOnClickListener(view -> {
            String ip, port, id, pw;
            ip = mBinding.editSetPbxIp.getText().toString();
            port = mBinding.editSetPbxPort.getText().toString();
            id = mBinding.editSetPbxId.getText().toString();
            pw = mBinding.editSetPbxPw.getText().toString();

            if (ip.isEmpty()) {
                NurseCallUtils.printShort(getContext(), "PBX IP 주소 값을 다시 확인해주세요.");
                return;
            }

            if (port.isEmpty()) {
                NurseCallUtils.printShort(getContext(), "PBX 포트 값을 다시 확인해주세요.");
                return;
            }

            if (id.isEmpty()) {
                NurseCallUtils.printShort(getContext(), "PBX 아이디 값을 다시 확인해주세요.");
                return;
            }

            if (pw.isEmpty()) {
                NurseCallUtils.printShort(getContext(), "PBX 비밀번호 값을 다시 확인해주세요.");
                return;
            }

            TaskLogin(new goLogin(), ip, port, id, pw);
        });


        if (tinyDB.getString(KeyList.PBX_IP).equals("")) {
            mBinding.btnSipSetup.setEnabled(false);
            mBinding.btnSipSetup.setText("PBX가 설정되어 있지 않습니다. PBX 설정 후 시도하세요.");
            mBinding.btnSipSetup.setTextColor(getResources().getColor(R.color.black));

        }

        return mBinding.getRoot();
    }


    private void cfgAccount() {
        mAccountCreator.setUsername(mBinding.editSipId.getText().toString());
        mAccountCreator.setPassword(mBinding.editSipPw.getText().toString());
        mAccountCreator.setDomain(mBinding.editSetPbxIp.getText().toString());
        mAccountCreator.setDisplayName(mBinding.editSipId.getText().toString());
        mAccountCreator.setTransport(TransportType.Udp);

        core.clearProxyConfig();
        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();
        core.setDefaultProxyConfig(proxyConfig);
    }

    private void getPbxInfo() {
        mBinding.editSipId.setText(tinyDB.getString(KeyList.SIP_ID));
        mBinding.editSipPw.setText(tinyDB.getString(KeyList.SIP_PW));

        mBinding.editSetPbxIp.setText(tinyDB.getString(KeyList.PBX_IP));
        mBinding.editSetPbxPort.setText(tinyDB.getString(KeyList.PBX_PORT));
        mBinding.editSetPbxId.setText(tinyDB.getString(KeyList.PBX_ID));
    }

    private void TaskLogin(AsyncTask<String, Void, String> asyncTask, String ip, String port, String id, String pw) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip, port, id, pw);
    }

    private class goLogin extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String ip = strings[0];
            String port = strings[1];
            String id = strings[2];
            String str_pw = strings[3];

            tinyDB.putString(KeyList.PBX_IP, ip);
            tinyDB.putString(KeyList.PBX_PORT, port);
            tinyDB.putString(KeyList.PBX_ID, id);

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
                    progressDialog.dismiss();
//                    new getExtension().execute(element.getAsJsonObject().get("token").getAsString());
                    TaskExtension(new getExtension());
                } else {
                    PbxData.token = null;
                    NurseCallUtils.printShort(getContext(), "비밀번호를 다시 확인해주세요.");
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
            }
        }
    }

    private void TaskExtension(getExtension asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class getExtension extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("번호 가져오는중...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                JsonElement element = new JsonParser().parse(result);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    JsonElement js = element.getAsJsonObject().get("extlist");
                    OpenApi.ExtList[] mExtListArray = new Gson().fromJson(js, OpenApi.ExtList[].class);

                    if (mExtListArray != null) {
                        List<OpenApi.ExtList> items = Arrays.asList(mExtListArray);
                        ArrayList<ExtItem> tempList = new ArrayList<>();

                        for (int i = 0; i < items.size(); i++) {
                            final OpenApi.ExtList item = items.get(i);
//                            Log.d(TAG, "Ext Number: " + item.extnumber + " Name: " + item.username);
                            tempList.add(new ExtItem(item.extnumber, item.username, false));
                        }
                        NurseCallUtils.putExtList(tinyDB, KeyList.KEY_ALL_EXTENSION, tempList);
                    }
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

        ProgressDialog progressDialog = new ProgressDialog(getContext());

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
                Log.w(TAG, "Result: " + str);
                if (str.equals("Success")) {
                    mBinding.btnSipSetup.setEnabled(true);
                    mBinding.btnSipSetup.setText("SIP 계정 설정");
                    mBinding.btnSipSetup.setTextColor(getResources().getColor(R.color.white));

                    NurseCallUtils.printShort(getContext(), "PBX 설정이 완료되었습니다.");

                    progressDialog.dismiss();
                } else {
                    NurseCallUtils.printShort(getContext(), "실패했습니다.");
                    progressDialog.dismiss();

                }
            }
        }
    }
}
