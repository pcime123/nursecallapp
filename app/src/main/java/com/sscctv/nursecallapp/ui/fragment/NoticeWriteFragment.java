package com.sscctv.nursecallapp.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragNoticeWriteBinding;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class NoticeWriteFragment extends Fragment{
    private static final String TAG = NoticeWriteFragment.class.getSimpleName();
    private MainActivity activity;

    private RecyclerView boardList;
    private TinyDB tinyDB;
    private Context context;
    private static final String SEPARATOR = "_@#@_";
    private static final String FILE_EXTENSION = ".txt";
    private FragNoticeWriteBinding mBinding;
    private boolean mode;
    private String fileName;

    public static NoticeWriteFragment newInstance() {
        NoticeWriteFragment fragment = new NoticeWriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        activity = (MainActivity) getActivity();

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        activity = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_notice_write, container, false);
        tinyDB = new TinyDB(getContext());

        mBinding.btnPrev.setOnClickListener(view -> {
//            finish();
            Bundle bundle = new Bundle(1);
            bundle.putString("msg", "prev");

            NoticeFragment fragment = new NoticeFragment();
            fragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        });

        mBinding.btnWrite.setOnClickListener(view -> {
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

        Bundle bundle = this.getArguments();
        Log.d(TAG, "Bundle: " + bundle + " Get: " + bundle.getString("title"));
        fileName = bundle.getString("title");
        if (fileName != null) {
            mode = false;

            String[] fullName = Objects.requireNonNull(fileName).split(SEPARATOR);

            if(fullName[0].equals("t")) {
                mBinding.chk.setChecked(true);
            } else {
                mBinding.chk.setChecked(false);

            }
            mBinding.title.setText(fullName[1]);
            mBinding.person.setText(fullName[2].replaceAll(FILE_EXTENSION, ""));
            mBinding.content.setText(getTextAndRead(fileName));

        }
        return mBinding.getRoot();
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
            NurseCallUtils.printShort(getContext(), "제목이 입력되지 않았습니다.");
            mBinding.title.requestFocus();
            mBinding.title.setCursorVisible(true);
            return;
        }

        if (person.isEmpty()) {
            NurseCallUtils.printShort(getContext(), "작성자가 입력되지 않았습니다.");
            mBinding.person.requestFocus();
            mBinding.person.setCursorVisible(true);
            return;
        }

        if (content.isEmpty()) {
            NurseCallUtils.printShort(getContext(), "내용이 입력되지 않았습니다.");
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
            NurseCallUtils.printShort(getContext(), "중복된 제목의 게시글이 있습니다. 수정 해주세요.");
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

            NurseCallUtils.printShort(getContext(), "게시글 작성이 완료되었습니다.");
            Bundle bundle = new Bundle(1);
            bundle.putString("msg", "ok");

            NoticeFragment fragment = new NoticeFragment();
            fragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delText(String name) {
        File del = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE +
                File.separator + name);
        if(del.delete()) {
        } else {
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
