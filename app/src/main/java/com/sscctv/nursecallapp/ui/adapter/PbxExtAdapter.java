package com.sscctv.nursecallapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.pbx.OpenApi;

import java.util.List;

public class PbxExtAdapter extends RecyclerView.Adapter<PbxExtAdapter.ViewHolder> {

    private List<OpenApi.ExtList> items;


    public PbxExtAdapter(List<OpenApi.ExtList> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pbx_extension_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final OpenApi.ExtList item = items.get(position);
        holder.num.setText(item.extnumber);
        holder.reg.setText(item.status);
        holder.type.setText(item.type);
        holder.name.setText(item.username);
        holder.id.setText(item.agentid);
    }



    @Override
    public int getItemCount() {
        return this.items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView num, reg, type, name, id;

        ViewHolder(View view) {
            super(view);
            num = view.findViewById(R.id.txtNum);
            reg = view.findViewById(R.id.txtReg);
            type = view.findViewById(R.id.txtType);
            name = view.findViewById(R.id.txtName);
            id = view.findViewById(R.id.txtId);
        }
    }
}
