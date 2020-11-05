package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BcItem;
import com.sscctv.nursecallapp.data.BedItem;
import com.sscctv.nursecallapp.data.ExtListItem;
import com.sscctv.nursecallapp.data.RoomItem;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedCallAdapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedListAdapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.BroadCastAdapter;
import com.sscctv.nursecallapp.ui.utils.ExtListItemCompare;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class BedCallFragment extends Fragment implements IOnBackPressed, OnSelectCall {

    private static final String TAG = "BedCallFragment";
    private BedCallAdapter roomListAdapter;
    private BedListAdapter bedListAdapter;
    private TinyDB tinyDB;
    private RecyclerView roomList, bedList;
    private ArrayList<ExtListItem> getArrayList;
    private String roomSize, bedSize;
    private CardView btnAllList;
    private LinearLayout btnLayout;
    private ArrayList<BcItem> newList;
    int step = 0;
    String prev, next;

    public static BedCallFragment newInstance() {
        BedCallFragment fragment = new BedCallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Log.v(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Log.v(TAG, "onDetach()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getList();
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.frag_bed_call, container, false);
        tinyDB = new TinyDB(getContext());

        roomList = view.findViewById(R.id.call_room_list);
        roomList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        roomList.setLayoutManager(manager);
        tinyDB.putInt(KeyList.VIEW_HEIGHT, 604);

        btnAllList = view.findViewById(R.id.btnAllList);
        btnAllList.setOnClickListener(view1 -> {
            roomListAdapter.clearSelectedItem();
            getAllBedList();
        });

        btnLayout = view.findViewById(R.id.layout_allList);
        bedList = view.findViewById(R.id.call_bed_list);
        bedList.setHasFixedSize(true);

        Button btnTest = view.findViewById(R.id.btn_call_add);
        btnTest.setOnClickListener(view1 -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedListFragment.newInstance());
        });

        Button btnBroadcast = view.findViewById(R.id.btn_broadcast);
        btnBroadcast.setOnClickListener(view1 -> {
//            showBroadCast();
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BroadCastFragment.newInstance());
        });
        return view;

    }

    private ArrayList<ExtListItem> getTransList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ExtListItem> extList = new ArrayList<>();

        for (String jObjString : objStrings) {
            ExtListItem extItem = gson.fromJson(jObjString, ExtListItem.class);
            extList.add(extItem);
        }
        return extList;
    }

    private void getList() {
        getArrayList = getTransList(tinyDB, KeyList.KEY_TRANS_EXTENSION);
        Collections.sort(getArrayList, new ExtListItemCompare());

        getRoomList();
        getAllBedList();
//        getAllBtnList();
    }

    private void getRoomList() {
        ArrayList<RoomItem> roomItems = new ArrayList<>();
        ArrayList<String> room = new ArrayList<>();
        for (int i = 0; i < getArrayList.size(); i++) {
            ExtListItem item = getArrayList.get(i);
            if (!room.contains(item.getRoom()) && item.getRoom().contains("M")) {
                roomItems.add(new RoomItem(item.getRoom(), 0));
                room.add(item.getRoom());
//                Log.d(TAG, "Room Get: " + item.getRoom());
            }
        }

        Collections.sort(roomItems, (extItem, t1) -> {
            String roomId = "";
            String compareRoomId = "";
            if (extItem.getNum().contains("M")) {
                roomId = extItem.getNum().replaceAll("M", "");
                roomId = String.format(Locale.getDefault(), "%04d", Integer.valueOf(roomId));
            }

            if (t1.getNum().contains("M")) {
                compareRoomId = t1.getNum().replaceAll("M", "");
                compareRoomId = String.format(Locale.getDefault(), "%04d", Integer.valueOf(compareRoomId));
            }
            return roomId.compareTo(compareRoomId);
        });

        ArrayList<RoomItem> tempList = new ArrayList<>();
        for (int i = 0; i < roomItems.size(); i++) {
            RoomItem item = roomItems.get(i);
            tempList.add(new RoomItem(item.getNum(), bedBackColor(i)));
//            Log.d(TAG, "tempList Get: " + item.getNum());

        }

        if (NurseCallUtils.getBcList(tinyDB, KeyList.KEY_BC_LIST).size() == 0) {
            ArrayList<BcItem> bcItems = new ArrayList<>();
            for (int i = 0; i < roomItems.size(); i++) {
                bcItems.add(new BcItem(roomItems.get(i).getNum(), "", false));
            }

            NurseCallUtils.putBcList(tinyDB, KeyList.KEY_BC_LIST, bcItems);
        }

        NurseCallUtils.putRoomList(tinyDB, KeyList.KEY_ROOM_LIST, roomItems);

        roomListAdapter = new BedCallAdapter(getContext(), tempList, this);
        roomList.setAdapter(roomListAdapter);
    }


    private int bedBackColor(int pos) {

        switch (pos) {
            case 1:
            case 6:
            case 11:
            case 16:
            case 21:
            case 26:
            case 31:
            case 36:
                return tinyDB.getInt(KeyList.BED_COLOR_2);
            case 2:
            case 7:
            case 12:
            case 17:
            case 22:
            case 27:
            case 32:
            case 37:
                return tinyDB.getInt(KeyList.BED_COLOR_3);

            case 3:
            case 8:
            case 13:
            case 18:
            case 23:
            case 28:
            case 33:
            case 38:
                return tinyDB.getInt(KeyList.BED_COLOR_4);

            case 4:
            case 9:
            case 14:
            case 19:
            case 24:
            case 29:
            case 34:
            case 39:
                return tinyDB.getInt(KeyList.BED_COLOR_5);

            default:
                return tinyDB.getInt(KeyList.BED_COLOR_1);
        }

    }

    private void getBedList(String room, int pos) {
        ArrayList<BedItem> bedItems = new ArrayList<>();
        for (int i = 0; i < getArrayList.size(); i++) {
            ExtListItem item = getArrayList.get(i);
            if (room.equals(item.getRoom())) {
                bedItems.add(new BedItem(item.getRoom().substring(1), item.getBed(), item.getNum(), item.getName(), bedBackColor(pos)));
            }
        }
        NurseCallUtils.putBedList(tinyDB, KeyList.KEY_BED_LIST, bedItems);

        bedListAdapter = new BedListAdapter(getContext(), bedItems);
        bedList.setAdapter(bedListAdapter);

        int selectView;
        if (tinyDB.getString(KeyList.BED_SELECT_VIEW).equals("")) {
            selectView = 3;
        } else {
            selectView = Integer.valueOf(tinyDB.getString(KeyList.BED_SELECT_VIEW));
        }


        GridLayoutManager gridManager = new GridLayoutManager(getContext(), selectView);
        bedList.setLayoutManager(gridManager);
    }

    private int bedAllBackColor(String room) {
        if (step == 0) {
            prev = room;
            step = 1;
            return tinyDB.getInt(KeyList.BED_COLOR_1);
        } else if (step == 1) {
            next = room;

            if (prev.equals(next)) {
                return tinyDB.getInt(KeyList.BED_COLOR_1);
            } else {
                step = 2;
                return tinyDB.getInt(KeyList.BED_COLOR_2);
            }
        } else if (step == 2) {
            prev = room;
            step = 3;
            return tinyDB.getInt(KeyList.BED_COLOR_2);
        } else if (step == 3) {
            next = room;

            if (prev.equals(next)) {
                return tinyDB.getInt(KeyList.BED_COLOR_2);
            } else {
                step = 4;
                return tinyDB.getInt(KeyList.BED_COLOR_3);
            }
        } else if (step == 4) {
            prev = room;
            step = 5;
            return tinyDB.getInt(KeyList.BED_COLOR_3);
        } else if (step == 5) {
            next = room;

            if (prev.equals(next)) {
                return tinyDB.getInt(KeyList.BED_COLOR_3);
            } else {
                step = 6;
                return tinyDB.getInt(KeyList.BED_COLOR_4);
            }
        } else if (step == 6) {
            prev = room;
            step = 7;
            return tinyDB.getInt(KeyList.BED_COLOR_4);
        } else if (step == 7) {
            next = room;

            if (prev.equals(next)) {
                return tinyDB.getInt(KeyList.BED_COLOR_4);
            } else {
                step = 8;
                return tinyDB.getInt(KeyList.BED_COLOR_5);
            }
        } else if (step == 8) {
            prev = room;
            step = 9;
            return tinyDB.getInt(KeyList.BED_COLOR_5);
        } else if (step == 9) {
            next = room;

            if (prev.equals(next)) {
                return tinyDB.getInt(KeyList.BED_COLOR_5);
            } else {
                step = 0;
                return tinyDB.getInt(KeyList.BED_COLOR_1);
            }
        }
        return 0;
    }

    private void getAllBedList() {
        ArrayList<RoomItem> roomList;
        roomList = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
        step = 0;
        ArrayList<BedItem> bedItems = new ArrayList<>();
        for (int i = 0; i < roomList.size(); i++) {
            RoomItem room = roomList.get(i);
            String strRoom = room.getNum();

            for (int a = 0; a < getArrayList.size(); a++) {
                ExtListItem item = getArrayList.get(a);

                if (strRoom.equals(item.getRoom())) {
                    bedItems.add(new BedItem(item.getRoom().substring(1), item.getBed(), item.getNum(), item.getName(), bedAllBackColor(item.getRoom())));
//                    Log.d(TAG, "getAllBedList Ged Bed: " + item.getBed() + " Get Num: " + item.getNum());
                }
            }
        }
        NurseCallUtils.putBedList(tinyDB, KeyList.KEY_ALL_BED_LIST, bedItems);

        int allView;
        if (tinyDB.getString(KeyList.BED_ALL_VIEW).equals("")) {
            allView = 3;
        } else {
            allView = Integer.valueOf(tinyDB.getString(KeyList.BED_ALL_VIEW));
        }


        GridLayoutManager gridManager = new GridLayoutManager(getContext(), allView);
        bedList.setLayoutManager(gridManager);
        bedListAdapter = new BedListAdapter(getContext(), bedItems);
        bedList.setAdapter(bedListAdapter);
        btnLayout.setBackground(getResources().getDrawable(R.drawable.back_fly_high));
    }

    private void getAllBtnList() {
        ArrayList<RoomItem> roomItems = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
        roomSize = String.valueOf(roomItems.size());

        ArrayList<BedItem> bedItems = NurseCallUtils.getBedList(tinyDB, KeyList.KEY_ALL_BED_LIST);
        bedSize = String.valueOf(bedItems.size());
    }

    private void showBroadCast() {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bc_list);

        final RecyclerView roomList = dialog.findViewById(R.id.dg_room_list);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 3);
        roomList.setLayoutManager(manager);

        ArrayList<BcItem> bcItems = NurseCallUtils.getBcList(tinyDB, KeyList.KEY_BC_LIST);

