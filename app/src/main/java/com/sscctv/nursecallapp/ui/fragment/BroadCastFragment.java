package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BcItem;
import com.sscctv.nursecallapp.data.RoomItem;
import com.sscctv.nursecallapp.databinding.FragBedBcBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BroadCastAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.io.IOException;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class BroadCastFragment extends Fragment {

    private static final String TAG = BroadCastFragment.class.getSimpleName();
    private static final String STRING_FORMAT = "%04d";

    private FragBedBcBinding mBinding;
    private ArrayList<BcItem> curItemArrayList;
    private BroadCastAdapter adapter;
    private TinyDB tinyDB;
    private boolean isInit;
    private Core mCore;

    public static BroadCastFragment newInstance() {
        BroadCastFragment fragment = new BroadCastFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCore = MainCallService.getCore();

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
        super.onResume();

        isInit = tinyDB.getBoolean(KeyList.FIRST_BC_INIT);
        if(!isInit){
            adapter = new BroadCastAdapter(getContext(), initRoomList());
        } else {
            adapter = new BroadCastAdapter(getContext(), NurseCallUtils.getBcList(tinyDB, KeyList.KEY_BC_LIST));
        }
        mBinding.bcList.setAdapter(adapter);

    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_bed_bc, container, false);

        tinyDB = new TinyDB(getContext());

        mBinding.bcList.setHasFixedSize(true);
        GridLayoutManager manager = new GridLayoutManager(getContext(), 5);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) return 5;
                return 1;
            }
        });

        mBinding.bcList.setLayoutManager(manager);

        mBinding.btnRefresh.setOnClickListener(view -> {
            getPbxGroup();
//            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedAddFragment.newInstance());
        });

        mBinding.btnPrev.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedCallFragment.newInstance());
        });

        mBinding.btnBcStart.setOnClickListener(view -> {
            Log.d(TAG, "getSelected Position: " + adapter.getSelected());
            ArrayList<BcItem> bcItems = NurseCallUtils.getBcList(tinyDB, KeyList.KEY_BC_LIST);
//            Log.d(TAG, "Name: " + bcItems.get(adapter.getSelected()).getName() + " Number: " + bcItems.get(adapter.getSelected()).getNum());
            NurseCallUtils.newOutgoingCall(getContext(), bcItems.get(adapter.getSelected()).getNum());
        });

        return mBinding.getRoot();
    }

    private ArrayList<BcItem> initRoomList() {
        ArrayList<RoomItem> curRoom = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
        ArrayList<BcItem> bcItems = new ArrayList<>();
        bcItems.add(new BcItem("0000", "전체 호출", false));
        for (int i = 0; i < curRoom.size(); i++) {
            String replaceRoom = curRoom.get(i).getNum().replaceAll("M", "");
            bcItems.add(new BcItem("", replaceRoom, false));
        }
        return bcItems;
    }

    private ArrayList<BcItem> getRoomList(ArrayList<BcItem> bcList) {
        ArrayList<RoomItem> roomList = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
        roomList.add(0, new RoomItem("0000", 0));

        ArrayList<BcItem> temp = new ArrayList<>();
        for(int i = 0; i < roomList.size(); i++) {
            Log.d(TAG, "roomList Num: " + roomList.get(i).getNum());
            String replaceRoom = roomList.get(i).getNum().replaceAll("M", "");

            replaceRoom = String.format(Locale.getDefault(), "%04d", Integer.valueOf(replaceRoom));

            for(int a = 0; a < bcList.size(); a++) {
                if(bcList.get(a).getName().contains("-")) {
                    String[] bcListName = bcList.get(a).getName().split("-");
                    Log.d(TAG, "bcListName[1]: " + bcListName[1]);
                    if(bcListName[1].equals("0000")) {
                        temp.add(0,new BcItem(bcList.get(a).getNum(), "all", true));
                        break;
                    }

                    String bcName = String.format(Locale.getDefault(), "%04d", Integer.valueOf(bcListName[1]));

                    if(replaceRoom.equals(bcName)) {
                        temp.add(new BcItem(bcList.get(a).getNum(), bcList.get(a).getName(), true));
                        break;
                    }
                }
            }
        }
        tinyDB.putBoolean(KeyList.FIRST_BC_INIT, true);
        NurseCallUtils.putBcList(tinyDB, KeyList.KEY_BC_LIST, temp);
        return temp;
    }

    private void getPbxGroup() {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_login);
        final CheckBox ckbox = dialog.findViewById(R.id.dialog_chk_ward);
        final EditText pw = dialog.findViewById(R.id.dialog_pbx_pw);
        final Button login = dialog.findViewById(R.id.dialog_pbx_login);
        final TextView ward = dialog.findViewById(R.id.dialog_ward);

        final LinearLayout ipLayout = dialog.findViewById(R.id.dialog_pbx_server);
        final LinearLayout wardLayout = dialog.findViewById(R.id.dialog_layout_ward);
        ipLayout.setVisibility(View.GONE);
        wardLayout.setVisibility(View.GONE);

        final TextView title = dialog.findViewById(R.id.dialog_edit_title);
        title.setText("방송 목록 갱신");

        final ImageButton btnIpLayout = dialog.findViewById(R.id.dialog_view_pbx);
        btnIpLayout.setOnClickListener(view -> {
            if (ipLayout.getVisibility() == View.GONE) {
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
            if (pingTest(pbxIp.getText().toString())) {
                NurseCallUtils.printShort(getContext(), "\uD83D\uDC4D IP-PBX 가 연결되어 있습니다.");
            } else {
                NurseCallUtils.printShort(getContext(), "⛔ IP-PBX 연결 상태 및 IP 주소를 확인하세요.");
            }
        });

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
                dialog.dismiss();
                TaskLogin(new goLogin(), pw.getText().toString());
            } else {
                NurseCallUtils.printShort(getContext(), "네트워크 연결 상태를 확인하세요.");
            }
        });
        dialog.show();

    }

    private void TaskLogin(goLogin asyncTask, String pw) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pw);
    }

    private class goLogin extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... strings) {

            String ip = tinyDB.getString(KeyList.PBX_IP);
            String port = tinyDB.getString(KeyList.PBX_PORT);
            String id = tinyDB.getString(KeyList.PBX_ID);
            String str_pw = strings[0];

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
//                Log.d(TAG, "Success: " + result);
                JsonElement element = new JsonParser().parse(result);
                String result1 = element.getAsJsonObject().get("status").getAsString();
                if (result1.equals("Success")) {
                    PbxData.token = element.getAsJsonObject().get("token").getAsString();
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
                        ArrayList<BcItem> newList = new ArrayList<>();

                        for (int i = 0; i < items.size(); i++) {
                            Log.d(TAG, "Number: " + items.get(i).number + " Name: " + items.get(i).name);
                            newList.add(new BcItem(items.get(i).number, items.get(i).name, false));
                        }
                        adapter = new BroadCastAdapter(getContext(), getRoomList(newList));
                        mBinding.bcList.setAdapter(adapter);
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
                Log.w(TAG, "LogOut Result: " + str);
                if (!str.equals("Success")) {
                    NurseCallUtils.printShort(getContext(), "실패했습니다.");
                }
                progressDialog.dismiss();
            }
        }
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

}
