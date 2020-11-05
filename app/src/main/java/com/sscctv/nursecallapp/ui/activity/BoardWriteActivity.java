package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityBoardBinding;
import com.sscctv.nursecallapp.ui.utils.EncryptionUtil;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class BoardWriteActivity extends AppCompatActivity {
    private static final String TAG = BoardWriteActivity.class.getSimpleName();
    private static final String SEPARATOR = "_@#@_";
    private static final String FILE_EXTENSION = ".txt";
    private ActivityBoardBinding mBinding;
    private TinyDB tinyDB;
    private boolean mode;
    private String fileName;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_board);
        tinyDB = new TinyDB(this);

        mBinding.btnPrev.setOnClickListener(view -> {
//            finish();
        });

        mBinding.bedAdd.setOnClickListener(view -> {
            if(mode){
                writeText();
            } else {
                if(fileName != null) {
                    delText(fileName);
                }
                writeText();
            }
        });

        mode = true;

        Intent intent = getIntent();
        fileName = intent.getStringExtra("title");
        if(fileName != null) {
            mode = false;

            String[] fullName = fileName.split("_@#@_");

            if(fullName[0].equals("t")) {
               mBinding.chk.setChecked(true);
            } else {
                mBinding.chk.setChecked(false);

            }
            mBinding.title.setText(fullName[1]);
            mBinding.person.setText(fullName[2].replaceAll(".txt", ""));
            mBinding.content.setText(getTextAndRead(fileName));


        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
    private void delText(String name) {
        File del = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE +
                File.separator + name);
        if(del.delete()) {
        } else {
        }
    }


    private void writeText() {
        String check;
        if (mBinding.chk.isChecked()) {
            check = "t";
        } else {
            check = "f";
        }

        String title = mBinding.title.getText().toString().replaceAll("\n", "");
        String person = mBinding.person.getText().toString().replaceAll("\n", "");
        String content = mBinding.content.getText().toString();

        if (title.isEmpty()) {
            NurseCallUtils.printShort(this, "제목이 입력되지 않았습니다.");
            mBinding.title.requestFocus();
            mBinding.title.setCursorVisible(true);
            return;
        }

        if (person.isEmpty()) {
            NurseCallUtils.printShort(this, "작성자가 입력되지 않았습니다.");
            mBinding.person.requestFocus();
            mBinding.person.setCursorVisible(true);
            return;
        }

        if (content.isEmpty()) {
            NurseCallUtils.printShort(this, "내용이 입력되지 않았습니다.");
            mBinding.content.requestFocus();
            mBinding.content.setCursorVisible(true);
            return;
        }

        BufferedWriter bw = null;

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE);
        if (!dir.mkdirs()) {
            dir.mkdirs();
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE +
                File.separator + check + SEPARATOR + title + SEPARATOR + person + FILE_EXTENSION);

        FileWriter fw = null;

        if (file.isFile()) {
            NurseCallUtils.printShort(this, "중복된 제목의 게시글이 있습니다. 수정 해주세요.");
            return;
        }

        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (bw != null) {
                bw.close();
            }

            if (fw != null) {
                fw.close();
            }

            NurseCallUtils.printShort(this, "게시글 작성이 완료되었습니다.");
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getTextAndRead(String fileName) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE +
                File.separator + fileName);

        FileReader fr = null;
        BufferedReader bufrd = null;
        StringBuilder strBuffer = new StringBuilder();
        String str = null;
        if (file.exists()) {
            try {
                fr = new FileReader(file);
                bufrd = new BufferedReader(fr);
                while ((str = bufrd.readLine()) != null) {
                    strBuffer.append(str).append("\n");

                }
                bufrd.close();
                fr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return strBuffer.toString();

    }


}
