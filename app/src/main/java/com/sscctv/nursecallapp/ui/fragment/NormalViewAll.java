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
import com.sscctv.nursecallapp.databinding.TabCallAllBinding;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.fragment.adapter.TabListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;

public class NormalViewAll extends Fragment implements OnSelectCall {

    private static final String TAG = NormalViewAll.class.getSimpleName();
    private TinyDB tinyDB;
    private RecyclerView allList;
    private ArrayList<AllExtItem> getArrayList;
    private TabListAdapter adapter;

    @Override
    public void onResume() {
        super.onResume();
//        Log.v(TAG, "onResume()");
        getArrayList = new ArrayList<>();
        getList();
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TabCallAllBinding layout = DataBindingUtil.inflate(inflater, R.layout.tab_call_all, container, false);

        tinyDB = new TinyDB(getContext());
        allList = layout.tabAllList;
        layout.tabAllList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        layout.tabAllList.setLayoutManager(manager);


        return layout.getRoot();
    }

    private void getList() {
        getArrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);
        ArrayList<AllExtItem> temp = new ArrayList<>();
        for (int i = 0; i < getArrayList.size(); i++) {
//            Log.d(TAG, "Number: " + getArrayList.get(i).getNum() + " Name: " + getArrayList.get(i).getName());
            if(getArrayList.get(i).getName().contains("-") && !getArrayList.get(i).getNum().equals(tinyDB.getString(KeyList.SIP_ID))) {
//                Log.d(TAG, "Temp Number: " + getArrayList.get(i).getNum() + " Temp Name: " + getArrayList.get(i).getName());
                temp.add(new AllExtItem(getArrayList.get(i).getNum(), getArrayList.get(i).getName(),getArrayList.get(i).isSelected()));
            }
        }

        adapter = new TabListAdapter(getContext(), temp, true, this);
        allList.setAdapter(adapter);
    }


    @Override
    public void roomSelect(int position) { }

    @Override
    public void roomAllClear() {

    }

    public void refresh() {
        Log.d(TAG, "Refresh");
        onResume();
    }

    @Override
    public void starSelect(String num, boolean chk) {
//        ArrayList<AllExtItem> mark_list = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_FAVORITE_EXTENSION);

        for(int i = 0; i<getArrayList.size(); i++) {

            if(getArrayList.get(i).getNum().equals(num)) {
                getArrayList.get(i).setSelected(chk);
                adapter.notifyDataSetChanged();
                break;
            }
        }
//        NurseCallUtils.putAllExtList(tinyDB, KeyList.KEY_FAVORITE_EXTENSION, mark_list);
        NurseCallUtils.putAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION, getArrayList);
    }
}
