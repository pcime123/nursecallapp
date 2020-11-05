package com.sscctv.nursecallapp.ui.setup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupTypeBinding;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

public class SetupStepType extends AppCompatActivity {

//    private static final String TAG = "SetupStepType";
    private ActivitySetupTypeBinding mBinding;
    private TinyDB tinyDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_type);
        tinyDB = new TinyDB(this);
        mBinding.btnNext.setOnClickListener(view -> {
            Intent intent;
            if (mBinding.firstOn.isChecked()) {
                intent = new Intent(this, SetupStepNetwork.class);
                tinyDB.putBoolean(KeyList.DEVICE_USED, false);
            } else {
                intent = new Intent(this, SetupStepDevice.class);
                tinyDB.putBoolean(KeyList.DEVICE_USED, true);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
