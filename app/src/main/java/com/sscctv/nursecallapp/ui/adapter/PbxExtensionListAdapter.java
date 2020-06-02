package com.sscctv.nursecallapp.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.utils.KeyList;

import java.util.ArrayList;
import java.util.List;

public class PbxExtensionListAdapter extends RecyclerView.Adapter<PbxExtensionListAdapter.ViewHolder> {

    private static final String TAG = "PbxExtensionAddAdapter";
    private List<ExtItem> items;
    private String idModel, idWard, idRoom, idBed;

    public PbxExtensionListAdapter(List<ExtItem> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bed_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;
        final ExtItem item = items.get(position);
        holder.num.setText(item.getNum());
        String[] callerId = item.getName().split("-");
        idModel = callerId[0];
        idWard = callerId[1];
        idRoom = callerId[2];
        idBed = callerId[3];

        String strModel = null;
        switch (idModel) {
            case KeyList.MODEL_PAGER_BASIC:
                strModel = "간호사 호출기";
                break;
            case KeyList.MODEL_PAGER_EXTENTION:
                strModel = "간호사 호출기 3P";
                break;
            case KeyList.MODEL_PAGER_OPERATING_01:
                strModel = "수술실 호출기";
                break;
            case KeyList.MODEL_PAGER_PUBLIC:
                strModel = "공용 호출기";
                break;
        }

        String strWard;
        Log.d(TAG, "WARD: " + idWard + " RooM: " + idRoom + " Bed: " +idBed);
        if(idWard.equals("0")) {
            strWard = "해당 없음";
        } else {
            strWard = idWard;
        }

        String strRoom;
        if(idRoom.equals("0")) {
            strRoom = "해당 없음";
        } else {
            strRoom = idRoom;
        }

        holder.name.setText(strModel);
        holder.ward.setText(strWard);
        holder.room.setText(strRoom);
        holder.bed.setText(idBed);

        holder.chk.setChecked(item.isSelected());
        holder.chk.setTag(items.get(position));
        holder.chk.setOnClickListener(view -> {
            CheckBox cb = (CheckBox) view;
            ExtItem extItem = (ExtItem) cb.getTag();
            extItem.setSelected(cb.isChecked());
            items.get(pos).setSelected(cb.isChecked());
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(view -> {
            holder.chk.performClick();
        });
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView num, room, ward, name, bed;
        CheckBox chk;

        ViewHolder(View view) {
            super(view);
            chk = view.findViewById(R.id.list_checkbox);
            num = view.findViewById(R.id.bed_list_num);
            name = view.findViewById(R.id.bed_list_name);
            ward = view.findViewById(R.id.bed_list_ward);
            room = view.findViewById(R.id.bed_list_room);
            bed = view.findViewById(R.id.bed_list_bed);
        }
    }

    public List<ExtItem> getSelectedItem() {
        List<ExtItem> sel = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ExtItem item = items.get(i);
            if(item.isSelected()) {
                sel.add(new ExtItem(item.getNum(), item.getName(), true));
            }
        }
        return sel;
    }

    public void setAllSelected(final boolean ischked) {
        final int tempSize = items.size();

        if(ischked) {
            for(int i = 0; i< tempSize; i++) {
                items.get(i).setSelected(true);
            }
        } else {
            for(int i = 0; i< tempSize; i++) {
                items.get(i).setSelected(false);
            }
        }
    }

    public void delete(int position) {

        try {
            items.remove(position);
            notifyItemRemoved(position);
        } catch(IndexOutOfBoundsException ex) {

            ex.printStackTrace();

        }

    }

    public void setSelectDelete() {
        final int tempSize = items.size();


        for(int i = 0; i < tempSize; i++) {

            if(items.get(i).isSelected()) {
                items.remove(i);
                i++;
                notifyItemRemoved(i);
            }

        }
    }


}
