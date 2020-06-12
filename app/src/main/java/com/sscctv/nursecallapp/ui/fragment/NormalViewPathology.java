package com.sscctv.nursecallapp.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.TabCallEtcBinding;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.fragment.adapter.TabListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;

public class NormalViewPathology extends Fragment implements OnSelectCall {

    private static final String TAG = "NormalViewPathology";
    private TinyDB tinyDB;
    private RecyclerView etcList;
    private ArrayList<AllExtItem> getArrayList;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getArrayList = new ArrayList<>();
        getList();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TabCallEtcBinding layout = DataBindingUtil.inflate(inflater, R.layout.tab_call_etc, container, false);

        tinyDB = new TinyDB(getContext());
        etcList = layout.tabEtcList;
        layout.tabEtcList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        layout.tabEtcList.setLayoutManager(manager);


        return layout.getRoot();
    }

    private void getList() {
        ArrayList<AllExtItem> bedArrayList = new ArrayList<>();
        getArrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);
        for (int i = 0; i < getArrayList.size(); i++) {
            AllExtItem item = getArrayList.get(i);
            if (item.getName().contains("NCTP")) {
                bedArrayList.add(new AllExtItem(item.getNum(), item.getName(),false));
            }
        }
        TabListAdapter adapter = new TabListAdapter(getContext(), bedArrayList,false, this);
        etcList.setAdapter(adapter);
    }


    @Override
    public void roomSelect() {

    }

    @Override
    public void roomAllClear() {

    }

    public void refresh() {
        onResume();
    }

    @Override
    public void starSelect(int position, boolean chk) {

    }
}
