package com.sscctv.nursecallapp.ui.setup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupDeviceBinding;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

public class SetupStepDevice extends AppCompatActivity {
    private static final String TAG = "SetupStepDevice";

    private ActivitySetupDeviceBinding mBinding;
    private int mode;
    private TinyDB tinyDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_device);
        tinyDB = new TinyDB(this);

        mBinding.btnSecurity.setEnabled(false);
        mBinding.btnPath.setEnabled(false);
        mBinding.btnEtc.setEnabled(false);

        mBinding.btnPrev.setOnClickListener(view -> finish());

        mBinding.btnNext.setOnClickListener(view -> {

            getSelectMode();

            Intent intent = new Intent(this, SetupStepAccount.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void getSelectMode() {
        if(mBinding.btnNurse.isChecked()){
            mode = 0;
        } else if (mBinding.btnSecurity.isChecked()) {
            mode = 1;
        } else if (mBinding.btnPath.isChecked()) {
            mode = 2;
        } else if (mBinding.btnEtc.isChecked()) {
            mode = 3;
        }

        tinyDB.putInt(KeyList.DEVICE_TYPE, mode);
        tinyDB.putString(KeyList.DEVICE_WARD,mBinding.inputUseWard.getText().toString());
    }
}