//        BroadCastAdapter roomAdapter = new BroadCastAdapter(getContext(), bcItems, this);
//        roomList.setAdapter(roomAdapter);

        final LinearLayout layoutLogin = dialog.findViewById(R.id.dg_layout_right);
        final Button btnView = dialog.findViewById(R.id.btn_refresh);
        btnView.setOnClickListener(view -> {

            if (layoutLogin.getVisibility() == View.GONE) {
                layoutLogin.setVisibility(View.VISIBLE);
                btnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_left_black_24dp, 0);
            } else {
                layoutLogin.setVisibility(View.GONE);
                btnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0);
            }

        });

        final EditText pw = dialog.findViewById(R.id.dg_bc_pw);
        final Button btnRefresh = dialog.findViewById(R.id.dg_bc_refresh);
        btnRefresh.setOnClickListener(view -> {
            TaskLogin(new goLogin(), pw.getText().toString());
            Log.d(TAG, "Time Check ");
        });

        final LinearLayout layoutAll = dialog.findViewById(R.id.dg_layout_all);
        final CardView btnAll = dialog.findViewById(R.id.dg_bc_all);
        layoutAll.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.back_loon_crest));

        btnAll.setOnClickListener(view -> {

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
                        newList = new ArrayList<>();

                        for (int i = 0; i < items.size(); i++) {
                            Log.d(TAG, "Number: " + items.get(i).number + " Name: " + items.get(i).name);
                            newList.add(new BcItem(items.get(i).number, items.get(i).name, false));
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
                Log.w(TAG, "LogOut Result: " + str);
                if (!str.equals("Success")) {
                    NurseCallUtils.printShort(getContext(), "실패했습니다.");
                }
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }

    @Override
    public void roomSelect(int position) {
        btnLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.back_loon_crest));
        getBedList(tinyDB.getString(KeyList.KEY_SELECT), position);

    }

    @Override
    public void roomAllClear() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void starSelect(String position, boolean chk) {

    }


}
