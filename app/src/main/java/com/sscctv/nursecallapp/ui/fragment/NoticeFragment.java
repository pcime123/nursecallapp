package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BoardItem;
import com.sscctv.nursecallapp.databinding.FragNoticeBinding;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BoardListAdapter;
import com.sscctv.nursecallapp.ui.utils.OnSelectBoard;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class NoticeFragment extends Fragment implements OnSelectBoard {
    private static final String TAG = NoticeFragment.class.getSimpleName();
    private MainActivity activity;

    private RecyclerView boardList;
    private TinyDB tinyDB;
    private Context context;

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
        getFileList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragNoticeBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_notice, container, false);
        tinyDB = new TinyDB(getContext());
        boardList = layout.boardList;
        boardList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        boardList.setLayoutManager(manager);

        layout.btnWrite.setOnClickListener(view -> {
//            NurseCallUtils.startIntent(getContext(), BoardWriteActivity.class);
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(NoticeWriteFragment.newInstance());

        });

        layout.btnGetExt1.setOnClickListener(view -> {
//            Log.d(TAG, "GOGO Service " );
//            activity.startService(new Intent(activity, MissedCallService.class));
        });

        return layout.getRoot();
    }

    private void getFileList() {
        ArrayList<BoardItem> list = new ArrayList<>();

        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE;
        File file = new File(root);


        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File value : fileList) {
                list.add(new BoardItem(value.getName(), value.lastModified()));
            }
        }

        Collections.sort(list, (extItem, t1) -> {
            String date = String.valueOf(extItem.getDate());
            String cDate = String.valueOf(t1.getDate());
            return cDate.compareTo(date);
        });


        BoardListAdapter boardListAdapter = new BoardListAdapter(getContext(), compareType(list), this);
        boardList.setAdapter(boardListAdapter);

    }

    private ArrayList<BoardItem> compareType(ArrayList<BoardItem> list) {


        Collections.sort(list, (extItem, t1) -> {
            String[] tInfo = extItem.getTitle().split("_@#@_");
            String tType = tInfo[0];

            String[] fInfo = t1.getTitle().split("_@#@_");
            String fType = fInfo[0];

            return fType.compareTo(tType);
        });


        return list;
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


    @Override
    public void boardSelect(String name, String number, String date) {

        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_board);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        final ImageView icon = dialog.findViewById(R.id.dialog_board_head_icon);
        final TextView num = dialog.findViewById(R.id.dialog_board_head_num);
        final TextView title = dialog.findViewById(R.id.dialog_board_head_title);
        final TextView writer = dialog.findViewById(R.id.dialog_board_head_writer);
        final TextView time = dialog.findViewById(R.id.dialog_board_head_time);
        final TextView content = dialog.findViewById(R.id.dialog_board_content);

        final Button modify = dialog.findViewById(R.id.dialog_board_btn_modify);
        final Button delete = dialog.findViewById(R.id.dialog_board_btn_delete);
        final Button close = dialog.findViewById(R.id.dialog_board_btn_close);

        String[] fullName = name.split("_@#@_");

        if(fullName[0].equals("t")) {
            icon.setVisibility(View.VISIBLE);
            num.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Number" + number);
            icon.setVisibility(View.GONE);
            num.setVisibility(View.VISIBLE);
            num.setText(number);
        }

        title.setText(fullName[1]);
        writer.setText(fullName[2].replaceAll(".txt", ""));
        time.setText(date);

        content.setText(getTextAndRead(name));

        modify.setOnClickListener(view -> {
            Bundle bundle = new Bundle(1);
            bundle.putString("title", name);

            NoticeWriteFragment fragment = new NoticeWriteFragment();
            fragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment).commit();
            dialog.dismiss();
        });

        delete.setOnClickListener(view -> {
            File del = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.BOARD_FILE +
                    File.separator + name);
            if(del.delete()) {
                NurseCallUtils.printShort(getContext(), "게시글이 삭제되었습니다.");
                dialog.dismiss();
                getFileList();
            } else {
                NurseCallUtils.printShort(getContext(), "게시글이 삭제되지 않았습니다.");
            }

        });

        close.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
