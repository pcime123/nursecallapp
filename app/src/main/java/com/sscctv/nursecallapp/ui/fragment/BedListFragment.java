package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.data.ExtListItem;
import com.sscctv.nursecallapp.databinding.FragBedListBinding;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedExtAddAdapter;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedExtListAdapter;
import com.sscctv.nursecallapp.ui.utils.ExtItemCompare;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class BedListFragment extends Fragment {

    private static final String TAG = BedListFragment.class.getSimpleName();
    private static final String STRING_FORMAT = "%04d";

    private ArrayList<ExtItem> curItemArrayList;
    private BedExtListAdapter pbxExtensionListAdapter;
    private TinyDB tinyDB;
    private RecyclerView bedConfigList;
    private Button btnSortNum, btnSortName, btnSortWard, btnSortRoom, btnSortBed;
    private CheckBox listChkBox;
    private boolean chkNum, chkName, chkWard, chkRoom, chkBed;

    public static BedListFragment newInstance() {
        BedListFragment fragment = new BedListFragment();
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

        reLoad();
        init(0);
        super.onResume();

    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        pbxExtensionListAdapter.setAllSelected(false);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragBedListBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_bed_list, container, false);

        tinyDB = new TinyDB(getContext());

        bedConfigList = layout.bedConfigList;

        bedConfigList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        bedConfigList.setLayoutManager(layoutManager);


        layout.btnAdd.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedAddFragment.newInstance());

        });

        layout.btnPrev.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedCallFragment.newInstance());
        });



        listChkBox = layout.listCheckbox;
        listChkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            pbxExtensionListAdapter = new BedExtListAdapter(getContext(), curItemArrayList);
            if (listChkBox.isChecked()) {
                pbxExtensionListAdapter.setAllSelected(true);
            } else {
                pbxExtensionListAdapter.setAllSelected(false);
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));
        });


        layout.btnDel.setOnClickListener(view -> {
            int size = curItemArrayList.size();
            boolean stat = true;
            for (int i = 0; i < size; i++) {
                ExtItem item = curItemArrayList.get(i);
                Log.d(TAG, "Num: " + item.getNum() + " isSelect: " + item.isSelected());
                if (!item.isSelected()) {
                    stat = false;
                } else {
                    stat = true;
                    break;
                }

            }

            if(stat) {
                realDelete();
            } else {
                NurseCallUtils.printShort(getContext(), "선택된 장비가 없습니다.");

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

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkNum = false;
                btnSortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return ward.compareTo(compareWard);
                });
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));
            init(1);

        });

        btnSortName.setOnClickListener(view -> {

            if (!chkName) {
                chkName = true;
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> t1.getName().compareTo(extItem.getName()));
            } else {
                chkName = false;
                btnSortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> extItem.getName().compareTo(t1.getName()));
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));

            init(2);
        });


        btnSortWard.setOnClickListener(view -> {

            if (!chkWard) {
                chkWard = true;
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });
            } else {
                chkWard = false;
                btnSortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));

            init(3);
        });

        btnSortRoom.setOnClickListener(view -> {

            if (!chkRoom) {
                chkRoom = true;
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkRoom = false;
                btnSortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {

                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(), STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));

            init(4);
        });

        btnSortBed.setOnClickListener(view -> {

            if (!chkBed) {
                chkBed = true;
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkBed = false;
                btnSortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(Locale.getDefault(),STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));

            init(5);
        });


        return layout.getRoot();
    }


    private void reLoad() {
        curItemArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);
        Collections.sort(curItemArrayList, new ExtItemCompare());

        pbxExtensionListAdapter = new BedExtListAdapter(getContext(), curItemArrayList);
        bedConfigList.setAdapter(new BedExtListAdapter(getContext(), curItemArrayList));

        transList();
    }

    private void transList() {
        String idModel, idWard, idRoom, idBed;
        ArrayList<ExtListItem> listItems = new ArrayList<>();
        for (int i = 0; i < curItemArrayList.size(); i++) {
            ExtItem item = curItemArrayList.get(i);
            String[] callerId = item.getName().split("-");
            idModel = callerId[0];
            idWard = callerId[1];
            idRoom = callerId[2];
            idBed = callerId[3];

            listItems.add(new ExtListItem(item.getNum(), idModel, idWard, idRoom, idBed));
        }

        putTransList(tinyDB, KeyList.KEY_TRANS_EXTENSION, listItems);
    }

    public void putTransList(TinyDB tinyDB, String key, ArrayList<ExtListItem> extItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (extItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (ExtListItem extItem : extItems) {
                objString.add(gson.toJson(extItem));
            }
            tinyDB.putListString(key, objString);
        }
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

    private void realDelete() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_room);
        final Button yes = dialog.findViewById(R.id.dialog_real_yes);
        final Button no = dialog.findViewById(R.id.dialog_real_no);

        yes.setOnClickListener(view1 -> {
            tinyDB.remove(KeyList.KEY_GET_EXTENSION);

            int size = curItemArrayList.size();

            for (int i = 0; i < size; i++) {
                ExtItem item = curItemArrayList.get(i);

                if (item.isSelected()) {
                    curItemArrayList.remove(i);
                    size--;
                    i--;

                }
            }

            pbxExtensionListAdapter.setAllSelected(false);
            listChkBox.setChecked(false);
            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_CUR_EXTENSION, curItemArrayList);
            transList();
            bedConfigList.setAdapter(new BedExtAddAdapter(curItemArrayList));

            dialog.dismiss();
        });

        no.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();

    }

}
