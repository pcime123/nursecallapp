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
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
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
        getArrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);
        ArrayList<AllExtItem> temp = new ArrayList<>();
        for(int i = 0; i<getArrayList.size(); i++) {
            if(getArrayList.get(i).isSelected()) {
                temp.add(new AllExtItem(getArrayList.get(i).getNum(), getArrayList.get(i).getName(), getArrayList.get(i).isSelected()));
            }
        }
        Collections.sort(temp);

        TabListAdapter adapter = new TabListAdapter(getContext(), temp, false, this);
        markList.setAdapter(adapter);
    }

    @Override
    public void roomSelect(int position) { }

    @Override
    public void roomAllClear() {

    }

    public void refresh() {
        onResume();
    }

    @Override
    public void starSelect(String position, boolean chk) {

    }
}
