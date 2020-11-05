package com.sscctv.nursecallapp.ui.activity;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BedItem;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedListAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class PagingAdapter extends RecyclerView.Adapter<PagingAdapter.ViewHolder> {
    private static final String TAG = PagingAdapter.class.getSimpleName();

    private List<OpenApi.PagingGroupList> items;
    private Context context;

    public PagingAdapter(Context context, List<OpenApi.PagingGroupList> items) {
        super();
        this.context = context;
        this.items = items;

    }

    @NonNull
    @Override
    public PagingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paging_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagingAdapter.ViewHolder holder, int position) {
        holder.num.setText(items.get(position).number);
        holder.name.setText(items.get(position).name);

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView num;
        private TextView name;

        private FrameLayout bedColor;

        ViewHolder(View view) {
            super(view);
            num = view.findViewById(R.id.txtPagingNum);
            name = view.findViewById(R.id.txtPagingName);
        }
    }


}