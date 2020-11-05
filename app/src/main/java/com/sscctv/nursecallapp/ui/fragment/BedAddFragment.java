package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.databinding.FragBedAddBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedExtAddAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class BedAddFragment extends Fragment {

    private static final String TAG = BedAddFragment.class.getSimpleName();
    private static final String STRING_FORMAT = "%04d";

    private ArrayList<ExtItem> extItemArrayList;
    private TinyDB tinyDB;
    private Dialog dialog;
    private BedExtAddAdapter pbxExtensionAddAdapter;
    private ArrayList<ExtItem> compareArrayList;
    private String searchMode;
    private String passWord;
    private boolean isWard;

    private RecyclerView bedAddList;
    private Button btnSortNum, btnSortName, btnSortWard, btnSortRoom, btnSortBed;
    private CheckBox listChkBox;
    private boolean chkNum, chkName, chkWard, chkRoom, chkBed;

    public static BedAddFragment newInstance() {
        BedAddFragment fragment = new BedAddFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach()");
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        extItemArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_GET_EXTENSION);
        pbxExtensionAddAdapter = new BedExtAddAdapter(extItemArrayList);
        bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));

        tinyDB.putString(KeyList.DEVICE_SEARCH_WARD, "ward");
        searchMode = tinyDB.getString(KeyList.DEVICE_SEARCH_WARD);

        isWard = true;

        super.onResume();

    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        tinyDB.remove(KeyList.KEY_GET_EXTENSION);

        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragBedAddBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_bed_add, container, false);

        tinyDB = new TinyDB(getContext());

        bedAddList = layout.bedList;

        bedAddList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        bedAddList.setLayoutManager(layoutManager);

        layout.btnSearch.setOnClickListener(view -> {
            goSearch();
        });

        layout.btnPrev.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedListFragment.newInstance());
        });


        layout.btnImport.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedCSVFragment.newInstance());
        });

        compareArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);

        layout.bedAdd.setOnClickListener(view -> {
            if (pbxExtensionAddAdapter.getItemCount() == 0) {
                toastView(view, "선택된 병상이 없습니다. 다시 확인해주세요.");
                return;
            }

            ArrayList<ExtItem> curList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);

            List<ExtItem> selList = pbxExtensionAddAdapter.getSelectedItem();
            for (int i = 0; i < selList.size(); i++) {
                ExtItem item = selList.get(i);
                Log.d(TAG, item.getNum() + " ," + item.getName());
                curList.add(new ExtItem(item.getNum(), item.getName(), false));
            }

            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_CUR_EXTENSION, curList);
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedListFragment.newInstance());

        });

        listChkBox = layout.listCheckbox;
        listChkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (listChkBox.isChecked()) {
                pbxExtensionAddAdapter.setAllSelected(true);
                bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            } else {
                pbxExtensionAddAdapter.setAllSelected(false);
                bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            }
        });


        btnSortNum = layout.sortNum;
        btnSortName = layout.sortName;
        btnSortWard = layout.sortWard;
        btnSortRoom = layout.sortRoom;
        btnSortBed = layout.sortBed;

        btnSortNum.setOnClickListener(view -> {

            if (!chkNum) {
                chkNum = true;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkNum = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return ward.compareTo(compareWard);
                });
            }
            bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            init(1);

        });

        btnSortName.setOnClickListener(view -> {

            if (!chkName) {
                chkName = true;
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> t1.getName().compareTo(extItem.getName()));
            } else {
                chkName = false;
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> extItem.getName().compareTo(t1.getName()));
            }
            bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            init(2);
        });


        btnSortWard.setOnClickListener(view -> {

            if (!chkWard) {
                chkWard = true;
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });
            } else {
                chkWard = false;
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            init(3);
        });

        btnSortRoom.setOnClickListener(view -> {

            if (!chkRoom) {
                chkRoom = true;
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkRoom = false;
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {

                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));
            init(4);
        });

        btnSortBed.setOnClickListener(view -> {

            if (!chkBed) {
                chkBed = true;
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkBed = false;
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(extItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));

            init(5);
        });


        return layout.getRoot();
    }


    private void goSearch() {
        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_login);
        final CheckBox ckbox = dialog.findViewById(R.id.dialog_chk_ward);
        final EditText pw = dialog.findViewById(R.id.dialog_pbx_pw);
        final Button login = dialog.findViewById(R.id.dialog_pbx_login);
        final TextView ward = dialog.findViewById(R.id.dialog_ward);

        final LinearLayout ipLayout = dialog.findViewById(R.id.dialog_pbx_server);
        ipLayout.setVisibility(View.GONE);

        final ImageButton btnIpLayout = dialog.findViewById(R.id.dialog_view_pbx);
        btnIpLayout.setOnClickListener(view -> {
            if(ipLayout.getVisibility() == View.GONE) {
                ipLayout.setVisibility(View.VISIBLE);
                btnIpLayout.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp);
            } else {
                ipLayout.setVisibility(View.GONE);
                btnIpLayout.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp);
            }
        });

        final EditText pbxIp = dialog.findViewById(R.id.dialog_pbx_ip);
        pbxIp.setText(tinyDB.getString(KeyList.PBX_IP));

        final Button btnPing = dialog.findViewById(R.id.dialog_pbx_ping);
        btnPing.setOnClickListener(view -> {
            if(pingTest(pbxIp.getText().toString())) {
                NurseCallUtils.printShort(getContext(), "\uD83D\uDC4D IP-PBX 가 연결되어 있습니다.");
            } else {
                NurseCallUtils.printShort(getContext(), "⛔ IP-PBX 연결 상태 및 IP 주소를 확인하세요.");
            }
        });

        final LinearLayout layoutWard = dialog.findViewById(R.id.dialog_layout_ward);
        final LinearLayout layoutError = dialog.findViewById(R.id.dialog_layout_ward_error);

        ward.setText(tinyDB.getString(KeyList.DEVICE_WARD));
        if (ward.getText().toString().equals("")) {
            ckbox.setChecked(false);
            ckbox.setEnabled(false);
            layoutWard.setVisibility(View.GONE);
            layoutError.setVisibility(View.VISIBLE);
        } else {
            ckbox.setChecked(isWard);
            ckbox.setEnabled(isWard);
            layoutWard.setVisibility(View.VISIBLE);
            layoutError.setVisibility(View.GONE);
        }


        final Button toggle = dialog.findViewById(R.id.dialog_toggle_pw);
        toggle.setOnClickListener(view -> {
            if (pw.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
                pw.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggle.setText("보기");
            } else {
                pw.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggle.setText("숨기기");
            }
        });


        pw.setText("Sscctv12");
        login.setOnClickListener(view1 -> {
            if (pw.getText().toString().isEmpty()) {
                NurseCallUtils.printShort(getContext(), "패스워드가 입력되지 않았습니다.");
                return;
            }

            if (pingTest(pbxIp.getText().toString())) {
                isWard = ckbox.isChecked();
                passWord = pw.getText().toString();
                pbxExtensionAddAdapter.clear();

                TaskLogin(new goLogin());
            } else {
                NurseCallUtils.printShort(getContext(), "네트워크 연결 상태를 확인하세요.");
            }
        });
        dialog.show();

    }

    private void errorSearch() {
        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error_pbx);
        final TextView error = dialog.findViewById(R.id.dialog_txt_pbx_error);
        final Button close = dialog.findViewById(R.id.dialog_pbx_close);
        close.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
        dialog.show();

    }

    private void TaskLogin(goLogin asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class goLogin extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String ip = tinyDB.getString(KeyList.PBX_IP);
            String port = tinyDB.getString(KeyList.PBX_PORT);
            String id = tinyDB.getString(KeyList.PBX_ID);
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
                    TaskExtension(new getExtension());
                } else {
                    PbxData.token = null;
                    NurseCallUtils.printShort(getContext(), "비밀번호를 다시 확인해주세요.");
                }
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
        protected String doInBackground(Void... strings) {
            String url = EndPoint.PROTOCOL + tinyDB.getString(KeyList.PBX_IP) + ":" + tinyDB.getString(KeyList.PBX_PORT) + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;

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
//                            Log.d(TAG, "Ext Number: " + item.extnumber + " Name: " + item.username);

                            if (compareType(item.type) && compareName(item.username) && compareDevice(item.username)) {
//                                Log.d(TAG, "Ext Number: " + item.extnumber + " Name: " + item.username);
                                tempList.add(new ExtItem(item.extnumber, item.username, false));
                            }
                        }

                        for (int j = 0; j < tempList.size(); j++) {
                            for (int a = 0; a < compareArrayList.size(); a++) {
//                                Log.d(TAG, "Temp Number: " + tempList.get(j).getNum() + " Compare Num: " + compareArrayList.get(a).getNum());
                                if (tempList.get(j).getNum().equals(compareArrayList.get(a).getNum())) {
//                                    Log.e(TAG, "Temp Number: " + tempList.get(j).getNum() + " Compare Num: " + compareArrayList.get(a).getNum());
                                    tempList.remove(j);
                                }
                            }
                        }

                        if (isWard) {
                            if (searchMode.equals("ward")) {
                                String device_ward = tinyDB.getString(KeyList.DEVICE_WARD);

                                for (int i = 0; i < tempList.size(); i++) {
                                    String[] callerId = tempList.get(i).getName().split("-");
                                    String ward = callerId[1];
                                    Log.d(TAG, "Caller ID: " + callerId[2]);
                                    Log.d(TAG, "DEVICE: " + device_ward + " Ward: " + ward);
                                    if (device_ward.equals(ward)) {
                                        Log.w(TAG, "DEVICE1: " + device_ward + " Ward1: " + ward);

                                        extItemArrayList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                                    }
                                }
                            } else {
                                extItemArrayList = tempList;

                            }
                        } else {
                            for (int i = 0; i < tempList.size(); i++) {
                                extItemArrayList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                            }
                        }

                        NurseCallUtils.putExtList(tinyDB, KeyList.KEY_GET_EXTENSION, extItemArrayList);
                        bedAddList.setAdapter(new BedExtAddAdapter(extItemArrayList));

                        if (extItemArrayList.size() == 0) {
                            errorSearch();
                        }
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
                    progressDialog.dismiss();
                } else {
                    NurseCallUtils.printShort(getContext(), "실패했습니다.");
                    progressDialog.dismiss();

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

//    private boolean compareDevice(String name) {
//        return !name.contains(KeyList.MODEL_TELEPHONE_MASTER) && !name.contains(KeyList.MODEL_TELEPHONE_SECURITY)
//                && !name.contains(KeyList.MODEL_TELEPHONE_PUBLIC);
//    }

    //    private boolean compareDevice(String name) {
//        return name.contains(KeyList.MODEL_PAGER_BASIC) && name.contains(KeyList.MODEL_PAGER_EXTENTION)
//                && name.contains(KeyList.MODEL_PAGER_BASIC_WALL) && name.contains(KeyList.MODEL_PAGER_BASIC_STAND);
//    }
    private boolean compareDevice(String name) {
        return name.contains(KeyList.MODEL_PAGER_BASIC) || name.contains(KeyList.MODEL_PAGER_EXTENTION)
                || name.contains(KeyList.MODEL_PAGER_BASIC_WALL) || name.contains(KeyList.MODEL_PAGER_BASIC_STAND);
    }


    private boolean pingTest(String ip) {
        Runtime runtime = Runtime.getRuntime();

        String cmd = "ping -c 1 -W 3 " + ip;

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
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 2:
                chkNum = false;
                chkWard = false;
                chkRoom = false;
                chkBed = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 3:
                chkNum = false;
                chkName = false;
                chkRoom = false;
                chkBed = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 4:
                chkNum = false;
                chkName = false;
                chkWard = false;
                chkBed = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 5:
                chkNum = false;
                chkName = false;
                chkWard = false;
                chkRoom = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
        }
    }

    private void toastView(View view,String str) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View toastDesign = layoutInflater.inflate(R.layout.custom_toast, view.findViewById(R.id.toast_design_root));

        TextView text = toastDesign.findViewById(R.id.TextView_toast_design);
        text.setText(str);

        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastDesign);
        toast.show();
    }

}
