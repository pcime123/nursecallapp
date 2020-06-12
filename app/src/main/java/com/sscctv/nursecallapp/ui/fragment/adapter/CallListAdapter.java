package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.adapter.CallLogItem;
import com.sscctv.nursecallapp.ui.adapter.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.TinyDB;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.ViewHolder> {
    private static final String TAG = "TabListAdapter";
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private final ArrayList<CallLogItem> mLogs;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int selectedItem;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private String model, ward, zero, serial;
    private boolean visible;
    private OnSelectCall mCallback;

    public CallListAdapter(Context context, ArrayList<CallLogItem> logs, OnSelectCall listener) {
        super();
        this.context = context;
        mLogs = logs;
        selectedItem = -1;
        disableSelectItem = 0;
        this.mCallback = listener;
    }

    public Object getItem(int position) {
        return mLogs.get(position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;
        CallLogItem log = mLogs.get(position);

        holder.type.setText(log.getCallType());
        holder.device.setText(log.getCallLocation());
        holder.date.setText(log.getCallDate());
        holder.time.setText(log.getCallTime());

        String mDevice = log.getDeviceType();

        switch (mDevice){
            case "간호사 호출기":
                holder.icon1.setImageResource(R.drawable.call_device);
                break;
            case "간호사 스테이션":
                holder.icon1.setImageResource(R.drawable.main_device);
                break;
            case "보안 스테이션":
                holder.icon1.setImageResource(R.drawable.security_device);
                break;
            case "병리실 스테이션":
                holder.icon1.setImageResource(R.drawable.etc_device);
                break;
        }

        String mCall = log.getCallType();

        switch (mCall) {
            case "호출 송신":
                holder.icon2.setImageResource(R.drawable.call_out_list);
                break;
            case "호출 수신":
                holder.icon2.setImageResource(R.drawable.call_in_list);
                break;
            case "호출 부재":
                holder.icon2.setImageResource(R.drawable.call_miss_list);
                break;
        }



//
//        if (log.getDir() == Call.Dir.Incoming) {
//            address = log.getFromAddress();
//            if (log.getStatus() == Call.Status.Missed) {
//                holder.type.setText("Missed Call");
//                holder.icon1.setImageResource(R.drawable.call_miss_list);
//            } else {
//                holder.type.setText("Incoming Call");
//                holder.icon1.setImageResource(R.drawable.call_in_list);
//            }
//        } else {
//            address = log.getToAddress();
//            holder.type.setText("Outgoing Call");
//            holder.icon1.setImageResource(R.drawable.call_out_list);
//        }
//
//
//        String displayName = null;
//        String sipUri = (address != null) ? address.asString() : "";
//
//        if (address != null) {
//            displayName = address.getDisplayName();
//        }
//        if (displayName == null) {
//            assert address != null;
//            holder.device.setText(address.getUsername());
//        } else {
//            holder.device.setText(displayName);
//        }
//        holder.btn.setChecked(item.isSelected());
//        holder.btn.setTag(items.get(position));
//        holder.btn.setOnClickListener(view -> {
//            CheckBox cb = (CheckBox) view;
//            AllExtItem extItem = (AllExtItem) cb.getTag();
//            extItem.setSelected(cb.isChecked());
//            items.get(pos).setSelected(cb.isChecked());
//            mCallback.starSelect(pos, item.isSelected());
//            notifyDataSetChanged();
//        });

//
//        if (selectedItem == position) {
//            if (!holder.num.getText().toString().equals("")) {
//
//
//                NurseCallUtils.newOutgoingCall(context, holder.num.getText().toString());
//            } else {
//                NurseCallUtils.printShort(context, "번호를 입력해주세요");
//            }
//        }
//
//
//        holder.itemView.setTag(items.get(position));
//        holder.itemView.setOnClickListener(view -> {
//            int previousItem = selectedItem;
//            selectedItem = position;
//            notifyItemChanged(previousItem);
//            notifyItemChanged(position);
//        });

    }

    @Override
    public int getItemCount() {
        return this.mLogs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView type;
        private TextView device;
        private TextView time;
        private TextView date;
        private ImageView icon1;
        private ImageView icon2;
        private CheckBox btn;
        ViewHolder(View view) {
            super(view);

            type = view.findViewById(R.id.list_call_type);
            device = view.findViewById(R.id.list_call_device);
            time = view.findViewById(R.id.list_time);
            date = view.findViewById(R.id.list_date);
            icon1 = view.findViewById(R.id.list_icon1);
            icon2 = view.findViewById(R.id.list_icon2);
            btn = view.findViewById(R.id.list_select);

            tinyDB = new TinyDB(context);


        }

    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("a HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    public static String _timestampToHumanDate(Context context, long timestamp, String format) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp * 1000);

            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat(format, Locale.getDefault());

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

//    public List<AllExtItem> getSelectedItem() {
//        List<AllExtItem> sel = new ArrayList<>();
//
//        for (int i = 0; i < items.size(); i++) {
//            AllExtItem item = items.get(i);
//            if(item.isSelected()) {
//                sel.add(new AllExtItem(item.getNum(), item.getName(), true));
//            }
//        }
//        return sel;
//    }
//
//    private void toggleItemSelected(int position) {
//        Log.d(TAG, "Select: " + mSelectedItems.get(position));
//        if (mSelectedItems.get(position)) {
////            mSelectedItems.delete(position);
//            notifyItemChanged(position);
//        } else {
//            mSelectedItems.put(position, true);
//            notifyItemChanged(position);
//        }
//    }
//
//    private boolean isItemSelected(int position) {
//        return mSelectedItems.get(position, false);
//    }
//
//    public void clearSelectedItem() {
//        int position;
//
//        for (int i = 0; i < mSelectedItems.size(); i++) {
//            position = mSelectedItems.keyAt(i);
//            mSelectedItems.put(position, false);
//            notifyItemChanged(position);
//        }
//
//        mSelectedItems.clear();
//    }
//
//
//    public void clear() {
//        int size = items.size();
//        if (size > 0) {
//            items.subList(0, size).clear();
//            notifyItemRangeRemoved(0, size);
//        }
//    }


}
