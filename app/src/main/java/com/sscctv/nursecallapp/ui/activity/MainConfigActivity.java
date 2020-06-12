package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

//import com.sscctv.nursecallapp.databinding.ActivityMainConfiguBinding;
import com.sscctv.nursecallapp.ui.adapter.BedItem;
import com.sscctv.nursecallapp.ui.adapter.BedListAdapter;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.adapter.RoomItem;
import com.sscctv.nursecallapp.ui.adapter.RoomListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;

public class MainConfigActivity extends AppCompatActivity implements OnSelectCall {

    private static final String TAG = "MainConfiguration";
    private static final String STRING_FORMAT = "%04d";
//    ActivityBackgroundBinding mBinding;
//    private ActivityMainConfiguBinding mBinding;
    private ArrayList<RoomItem> roomItemArrayList;
    private ArrayList<BedItem> bedItemArrayList;
    private BedListAdapter bedListAdapter;
    private RoomListAdapter roomListAdapter;

    private TinyDB tinyDB;
    private String enCheck;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mBinding = DataBindingUtil.setContentView(this, R.layout.frag_bed_call);
//        tinyDB = new TinyDB(this);
//        NurseCallUtils.sendRefreshTimer(this);
//
//        // Room list LinearLayout properties apply
//        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
//        mBinding.roomList.setHasFixedSize(true);
//        mBinding.roomList.setLayoutManager(manager);
//
//
//        /*  Room list SharedPreferences
//            Key = room_list
//            Register OnSelectCall listener. */
//
//        roomItemArrayList = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST);
//        roomListAdapter = new RoomListAdapter(this, roomItemArrayList, this);
//        mBinding.roomList.setAdapter(roomListAdapter);
//
//        Map<String, ?> allEntries = tinyDB.getAll();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            final String key = entry.getKey();
//            final Object value = entry.getValue();
//            Log.d("map values", key + ": " + value);
//        }
//
//
//        Log.d(TAG, "Room: " + roomItemArrayList.size());
//        /*  Add Room list Click listener
//            Create registrable dialog.
//
//            Spec.
//            1) Registration from 1 to 9999 is possible.
//            2) ' - ' <- Input range available on input.
//            3) Range input is available in 50 units.
//            4) Numbers are registered as 4 digits. ( ex: 0001, 0049 )
//            5) Create Array List and input to DB.    */
//
//        mBinding.addRoomList.setOnClickListener(view -> {
//            Dialog dialog = new Dialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.dialog_add_room);
//
//            final Button btnAdd = dialog.findViewById(R.id.btn_add_apply);
//            final EditText inputNumber = dialog.findViewById(R.id.add_roomNum);
//
//            inputNumber.setHint(this.getResources().getString(R.string.enter_room_number));
//
//            btnAdd.setOnClickListener(view1 -> {
//                String roomNumber = inputNumber.getText().toString();
//
//                if (roomNumber.equals("")) {
//                    NurseCallUtils.printShort(this, "Warning: No value entered.");
//                    return;
//                }
//
//                if (roomNumber.contains("-")) {
//                    int index = roomNumber.indexOf("-");
//                    String startIndex = roomNumber.substring(0, index);
//                    String endIndex = roomNumber.substring(index + 1);
//
//                    if (startIndex.equals("") || endIndex.equals("")) {
//                        NurseCallUtils.printShort(this, "Warning: No value entered.");
//                        return;
//                    }
//
//                    if (startIndex.equals("0") || endIndex.equals("0")) {
//                        NurseCallUtils.printShort(this, "Warning: '0' can't be entered");
//                        return;
//                    }
//
//                    for (int i = 0; i < roomItemArrayList.size(); i++) {
//                        if (startIndex.equals(roomItemArrayList.get(i).getNum()) || endIndex.equals((roomItemArrayList.get(i).getNum()))) {
//                            NurseCallUtils.printShort(this, "Warning: Same Room Number.");
//                            return;
//                        }
//                    }
//
//                    int numStart = Integer.valueOf(startIndex);
//                    int numEnd = Integer.valueOf(endIndex);
//
//                    int lenStart = (int) Math.log10(numStart) + 1;
//                    int lenEnd = (int) Math.log10(numEnd) + 1;
//
//                    if (numEnd < numStart) {
//                        NurseCallUtils.printShort(this, "Warning: Invalid number range");
//                        return;
//                    }
//
//                    if ((numEnd - numStart) > 49) {
//                        NurseCallUtils.printShort(this, "Warning: Can't create more than 50");
//                        return;
//                    }
//
//                    if (lenStart > 4 || lenEnd > 4) {
//                        NurseCallUtils.printShort(this, "Warning: Room number can be up to 4 digits");
//                        return;
//                    }
//
//                    int cal = numEnd - numStart;
//                    String str;
//                    boolean check = true;
//
//                    for (int j = 0; j < cal + 1; j++) {
//                        str = String.format(STRING_FORMAT, numStart + j);
//
//                        for (int a = 0; a < NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST).size(); a++) {
//                            String val2 = NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST).get(a).getNum();
//                            if (str.equals(val2)) {
//                                check = false;
//                                break;
//                            } else {
//                                check = true;
//                            }
//                        }
//
//                        if (check) {
//                            NurseCallUtils.putBedList(tinyDB, str, null);
//                            RoomItem roomItem = new RoomItem(str);
//                            roomItemArrayList.add(roomItem);
//                        }
//                    }
//                } else {
//
//                    int intNum = Integer.valueOf(roomNumber);
//                    int lenIntNum = (int) Math.log10(intNum) + 1;
//
//                    if (intNum == 0) {
//                        NurseCallUtils.printShort(this, "Warning: '0' can't be entered");
//                        return;
//                    }
//
//                    if (lenIntNum > 4) {
//                        NurseCallUtils.printShort(this, "Warning: Room number can be up to 4 digits");
//                        return;
//                    }
//
//                    String str = String.format(STRING_FORMAT, Integer.valueOf(roomNumber));
//                    for (int i = 0; i < roomItemArrayList.size(); i++) {
//                        if (str.equals(roomItemArrayList.get(i).getNum())) {
//                            NurseCallUtils.printShort(this, "Warning: Same Room Number.");
//                            return;
//                        }
//                    }
//
//                    NurseCallUtils.putBedList(tinyDB, str, null);
//                    RoomItem roomItem = new RoomItem(str);
//                    roomItemArrayList.add(roomItem);
//                }
//                Collections.sort(roomItemArrayList, (roomItem1, t1) -> roomItem1.getNum().compareTo(t1.getNum()));
//                NurseCallUtils.putRoomList(tinyDB, KeyList.KEY_ROOM_LIST, roomItemArrayList);
//                roomListAdapter.notifyDataSetChanged();
//                dialog.dismiss();
//
//            });
//            dialog.show();
//        });
//
//
//        GridLayoutManager gridManager = new GridLayoutManager(getApplicationContext(), 3);
//        mBinding.bedList.setHasFixedSize(true);
//        mBinding.bedList.setLayoutManager(gridManager);
//
//
//        mBinding.configBed.setOnClickListener(view -> {
//
//            String room = getStringDB(tinyDB, KeyList.KEY_SELECT);
//            if (compareRoomList(room).equals("false")) {
//                NurseCallUtils.printShort(this, "Warning: No Room Information!");
//            }
//
//            NurseCallUtils.startIntent(this, MainBedConfigActivity.class);
//
//        });
//
//        mBinding.editBedList.setOnClickListener(view -> {
//            bedListAdapter.clear();
//
//        });
//
//        mBinding.changeBed.setOnClickListener(view -> {
//            mBinding.bedList.setVisibility(View.GONE);
//
//            mBinding.bedCanvas.setVisibility(View.VISIBLE);
//
//            ImageView btn = new ImageButton(this);
//            btn.setLayoutParams(new GridLayoutManager.LayoutParams(150, 150));
//            btn.setBackgroundResource(R.drawable.ic_airline_seat_flat_black_24dp);
//
//            mBinding.bedCanvas.addView(btn);
//            btn.setTag("BED!");
//            btn.setOnLongClickListener(new LongClickListener());
//
//            mBinding.bedCanvas.setOnDragListener((view1, dragEvent) -> {
//                int id = view1.getId();
//                int action = dragEvent.getAction();
//                float num = 2;
//
//                switch (action) {
//                    case DragEvent.ACTION_DROP:
//                        if (id == R.id.bed_canvas) {
//                            float x = dragEvent.getX() - (btn.getWidth() / num);
//                            float y = dragEvent.getY() - (btn.getHeight() / num);
//                            btn.setX(x);
//                            btn.setY(y);
//
//                        }
//                        break;
//                }
//                return true;
//
//            });
//        });
//
//        mBinding.btnEntranceAdd.setOnClickListener(view -> {
//            final int[] rotation = {0};
//            Dialog dialog = new Dialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.dialog_entrance);
//
//            final RadioGroup group = dialog.findViewById(R.id.group_entrance);
//            final RadioButton enable = dialog.findViewById(R.id.entrance_enalbe);
//            final RadioButton disable = dialog.findViewById(R.id.entrance_disable);
//            final ConstraintLayout layout = dialog.findViewById(R.id.layout_arrow);
//            final LinearLayout top = dialog.findViewById(R.id.entrance_top);
//            final LinearLayout left = dialog.findViewById(R.id.entrance_left);
//            final LinearLayout right = dialog.findViewById(R.id.entrance_right);
//            final LinearLayout bottom = dialog.findViewById(R.id.entrance_bottom);
//            final Button apply = dialog.findViewById(R.id.btn_entrance_apply);
//
//
//            enCheck = tinyDB.getString("use_entrance");
//            if (enCheck.equals("true")) {
//                layout.setVisibility(View.VISIBLE);
//                enable.setChecked(true);
//
//                int val = tinyDB.getInt("rotation_entrance");
//                switch (val) {
//                    case 0:
//                        right.setSelected(true);
//                        break;
//                    case 90:
//                        bottom.setSelected(true);
//                        break;
//                    case 180:
//                        left.setSelected(true);
//                        break;
//                    case 270:
//                        top.setSelected(true);
//                        break;
//                    case -1:
//                        right.setSelected(false);
//                        bottom.setSelected(false);
//                        left.setSelected(false);
//                        top.setSelected(false);
//                        break;
//                }
//            } else {
//                disable.setChecked(true);
//                layout.setVisibility(View.GONE);
//            }
//
//            group.setOnCheckedChangeListener((radioGroup, i) -> {
//                Log.d(TAG, "setOnCheckedChangeListener: " + enCheck);
//                if (i == R.id.entrance_enalbe) {
//                    layout.setVisibility(View.VISIBLE);
//                } else {
//                    layout.setVisibility(View.GONE);
//                }
//            });
//
//
//            top.setOnClickListener(view1 -> {
//                right.setSelected(false);
//                bottom.setSelected(false);
//                left.setSelected(false);
//                top.setSelected(true);
//                rotation[0] = 270;
//            });
//
//            left.setOnClickListener(view1 -> {
//                right.setSelected(false);
//                bottom.setSelected(false);
//                left.setSelected(true);
//                top.setSelected(false);
//                rotation[0] = 180;
//            });
//
//            right.setOnClickListener(view1 -> {
//                right.setSelected(true);
//                bottom.setSelected(false);
//                left.setSelected(false);
//                top.setSelected(false);
//                rotation[0] = 0;
//            });
//
//            bottom.setOnClickListener(view1 -> {
//                right.setSelected(false);
//                bottom.setSelected(true);
//                left.setSelected(false);
//                top.setSelected(false);
//                rotation[0] = 90;
//            });
//
//            apply.setOnClickListener(view1 -> {
//                setEntrance(rotation[0]);
//                if (enable.isChecked()) {
//                    mBinding.viewEntrance.setVisibility(View.VISIBLE);
//                    tinyDB.putString("use_entrance", "true");
//                    tinyDB.putInt("rotation_entrance", rotation[0]);
//                } else {
//                    mBinding.viewEntrance.setVisibility(View.INVISIBLE);
//                    tinyDB.putString("use_entrance", "false");
//                    tinyDB.putInt("rotation_entrance", -1);
//                }
//                dialog.dismiss();
//            });
//
//            dialog.show();
//
//        });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        reLoad();
//
//
//    }

//    private void setEntrance(int rotation) {
//        mBinding.viewEntrance.setRotation(rotation);
//
//        switch (rotation) {
//            case 0:
//                mBinding.viewEntrance.setX(mBinding.bedCanvas.getWidth() - mBinding.viewEntrance.getWidth());
//                mBinding.viewEntrance.setY(mBinding.bedCanvas.getHeight() / 2 - mBinding.viewEntrance.getHeight() / 2);
//                break;
//            case 90:
//                mBinding.viewEntrance.setX(mBinding.bedCanvas.getWidth() / 2 - mBinding.viewEntrance.getHeight() / 2);
//                mBinding.viewEntrance.setY(mBinding.bedCanvas.getHeight() - mBinding.viewEntrance.getHeight());
//                break;
//            case 180:
//
//                mBinding.viewEntrance.setX(0);
//                mBinding.viewEntrance.setY(mBinding.bedCanvas.getHeight() / 2 - mBinding.viewEntrance.getHeight() / 2);
//                break;
//            case 270:
//                mBinding.viewEntrance.setX(mBinding.bedCanvas.getWidth() / 2 - mBinding.viewEntrance.getWidth() / 2);
//                mBinding.viewEntrance.setY(0);
//                break;
//
//        }
//    }
//
//
//    public void reLoad() {
//        bedItemArrayList = NurseCallUtils.getBedList(tinyDB, getStringDB(tinyDB, KeyList.KEY_SELECT));
//        bedListAdapter = new BedListAdapter(this, bedItemArrayList);
//        mBinding.bedList.setAdapter(bedListAdapter);
//
////        LayoutBed bed = new LayoutBed(this);
////        mBinding.bedCanvas.addView(bed);
//
//
//        Log.d(TAG, "reLoad: " + bedItemArrayList.size());
//    }


    public static String getStringDB(TinyDB tinyDB, String key) {
        return tinyDB.getString(key);
    }

    private String compareRoomList(String str) {
        for (int i = 0; i < NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST).size(); i++) {
            if (str.equals(NurseCallUtils.getRoomList(tinyDB, KeyList.KEY_ROOM_LIST).get(i).getNum())) {
                return str;
            }
        }
        return "false";
    }

    @Override
    public void roomSelect() {
//        reLoad();
    }

    @Override
    public void refresh() {
        onResume();
    }

    @Override
    public void starSelect(int position, boolean chk) {

    }

    @Override
    public void roomAllClear() {
        tinyDB.clear();
        onResume();
    }


}
