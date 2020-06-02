package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityMainbedConfigBinding;
import com.sscctv.nursecallapp.ui.adapter.BedConfigListAdapter;
import com.sscctv.nursecallapp.ui.adapter.BedItem;
import com.sscctv.nursecallapp.ui.adapter.SpinnerAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainBedConfigActivity extends AppCompatActivity {
    private static final String TAG = "MainBedConfigActivity";
    private static final String STRING_FORMAT = "%04d";
    private static final int BED_NUMBER = 12;

    private ActivityMainbedConfigBinding mBinding;
    private ArrayList<BedItem> bedItemArrayList;
    private BedConfigListAdapter bedConfigListAdapter;
    public List<String> bedNumberList;
    private TinyDB tinyDB;
    private int size;
    private Calendar calendar;
    private String mode;
    private String room;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mainbed_config);
        tinyDB = new TinyDB(this);

        bedNumberList = new ArrayList<>();
        for (int i = 1; i < BED_NUMBER + 1; i++) {
            bedNumberList.add(String.valueOf(i));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mBinding.bedConfigList.setHasFixedSize(true);
        mBinding.bedConfigList.setLayoutManager(linearLayoutManager);

        bedNumberList = new ArrayList<>();

        for (int i = 1; i < BED_NUMBER + 1; i++) {
            bedNumberList.add(String.valueOf(i));
        }

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, bedNumberList);
        mBinding.spinnerBed.setAdapter(spinnerAdapter);

//        calendar = Calendar.getInstance();

//        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, i, i1, i2) -> {
//            Log.d(TAG, "i: " + i + " i1: " + i1 + " i2: " + i2);
//            calendar.set(Calendar.YEAR, i);
//            calendar.set(Calendar.MONTH, i1);
//            calendar.set(Calendar.DAY_OF_MONTH, i2);
//            if (mode != null) {
//                updateDate(mode);
//
//            }
//        };
//
//        mBinding.bedConfigEnter.setOnClickListener(view -> {
//            mode = "enter";
//            new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
//
//        });
//
//        mBinding.bedConfigLeave.setOnClickListener(view -> {
//            mode = "leave";
//            new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
//        });
//
        mBinding.bedConfigAdd.setOnClickListener(view -> {
            int getNumber = Integer.valueOf(mBinding.spinnerBed.getSelectedItem().toString());

            int size = NurseCallUtils.getBedList(tinyDB, room).size();

            if (getNumber > size) {
                int cal = getNumber - size;
                for (int i = 0; i < cal; i++) {
                    bedItemArrayList.add(new BedItem("",String.valueOf((size + 1) + i), "None", "None"));
                }
            } else {
                int cal = size - getNumber;
                if (cal >= getNumber) {
                    bedItemArrayList.subList(getNumber, cal + 1).clear();
                }
            }

            NurseCallUtils.putBedList(tinyDB, room, bedItemArrayList);
            bedConfigListAdapter.notifyDataSetChanged();
            mBinding.bedConfigList.scrollToPosition(bedItemArrayList.size() - 1);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        reLoad();
    }

    public void reLoad() {
        room = MainConfigActivity.getStringDB(tinyDB, KeyList.KEY_SELECT);

        bedItemArrayList = NurseCallUtils.getBedList(tinyDB, room);
        bedConfigListAdapter = new BedConfigListAdapter(this, bedItemArrayList);
        mBinding.bedConfigList.setAdapter(bedConfigListAdapter);

        size = bedItemArrayList.size();
        mBinding.spinnerBed.setSelection(size-1);
    }

//    private void updateDate(String str) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
//
//        if (str.equals("enter")) {
//            mBinding.bedConfigEnter.setText(dateFormat.format(calendar.getTime()));
//        } else if (str.equals("leave")) {
//            mBinding.bedConfigLeave.setText(dateFormat.format(calendar.getTime()));
//        }
//    }


}
