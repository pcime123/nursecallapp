package com.sscctv.nursecallapp.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.activity.BedListConfigActivity;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.adapter.BedItem;
import com.sscctv.nursecallapp.ui.adapter.BedListAdapter;
import com.sscctv.nursecallapp.ui.adapter.ExtListItem;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.adapter.RoomItem;
import com.sscctv.nursecallapp.ui.adapter.RoomListAdapter;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.util.ArrayList;

public class BedCallFragment extends Fragment implements IOnBackPressed, OnSelectCall {

    private static final String TAG = "BedCallFragment";
    private RoomListAdapter roomListAdapter;
    private BedListAdapter bedListAdapter;
    private TinyDB tinyDB;
    private RecyclerView roomList, bedList;
    private ArrayList<ExtListItem> getArrayList;
    private String roomSize, bedSize;
    private Button btnAllList;

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
//        Log.v(TAG, "onResume()");
        getList();
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_bed_call, container, false);
        tinyDB = new TinyDB(getContext());

        roomList = view.findViewById(R.id.call_room_list);
        roomList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        roomList.setLayoutManager(manager);
        roomList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                roomList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                tinyDB.putInt(KeyList.VIEW_HEIGHT, roomList.getHeight());

            }
        });

        btnAllList = view.findViewById(R.id.btnAllList);
        btnAllList.setOnClickListener(view1 -> {
            roomListAdapter.clearSelectedItem();
            getAllBedList(true);

        });


        bedList = view.findViewById(R.id.call_bed_list);
        bedList.setHasFixedSize(true);

        Button btnTest = view.findViewById(R.id.btn_call_add);
        btnTest.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), BedListConfigActivity.class);
            startActivity(intent);
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
        getRoomList();
        getAllBedList(true);
        getAllBtnList();
    }


    private void getRoomList() {
        ArrayList<RoomItem> roomItems = new ArrayList<>();
        ArrayList<String> room = new ArrayList<>();
        for (int i = 0; i < getArrayList.size(); i++) {
            ExtListItem item = getArrayList.get(i);
            if (!room.contains(item.getRoom()) && item.getRoom().contains("M")) {
//                Log.d(TAG, "Num: " + item.getNum() + " | Model: " + item.getName() + " | Ward: " + item.getWard() + " | Room: " + item.getRoom() + " | Bed: " + item.getBed());
                roomItems.add(new RoomItem(item.getRoom()));
                room.add(item.getRoom());
            }

        }
        NurseCallUtils.putRoomList(tinyDB, KeyList.KEY_ROOM_LIST, roomItems);
        roomListAdapter = new RoomListAdapter(getContext(), roomItems, this);
        roomList.setAdapter(roomListAdapter);
    }

    private void getBedList(String room) {
//        Log.d(TAG, "Select Room: " + room);
        ArrayList<BedItem> bedItems = new ArrayList<>();
        for (int i = 0; i < getArrayList.size(); i++) {
            ExtListItem item = getArrayList.get(i);
            if (room.equals(item.getRoom())) {
                bedItems.add(new BedItem(item.getRoom().substring(1), item.getBed(), item.getNum(), item.getName()));
//                Log.d(TAG, "Add: " + item.getRoom() + " Bed: " + item.getBed());
            }
        }
        NurseCallUtils.putBedList(tinyDB, KeyList.KEY_BED_LIST, bedItems);

        bedListAdapter = new BedListAdapter(getContext(), bedItems);
        bedList.setAdapter(bedListAdapter);

        int selectView;
        if(tinyDB.getString(KeyList.BED_SELECT_VIEW).equals("")){
            selectView = 3;
        } else {
            selectView = Integer.valueOf(tinyDB.getString(KeyList.BED_SELECT_VIEW));
        }


        GridLayoutManager gridManager = new GridLayoutManager(getContext(), selectView);
        bedList.setLayoutManager(gridManager);
    }

    private void getAllBedList(boolean update) {
        ArrayList<RoomItem> roomList;
        roomList = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);

        ArrayList<BedItem> bedItems = new ArrayList<>();
        for (int i = 0; i < roomList.size(); i++) {
            RoomItem room = roomList.get(i);
            String strRoom = room.getNum();

            for (int a = 0; a < getArrayList.size(); a++) {
                ExtListItem item = getArrayList.get(a);

                if (strRoom.equals(item.getRoom())) {
//                    Log.d(TAG, "RooM: " + strRoom + " GetRoom: " + item.getRoom() + " Bed: " + item.getBed());
                    bedItems.add(new BedItem(item.getRoom().substring(1), item.getBed(), item.getNum(), item.getName()));

                }
            }
        }
        NurseCallUtils.putBedList(tinyDB, KeyList.KEY_ALL_BED_LIST, bedItems);

        int allView;
        if(tinyDB.getString(KeyList.BED_ALL_VIEW).equals("")){
            allView = 3;
        } else {
            allView = Integer.valueOf(tinyDB.getString(KeyList.BED_ALL_VIEW));
        }

        GridLayoutManager gridManager = new GridLayoutManager(getContext(), allView);
        bedList.setLayoutManager(gridManager);
        if (update) {
            bedListAdapter = new BedListAdapter(getContext(), bedItems);
            bedList.setAdapter(bedListAdapter);
        }
//        btnAllList.setBackgroundColor(getResources().getColor(R.color.JBlue));
            btnAllList.getBackground().setColorFilter(btnAllList.getContext().getResources().getColor(R.color.JBlue), PorterDuff.Mode.SRC_ATOP);
    }

    private void getAllBtnList() {
        ArrayList<RoomItem> roomItems = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
        roomSize = String.valueOf(roomItems.size());

        ArrayList<BedItem> bedItems = NurseCallUtils.getBedList(tinyDB, KeyList.KEY_ALL_BED_LIST);
        bedSize = String.valueOf(bedItems.size());

        btnAllList.setText("전체 목록 보기\n" + roomSize + "개 호실 " + bedSize + " 병상");

    }


    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }

    @Override
    public void roomSelect() {
        btnAllList.getBackground().setColorFilter(btnAllList.getContext().getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);

//        btnAllList.setBackgroundColor(getResources().getColor(R.color.gray));
        getBedList(tinyDB.getString(KeyList.KEY_SELECT));

    }

    @Override
    public void roomAllClear() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void starSelect(int position, boolean chk) {

    }
}
