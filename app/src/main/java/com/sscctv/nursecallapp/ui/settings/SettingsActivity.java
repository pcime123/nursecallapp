package com.sscctv.nursecallapp.ui.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsGeneralBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.setup.SetupStepSplash;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private SettingsGeneralBinding mBinding;
    private long down, up;
    private boolean val;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_general);
        EncryptionUtil.getInstance();

        mBinding.btnEthernet.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SettingsEthernet.class);
            Core mCore = MainCallService.getCore();
            mCore.setRing(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Business_Ring.mp3");
            mCore.setRemoteRingbackTone(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Business_Ring.mp3");
        });

        mBinding.btnDisplay.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SetupStepSplash.class);
        });
        mBinding.btnBackup.setOnClickListener(view -> {
//            NurseCallUtils.startIntent(this, SetupStepNetwork.class);
//            NurseCallUtils.startIntent(this, _SettingsActivity.class);
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람 벨소시를 선택하세요");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);  // 무음을 선택 리스트에서 제외
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true); // 기본 벨소리는 선택 리스트에 넣는다.
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
            startActivityForResult(intent, 1);
        });
        mBinding.btnAccount.setOnClickListener(view -> {
            NurseCallUtils.startIntent(this, SettingsSipAccount.class);
        });

        Handler handler = new Handler();
        mBinding.btnAdminAccount.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBinding.btnAdminAccount.setBackgroundColor(getResources().getColor(R.color.temp));
                    val = false;
                    handler.postDelayed(run, 5000/* OR the amount of time you want */);
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mBinding.btnAdminAccount.setBackgroundColor(getResources().getColor(R.color.transparent));
                    handler.removeCallbacks(run);
                    if (!val) {
                        Dialog dialog = new Dialog(this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_admin_login);

                        final EditText pw = dialog.findViewById(R.id.edit_admin_login);
                        final TextView error = dialog.findViewById(R.id.dialog_admin_login_error);
                        final Button apply = dialog.findViewById(R.id.btn_admin_login);
                        apply.setOnClickListener(view1 -> {
                            if (pw.getText().toString().isEmpty()) {
                                if (error.getVisibility() != View.VISIBLE) {
                                    error.setVisibility(View.VISIBLE);
                                    error.setText("비밀번호가 입력되지 않았습니다.");
                                }
                                return;
                            }

                            if (pw.getText().toString().length() < 4) {
                                if (error.getVisibility() != View.VISIBLE) {
                                    error.setVisibility(View.VISIBLE);
                                    error.setText("비밀번호는 최소 4자리 이상 입력되어야 합니다.");
                                }
                                return;
                            }

                            if (pw.getText().toString().equals(EncryptionUtil.getPassword())) {
                                dialog.dismiss();
                                NurseCallUtils.startIntent(this, SettingsAdminAccount.class);
                            } else {
                                if (error.getVisibility() != View.VISIBLE) {
                                    error.setVisibility(View.VISIBLE);
                                    error.setText("비밀번호가 일치하지 않습니다.");
                                }
                            }

                        });
                        dialog.show();
                    }
                    break;

            }
            return true;
        });

        mBinding.btnAdminSetup.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_admin_login);

            final EditText pw = dialog.findViewById(R.id.edit_admin_login);
            final TextView error = dialog.findViewById(R.id.dialog_admin_login_error);
            final Button apply = dialog.findViewById(R.id.btn_admin_login);
            apply.setOnClickListener(view1 -> {
                if (pw.getText().toString().isEmpty()) {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호가 입력되지 않았습니다.");
                    }
                    return;
                }

                if (pw.getText().toString().length() < 4) {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호는 최소 4자리 이상 입력되어야 합니다.");
                    }
                    return;
                }

                if (pw.getText().toString().equals(EncryptionUtil.getPassword())) {
                    dialog.dismiss();
                    NurseCallUtils.startIntent(this, SettingsAdminSetup.class);

                } else {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호가 일치하지 않습니다.");
                    }
                }
            });
            dialog.show();
        });

        mBinding.btnVolume.setOnClickListener(view ->  {
            NurseCallUtils.startIntent(this, SettingsVolume.class);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {

            case 1 :

                if(resultCode == RESULT_OK ) {

                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                    String ringToneName = RingtoneManager.getRingtone(this, uri).getTitle(this);
                    Log.d(TAG, "ToneName: " + ringToneName);
                }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        val = false;
    }

    Runnable run = () -> {
        val = true;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_hide_default);
        dialog.setCancelable(false);

        final Button apply = dialog.findViewById(R.id.btn_admin);
        apply.setOnClickListener(view1 -> {
            val = false;
            changeDefaultPassword();
            dialog.dismiss();
        });
        dialog.show();
    };


    private void changeDefaultPassword() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PASSWORD);
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(EncryptionUtil.encode("1234"));
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
