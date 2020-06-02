package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityBedListBinding;
import com.sscctv.nursecallapp.ui.adapter.ExtItem;
import com.sscctv.nursecallapp.ui.adapter.ExtListItem;
import com.sscctv.nursecallapp.ui.adapter.PbxExtensionAddAdapter;
import com.sscctv.nursecallapp.ui.adapter.PbxExtensionListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Collections;

public class BedListConfigActivity extends AppCompatActivity {
    private static final String TAG = "BedListConfigActivity";
    private static final String STRING_FORMAT = "%04d";

    private ActivityBedListBinding mBinding;
    private ArrayList<ExtItem> curItemArrayList;
    private PbxExtensionListAdapter pbxExtensionListAdapter;
    private TinyDB tinyDB;

    private boolean chkNum, chkName, chkWard, chkRoom, chkBed;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_bed_list);
        tinyDB = new TinyDB(this);

        mBinding.bedConfigList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.bedConfigList.setLayoutManager(layoutManager);


        mBinding.btnAdd.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, BedAddConfigActivity.class);
        });

        mBinding.btnDel.setOnClickListener(view -> {
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
            mBinding.listCheckbox.setChecked(false);
            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_CUR_EXTENSION, curItemArrayList);
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));

        });

        mBinding.listCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            pbxExtensionListAdapter = new PbxExtensionListAdapter(curItemArrayList);
            if (mBinding.listCheckbox.isChecked()) {
                pbxExtensionListAdapter.setAllSelected(true);
                mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));
            } else {
                pbxExtensionListAdapter.setAllSelected(false);
                mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));
            }
        });
        mBinding.sortNum.setOnClickListener(view -> {

            if (!chkNum) {
                chkNum = true;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkNum = false;
                mBinding.sortNum.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(extItem.getNum()));
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(t1.getNum()));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));
            init(1);

        });

        mBinding.sortName.setOnClickListener(view -> {

            if (!chkName) {
                chkName = true;
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> t1.getName().compareTo(extItem.getName()));
            } else {
                chkName = false;
                mBinding.sortName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> extItem.getName().compareTo(t1.getName()));
            }
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));

            init(2);
        });


        mBinding.sortWard.setOnClickListener(view -> {

            if (!chkWard) {
                chkWard = true;
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });
            } else {
                chkWard = false;
                mBinding.sortWard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[1].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[1].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));

            init(3);
        });

        mBinding.sortRoom.setOnClickListener(view -> {

            if (!chkRoom) {
                chkRoom = true;
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkRoom = false;
                mBinding.sortRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {

                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[2].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[2].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));

            init(4);
        });

        mBinding.sortBed.setOnClickListener(view -> {

            if (!chkBed) {
                chkBed = true;
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return compareWard.compareTo(ward);
                });

            } else {
                chkBed = false;
                mBinding.sortBed.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);

                Collections.sort(curItemArrayList, (extItem, t1) -> {
                    String[] callerId = extItem.getName().split("-");
                    String ward = String.format(STRING_FORMAT, Integer.valueOf(callerId[3].replaceAll("[^0-9]", "")));
                    String[] compareId = t1.getName().split("-");
                    String compareWard = String.format(STRING_FORMAT, Integer.valueOf(compareId[3].replaceAll("[^0-9]", "")));
                    return ward.compareTo(compareWard);
                });
            }
            mBinding.bedConfigList.setAdapter(new PbxExtensionAddAdapter(curItemArrayList));

            init(5);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        reLoad();
        init(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pbxExtensionListAdapter.setAllSelected(false);
    }

    private void reLoad() {
        curItemArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);
        pbxExtensionListAdapter = new PbxExtensionListAdapter(curItemArrayList);
        mBinding.bedConfigList.setAdapter(new PbxExtensionListAdapter(curItemArrayList));

        transList();
    }

    private void transList() {
        String idModel, idWard, idRoom, idBed;
        ArrayList<ExtListItem> listItems  = new ArrayList<>();
        for (int i = 0; i < curItemArrayList.size(); i++) {
            ExtItem item = curItemArrayList.get(i);
            String[] callerId = item.getName().split("-");
            idModel = callerId[0];
            idWard = callerId[1];
            idRoom = callerId[2];
            idBed = callerId[3];

            listItems.add(new ExtListItem(item.getNum(), idModel, idWard,idRoom, idBed));
        }

        putTransList(tinyDB, KeyList.KEY_TRANS_EXTENSION, listItems);
    }

    public void putTransList(TinyDB tinyDB, String key, ArrayList<ExtListItem> extItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if(extItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for(ExtListItem extItem: extItems) {
                objString.add(gson.toJson(extItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    private void manualAddList () {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_error);
        final TextView error = dialog.findViewById(R.id.dialog_txt_pbx_error);
        final Button close = dialog.findViewById(R.id.dialog_pbx_close);
        close.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
        dialog.show();
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
