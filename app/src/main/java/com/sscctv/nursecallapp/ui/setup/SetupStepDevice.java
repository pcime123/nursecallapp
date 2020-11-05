package com.sscctv.nursecallapp.ui.setup;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

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

        mBinding.btnPrev.setOnClickListener(view -> {
            startActivity(new Intent(this, SetupStepType.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        mBinding.btnNext.setOnClickListener(view -> {

            if (mBinding.inputUseWard.getText().toString().isEmpty()) {
                Dialog dialog = new Dialog(SetupStepDevice.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_error_setup);

                final Button yes = dialog.findViewById(R.id.dialog_setup_yes);
                yes.setOnClickListener(view1 -> {
                    getSelectMode();

                    Intent intent = new Intent(this, SetupStepAccount.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                });

                final Button no = dialog.findViewById(R.id.dialog_setup_no);
                no.setOnClickListener(view1 -> {
                    dialog.dismiss();
                });

                dialog.show();

            } else {

                getSelectMode();

                Intent intent = new Intent(this, SetupStepAccount.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }

        });

    }

    private void getSelectMode() {
        if (mBinding.btnNurse.isChecked()) {
            mode = 0;
        } else if (mBinding.btnSecurity.isChecked()) {
            mode = 1;
        } else if (mBinding.btnPath.isChecked()) {
            mode = 2;
        } else if (mBinding.btnEtc.isChecked()) {
            mode = 3;
        }

        tinyDB.putInt(KeyList.DEVICE_TYPE, mode);
        tinyDB.putString(KeyList.DEVICE_WARD, mBinding.inputUseWard.getText().toString());
    }
}
