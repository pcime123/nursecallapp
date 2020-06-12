package com.sscctv.nursecallapp.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSetAccountBinding;
import com.sscctv.nursecallapp.databinding.FragSetAdminAccountBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class SetAdminFragment extends Fragment {

    private static final String TAG = SetAdminFragment.class.getSimpleName();
    private Core core;
    private TinyDB tinyDB;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    private EditText id, pw, domain;

    static SetAdminFragment newInstance() {
        return new SetAdminFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragSetAdminAccountBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_set_admin_account, container, false);

        tinyDB = new TinyDB(getContext());

        core = MainCallService.getCore();

        layout.btnAdminChange.setOnClickListener(view -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_admin_change);

            final EditText pw = dialog.findViewById(R.id.edit_admin_pw);
            final EditText pw_confirm = dialog.findViewById(R.id.edit_admin_pw_confirm);
            final TextView error = dialog.findViewById(R.id.dialog_admin_change_error);
            final Button apply = dialog.findViewById(R.id.btn_admin_pw_change);
            apply.setOnClickListener(view1 -> {
                if (pw.getText().toString().isEmpty() || pw_confirm.getText().toString().isEmpty()) {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호가 입력되지 않았습니다.");
                    }
                    return;
                }

                if(pw.getText().toString().length() < 4 || pw_confirm.getText().toString().length() < 4) {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호는 최소 4자리 이상 입력되어야 합니다.");
                    }
                    return;
                }

                if (pw.getText().toString().equals(pw_confirm.getText().toString())) {

                    if (!pw.getText().toString().equals(EncryptionUtil.getPassword())) {
                        changePassword(pw.getText().toString());
                        dialog.dismiss();
                    } else {
                        if (error.getVisibility() != View.VISIBLE) {
                            error.setVisibility(View.VISIBLE);
                            error.setText("등록된 비밀번호와 동일한 비밀번호가 입력되었습니다.");
                        }
                    }

                } else {
                    if (error.getVisibility() != View.VISIBLE) {
                        error.setVisibility(View.VISIBLE);
                        error.setText("비밀번호가 일치하지 않습니다.");
                    }
                }

            });
            dialog.show();
        });

        layout.btnAdminDefault.setOnClickListener(view -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_admin_hide_default);
            dialog.setCancelable(false);

            final Button apply = dialog.findViewById(R.id.btn_admin);
            apply.setOnClickListener(view1 -> {
                changePassword("1234");
                dialog.dismiss();
            });
            dialog.show();
        });

        return layout.getRoot();
    }

    private void changePassword(String str) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE);

        if(!dir.exists()){
            dir.mkdir();
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + "/" + KeyList.ADMIN_PASSWORD);
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(EncryptionUtil.encode(str));
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
