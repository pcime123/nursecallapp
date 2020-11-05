package com.sscctv.nursecallapp.ui.settings.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.DpItem;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedExtAddAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

public class AdapterSetDpBoard extends RecyclerView.Adapter<AdapterSetDpBoard.SetDpHolder> {

    private static final String TAG = AdapterSetDpBoard.class.getSimpleName();
    private ArrayList<DpItem> items;
    private boolean mode;
    private Context context;
    private int select_position;
    private TinyDB tinyDB;
    private ArrayList<DpItem> dbList;

    public AdapterSetDpBoard(Context context, ArrayList<DpItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public SetDpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dp_list, parent, false);
        tinyDB = new TinyDB(context);
        dbList = getDpList(tinyDB, KeyList.KEY_DP_LIST);
        return new SetDpHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetDpHolder holder, int position) {
        final DpItem item = items.get(position);
        final int pos = position;

        if (mode) {
            holder.chk.setVisibility(View.VISIBLE);
            holder.chk.setChecked(item.isSelected());
            holder.chk.setTag(items.get(position));
            holder.chk.setOnClickListener(view -> {
                CheckBox cb = (CheckBox) view;
                DpItem dpItem = (DpItem) cb.getTag();
                dpItem.setSelected(cb.isChecked());
                items.get(pos).setSelected(cb.isChecked());
                notifyDataSetChanged();
            });

            for(int i = 0; i<dbList.size(); i++) {

                if(dbList.get(i).getMacAddr().equals(items.get(position).getMacAddr())) {
                    holder.chk.setVisibility(View.INVISIBLE);
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.moreLightRed));
                }

            }

            holder.itemView.setOnClickListener(view -> {
                if(holder.chk.getVisibility() == View.VISIBLE) {
                    holder.chk.performClick();
                } else {
                    Toast.makeText(context, "등록되어 있는 전광판 입니다", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            holder.chk.setVisibility(View.GONE);

            if(pos == select_position) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.moreLightGray));
            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }

        holder.model.setText(items.get(position).getModel());
        holder.ipAddr.setText(items.get(position).getIpAddr());
        holder.macAddr.setText(items.get(position).getMacAddr());
        holder.location.setText(items.get(position).getLocation());


    }

    public class SetDpHolder extends RecyclerView.ViewHolder {
        private CheckBox chk;
        private TextView model;
        private TextView ipAddr;
        private TextView macAddr;
        private TextView location;

        SetDpHolder(@NonNull View itemView) {
            super(itemView);
            chk = itemView.findViewById(R.id.dp_chk);
            model = itemView.findViewById(R.id.dp_model);
            ipAddr = itemView.findViewById(R.id.dp_ip);
            macAddr = itemView.findViewById(R.id.dp_mac);
            location = itemView.findViewById(R.id.dp_location);

            itemView.setOnClickListener(view -> {
                select_position = getAdapterPosition();
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return (null != items ? items.size() : 0);
    }

    public ArrayList<DpItem> getSelectedItem() {
        ArrayList<DpItem> sel = new ArrayList<>();
        Log.d("AdapterSetDpBoard", "getSelectedItem: "+ items.size());

        for (int i = 0; i < items.size(); i++) {
            DpItem item = items.get(i);
            Log.d("AdapterSetDpBoard", "MAC: " + item.getMacAddr() + " , "+item.isSelected());
            if (item.isSelected()) {
                sel.add(new DpItem(true, item.getModel(), item.getIpAddr(), item.getMacAddr(), item.getLocation()));
            }
        }
        return sel;
    }

    public void setChkVisible(boolean mode) {
        this.mode = mode;
    }

    public DpItem getSelectData() {
        return items.get(select_position);
    }

    public int getSelectPosition() {
        return select_position;
    }

    public static ArrayList<DpItem> getDpList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<DpItem> dpItems = new ArrayList<>();

        for (String jObjString : objStrings) {
            DpItem dpItem = gson.fromJson(jObjString, DpItem.class);
            dpItems.add(dpItem);
        }
        return dpItems;
    }

}
