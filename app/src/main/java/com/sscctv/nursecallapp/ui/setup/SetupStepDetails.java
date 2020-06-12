package com.sscctv.nursecallapp.ui.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupDetaileBinding;
import com.sscctv.nursecallapp.ui.activity.LauncherActivity;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

public class SetupStepDetails extends AppCompatActivity {
    private static final String TAG = "SetupStepDevice";

    private ActivitySetupDetaileBinding mBinding;
    private TinyDB tinyDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_detaile);
        tinyDB = new TinyDB(this);

        mBinding.btnPrev.setOnClickListener(view -> finish());

        mBinding.btnFinish.setOnClickListener(view -> {

            tinyDB.putBoolean(KeyList.FIRST_KEY, true);

            setDetails();
            Intent intent = new Intent(this, LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });


        mBinding.inputUseWard.setText("10");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDeviceType();
    }

    @SuppressLint("SetTextI18n")
    private void getDeviceType() {

        int mode = tinyDB.getInt(KeyList.DEVICE_TYPE);
        switch (mode) {
            case 0:
                mBinding.detailTitle.setText(getResources().getString(R.string.detail_nurse) + " " + getResources().getString(R.string.detail_setup));
                mBinding.resultTitle.setText(getResources().getString(R.string.detail_nurse));
                mBinding.resultTitle1.append(getResources().getString(R.string.detail_content));
                mBinding.inputLocation.setText(getResources().getString(R.string.detail_nurse));
                break;
            case 1:
                mBinding.resultTitle.setText(getResources().getString(R.string.detail_security) + " " + getResources().getString(R.string.detail_setup));
                mBinding.resultTitle1.append(R.string.detail_security + " " + R.string.detail_content);

                break;
            case 2:
                mBinding.resultTitle.setText(R.string.detail_path + " " + R.string.detail_setup);
                mBinding.resultTitle1.append(R.string.detail_path + " " + R.string.detail_content);

                break;
            case 3:
                mBinding.resultTitle.setText(R.string.detail_etc + " " + R.string.detail_setup);
                mBinding.resultTitle1.append(R.string.detail_etc + " " + R.string.detail_content);

                break;
        }
    }

    private void setDetails() {
        if(!mBinding.inputUseWard.getText().toString().isEmpty()){
            String building = mBinding.inputUseWard.getText().toString();
            tinyDB.putString(KeyList.DEVICE_WARD, building);
        } else {
            tinyDB.putString(KeyList.DEVICE_WARD, getResources().getString(R.string.pref_none));
        }

        if(!mBinding.inputLocation.getText().toString().isEmpty()){
            String location = mBinding.inputLocation.getText().toString();
            tinyDB.putString(KeyList.DEVICE_LOCATION, location);
        } else {
            tinyDB.putString(KeyList.DEVICE_LOCATION, getResources().getString(R.string.pref_none));
        }

        if(!mBinding.inputEtc.getText().toString().isEmpty()){
            String etc = mBinding.inputEtc.getText().toString();
            tinyDB.putString(KeyList.DEVICE_ETC, etc);
        } else {
            tinyDB.putString(KeyList.DEVICE_ETC, getResources().getString(R.string.pref_none));
        }

    }
}
