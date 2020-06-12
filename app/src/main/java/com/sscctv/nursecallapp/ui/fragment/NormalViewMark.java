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
import com.sscctv.nursecallapp.databinding.TabCallMarkBinding;
import com.sscctv.nursecallapp.databinding.TabCallSecurityBinding;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.fragment.adapter.TabListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.Collections;

public class NormalViewMark extends Fragment implements OnSelectCall{
    private static final String TAG = "NormalViewMark";
    private TinyDB tinyDB;
    private RecyclerView markList;
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

        TabCallMarkBinding layout = DataBindingUtil.inflate(inflater, R.layout.tab_call_mark, container, false);
        tinyDB = new TinyDB(getContext());
        markList = layout.tabMarkList;
        markList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        markList.setLayoutManager(manager);
        return layout.getRoot();
    }

    private void getList() {
        getArrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_FAVORITE_EXTENSION);

        Collections.sort(getArrayList);

        TabListAdapter adapter = new TabListAdapter(getContext(), getArrayList, false, this);
        markList.setAdapter(adapter);
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
