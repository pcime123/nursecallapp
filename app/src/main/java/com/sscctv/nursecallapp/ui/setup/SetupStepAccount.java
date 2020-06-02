package com.sscctv.nursecallapp.ui.setup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupAccountBinding;
import com.sscctv.nursecallapp.ui.activity.LauncherActivity;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SetupStepAccount extends AppCompatActivity {
    private static final String TAG = "SetupStepAccount";

    private ActivitySetupAccountBinding mBinding;
    private TinyDB tinyDB;
    Intent intent;
    private static String IV;
    private final static String secretKey = "1435276533124653";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_account);
        tinyDB = new TinyDB(this);
        EncryptionUtil.getInstance();

        mBinding.btnPrev.setOnClickListener(view -> finish());

        mBinding.btnFinish.setOnClickListener(view -> {
            tinyDB.putBoolean(KeyList.FIRST_KEY, true);
            intent = new Intent(this, LauncherActivity.class);
            goLauncher task = new goLauncher();
            task.execute();
        });

        mBinding.inputAdminPw.setText("1234");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("StaticFieldLeak")
    private class goLauncher extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(SetupStepAccount.this);
        String pass,pass1;

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
            pass1 = mBinding.inputAdminPw.getText().toString();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                pass = encode(pass1);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            changePassword(pass);
            return null;
        }

        @Override
        @SuppressLint("DefaultLocale")
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            defaultColorSetup();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            super.onPostExecute(aVoid);
        }
    }

    private void changePassword(String str) {
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
            Log.d(TAG, "String: " + str);
            bufferedWriter.write(str);
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

    private String encode(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IV = secretKey.substring(0, 16);
        byte[] data = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(data, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));
        byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encodeBase64(encrypted));
    }

    private void defaultColorSetup() {
        tinyDB.putInt(KeyList.BED_COLOR_1, Color.parseColor("#af0054"));
        tinyDB.putInt(KeyList.BED_COLOR_2, Color.parseColor("#37a79e"));
        tinyDB.putInt(KeyList.BED_COLOR_3, Color.parseColor("#c67009"));
        tinyDB.putInt(KeyList.BED_COLOR_4, Color.parseColor("#4505c1"));
        tinyDB.putInt(KeyList.BED_COLOR_5, Color.parseColor("#3769b2"));

    }


}
