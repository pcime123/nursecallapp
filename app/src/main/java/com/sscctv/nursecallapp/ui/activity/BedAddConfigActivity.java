package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityBedAddBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.adapter.ExtItem;
import com.sscctv.nursecallapp.ui.adapter.PbxExtensionAddAdapter;
import com.sscctv.nursecallapp.ui.adapter.PbxExtensionListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class BedAddConfigActivity extends AppCompatActivity {
    private static final String TAG = "BedAddConfigActivity";
    private static final String STRING_FORMAT = "%04d";

    private ActivityBedAddBinding mBinding;
    private ArrayList<ExtItem> extItemArrayList;
    private TinyDB tinyDB;
    private Dialog dialog;
    private PbxExtensionAddAdapter pbxExtensionAddAdapter;
    private ArrayList<ExtItem> compareArrayList;
    private String searchMode;
    private boolean chkNum, chkName, chkWard, chkRoom, chkBed;
    private String passWord;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_bed_add);
        tinyDB = new TinyDB(this);

        mBinding.bedList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.bedList.setLayoutManager(layoutManager);

        mBinding.btnSearch.setOnClickListener(view -> {
            goSearch();
        });

        mBinding.btnImport.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, CSVScanActivity.class);
        });
        mBinding.btnServer.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PbxExtActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        compareArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);

        mBinding.bedAdd.setOnClickListener(view -> {
            ArrayList<ExtItem> curList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);

            List<ExtItem> selList = pbxExtensionAddAdapter.getSelectedItem();
            for (int i = 0; i < selList.size(); i++) {
                ExtItem item = selList.get(i);
                Log.d(TAG, item.getNum() + " ," + item.getName());
                curList.add(new ExtItem(item.getNum(), item.getName(), false));
            }

            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_CUR_EXTENSION, curList);
            finish();

        });

        mBinding.listCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mBinding.listCheckbox.isChecked()) {
                pbxExtensionAddAdapter.setAllSelected(true);
                mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            } else {
                pbxExtensionAddAdapter.setAllSelected(false);
                mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            }
        });

        mBinding.sortNum.setOnClickListener(view -> {


            if (!chkNum) {
                chkNum = true;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    Log.d(TAG, "Ward: "  + ward + " Compare: " + compareWard);

                    return compareWard.compareTo(ward);
                });

            } else {
                chkNum = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    Log.d(TAG, "Compare: "  + compareWard + " Ward: " + ward);

                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            init(1);

        });

        mBinding.sortName.setOnClickListener(view -> {

            if (!chkName) {
                chkName = true;
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> t1.getName().compareTo(extItem.getName()));
            } else {
                chkName = false;
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> extItem.getName().compareTo(t1.getName()));
            }
            mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            init(2);
        });


        mBinding.sortWard.setOnClickListener(view -> {

            if (!chkWard) {
                chkWard = true;
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]","")));
                    return compareWard.compareTo(ward);
                });
            } else {
                chkWard = false;
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]","")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            init(3);
        });

        mBinding.sortRoom.setOnClickListener(view -> {

            if (!chkRoom) {
                chkRoom = true;
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]","")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkRoom = false;
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {

                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]","")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            init(4);
        });

        mBinding.sortBed.setOnClickListener(view -> {

            if (!chkBed) {
                chkBed = true;
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]","")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkBed = false;
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]","")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]","")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));
            init(5);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //all, ward
//        extItemArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_GET_EXTENSION);
        extItemArrayList = new ArrayList<>();
        Log.d(TAG, "Size: " + extItemArrayList.size());
        pbxExtensionAddAdapter = new PbxExtensionAddAdapter(extItemArrayList);
        mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));

        tinyDB.putString(KeyList.DEVICE_SEARCH_WARD, "ward");
