package com.sscctv.nursecallapp.ui.adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.activity.MainConfigActivity;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BedConfigListAdapter extends RecyclerView.Adapter<BedConfigListAdapter.ViewHolder> {
    private static final String TAG = "BedListAdapter";
    private static final String KEY = "bed_list";

    private List<BedItem> items;
    private Context context;
    private Calendar calendar;
    private TinyDB tinyDB;
    private String mode;

    public BedConfigListAdapter(Context context, List<BedItem> items) {
        super();
        this.context = context;
        this.items = items;
        tinyDB = new TinyDB(context);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bed_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BedItem item = items.get(position);

        holder.bedNum.setText(item.getNum());
        holder.name.setText(item.getAddr());
        holder.enter.setText(item.getModel());


        calendar = Calendar.getInstance();

        holder.edit.setOnClickListener(view -> {

            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_edit_bed);

            final TextView num = dialog.findViewById(R.id.dialog_edit_bed_title);
            final EditText name = dialog.findViewById(R.id.edit_bed_name);
            final EditText enter = dialog.findViewById(R.id.edit_bed_enter);
            final EditText leave = dialog.findViewById(R.id.edit_bed_Leave);

            num.setText(String.format("Bed %s", item.getNum()));
            name.setText(item.getAddr());
            enter.setText(item.getModel());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, i, i1, i2) -> {
                Log.d(TAG, "i: " + i + " i1: " + i1 + " i2: " + i2);
                calendar.set(Calendar.YEAR, i);
                calendar.set(Calendar.MONTH, i1);
                calendar.set(Calendar.DAY_OF_MONTH, i2);
                if (mode.equals("enter")) {
                    enter.setText(dateFormat.format(calendar.getTime()));
                } else if (mode.equals("leave")) {
                    leave.setText(dateFormat.format(calendar.getTime()));
                }
            };
            enter.setOnClickListener(view1 -> {
                mode = "enter";
                new DatePickerDialog(context, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });
            leave.setOnClickListener(view1 -> {
                mode = "leave";
                new DatePickerDialog(context, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });
            final Button apply = dialog.findViewById(R.id.btn_edit_apply);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Enter: " + enter.getText().toString() + " Leave: " + leave.getText().toString());

                if(!enter.getText().toString().equals("None") && !leave.getText().toString().equals("None")){
                    Date dayEnter = null;
                    Date dayLeave = null;
                    try {
                        dayEnter = dateFormat.parse(enter.getText().toString());
                        dayLeave = dateFormat.parse(leave.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (dayEnter == null) {
                        NurseCallUtils.printLong(context, "No Calendar value enter.");
                        return;
                    }

                    if (dayLeave == null) {
                        NurseCallUtils.printLong(context, "No Calendar value leave.");
                        return;
                    }

                    int compare = dayEnter.compareTo(dayLeave);

                    if (compare > 0) {
                        NurseCallUtils.printLong(context, "Calendar value is invalid.");
                        return;
                    }
                }


                BedItem bedItem = new BedItem("",item.getNum(), name.getText().toString(), enter.getText().toString());
                items.set(position, bedItem);
                notifyItemRangeChanged(position, items.size());
                NurseCallUtils.putBedList(tinyDB, MainConfigActivity.getStringDB(tinyDB, KeyList.KEY_SELECT), (ArrayList<BedItem>) items);
                notifyDataSetChanged();
                dialog.dismiss();
            });
            dialog.show();

        });

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        private TextView bedNum;
        private TextView name;
        private TextView enter;
        private TextView leave;

        private Button edit;


        ViewHolder(View view) {
            super(view);

            bedNum = view.findViewById(R.id.bed_list_num);
            name = view.findViewById(R.id.bed_list_name);
            enter = view.findViewById(R.id.bed_list_enter);
            leave = view.findViewById(R.id.bed_list_leave);
            edit = view.findViewById(R.id.bed_list_edit);

//            Button delete = view.findViewById(R.id.bed_list_delete);
//            delete.setOnClickListener(view1 -> {
//                items.remove(getAdapterPosition());
//                notifyItemRemoved(getAdapterPosition());
//                notifyItemRangeChanged(getAdapterPosition(), items.size());
//                NurseCallUtils.putBedList(tinyDB, MainConfigActivity.getStringDB(tinyDB, KeyList.KEY_SELECT), (ArrayList<BedItem>) items);
//                notifyDataSetChanged();
//            });


        }

    }


    public void clear() {
        TinyDB tinyDB = new TinyDB(context);
        int size = items.size();
        if (size > 0) {
            items.subList(0, size).clear();
            notifyItemRangeRemoved(0, size);
            NurseCallUtils.putBedList(tinyDB, KEY, (ArrayList<BedItem>) items);
            notifyDataSetChanged();
        }
    }
}