//        tinyDB.putString(KeyList.DEVICE_WARD, "10");
        searchMode = tinyDB.getString(KeyList.DEVICE_SEARCH_WARD);

    }

    @Override
    protected void onPause() {
        super.onPause();
        tinyDB.remove(KeyList.KEY_GET_EXTENSION);

    }

    private void goSearch() {
        dialog = new Dialog(BedAddConfigActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_login);
        final EditText pw = dialog.findViewById(R.id.dialog_pbx_pw);
        final Button login = dialog.findViewById(R.id.dialog_pbx_login);

        pw.setText("Sscctv12");
        login.setOnClickListener(view1 -> {
            if (pw.getText().toString().isEmpty()) {
                NurseCallUtils.printShort(this, "패스워드가 입력되지 않았습니다.");
                return;
            }

            if (pingTest()) {
                passWord = pw.getText().toString();
                new goLogin().execute();
            } else {
                NurseCallUtils.printShort(this, "네트워크 연결 상태를 확인하세요.");
            }
        });
        dialog.show();

    }

    private void errorSearch() {
        dialog = new Dialog(BedAddConfigActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_error);
        final TextView error = dialog.findViewById(R.id.dialog_txt_pbx_error);
        final Button close = dialog.findViewById(R.id.dialog_pbx_close);
        close.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
        dialog.show();

    }

    private class goLogin extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String ip = "175.195.153.235";
            String port = "8088";
            String id = "sscctv";
            String str_pw = passWord;

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
                    dialog.dismiss();
                    new getExtension().execute();
                } else {
                    PbxData.token = null;
                    NurseCallUtils.printShort(getApplicationContext(), "비밀번호를 다시 확인해주세요.");
                }
            }
        }
    }


    private class getExtension extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

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
                            if (compareType(item.type) && compareName(item.username) && compareDevice(item.username)) {
                                Log.d(TAG, "Ext Number: " + item.extnumber + " Name: " + item.username);
                                tempList.add(new ExtItem(item.extnumber, item.username, false));
                            }
                        }

                        for (int j = 0; j < tempList.size(); j++) {
                            for (int a = 0; a < compareArrayList.size(); a++) {
                                Log.d(TAG, "Temp Number: " + tempList.get(j).getNum() + " Compare Num: " + compareArrayList.get(a).getNum());
                                if (tempList.get(j).getNum().equals(compareArrayList.get(a).getNum())) {
                                    Log.e(TAG, "Temp Number: " + tempList.get(j).getNum() + " Compare Num: " + compareArrayList.get(a).getNum());
                                    tempList.remove(j);
                                }
                            }
                        }

                        if (searchMode.equals("ward")) {
                            String device_ward = tinyDB.getString(KeyList.DEVICE_WARD);

                            for (int i = 0; i < tempList.size();i++) {
                                String[] callerId = tempList.get(i).getName().split("-");
                                String ward = callerId[1];
                                Log.d(TAG, "Caller ID: " + callerId[2]);
                                Log.d(TAG, "DEVICE: " + device_ward + " Ward: " + ward);
                                if (device_ward.equals(ward)) {
                                    extItemArrayList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                                }
                            }
                        } else {
                            extItemArrayList = tempList;

                        }
                        NurseCallUtils.putExtList(tinyDB, KeyList.KEY_GET_EXTENSION, extItemArrayList);
                        mBinding.bedList.setAdapter(new PbxExtensionAddAdapter(extItemArrayList));

                        if (extItemArrayList.size() == 0) {
                            errorSearch();
                        }
                    }
                }

                new goLogOut().execute();

            }
        }
    }

    private class goLogOut extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.LOGOUT + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String str = element.getAsJsonObject().get("status").getAsString();
                Log.w(TAG, "Result: " + str);
                if (str.equals("Success")) {
//                            finish();
                } else {
                    NurseCallUtils.printShort(getApplicationContext(), "실패했습니다.");
                }
            }
        }
    }

    private boolean compareType(String type) {
        return type.equals("SIP");
    }

    private boolean compareName(String item) {
        return item.contains("-");
    }

    private boolean compareDevice(String name) {
        if (name.contains(KeyList.MODEL_TELEPHONE_MASTER) || name.contains(KeyList.MODEL_TELEPHONE_SECURITY)
                || name.contains(KeyList.MODEL_TELEPHONE_PUBLIC) || name.contains(KeyList.MODEL_TELEPHONE_OPERATION)
        || name.contains(KeyList.MODEL_PAGER_PUBLIC) || name.contains(KeyList.MODEL_PAGER_PUBLIC_01)) {
            return false;
        }
        return true;
    }

    private boolean pingTest() {
        Runtime runtime = Runtime.getRuntime();

        String host = "175.195.153.235";
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

    private void init(int mode) {
        switch (mode) {
            case 0:
                chkNum = false;
                chkName = false;
                chkWard = false;
                chkRoom = false;
                chkBed = false;
                break;
            case 1:
                chkName = false;
                chkWard = false;
                chkRoom = false;
                chkBed = false;
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 2:
                chkNum = false;
                chkWard = false;
                chkRoom = false;
                chkBed = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 3:
                chkNum = false;
                chkName = false;
                chkRoom = false;
                chkBed = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 4:
                chkNum = false;
                chkName = false;
                chkWard = false;
                chkBed = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 5:
                chkNum = false;
                chkName = false;
                chkWard = false;
                chkRoom = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
        }

    }
}
