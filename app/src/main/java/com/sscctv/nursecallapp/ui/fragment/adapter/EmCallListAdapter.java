package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.EmCallLogItem;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EmCallListAdapter extends RecyclerView.Adapter<EmCallListAdapter.ViewHolder> {
    private static final String TAG = EmCallListAdapter.class.getSimpleName();
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private final ArrayList<EmCallLogItem> mLogs;
    private Context context;
    private Core core;

    public EmCallListAdapter(Context context, ArrayList<EmCallLogItem> logs) {
        super();
        this.context = context;
        mLogs = logs;
        core = MainCallService.getCore();
    }

    public Object getItem(int position) {
        return mLogs.get(position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_em, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmCallLogItem log = mLogs.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.getDefault());
        holder.date.setText(dateFormat.format(log.getCallDate()));
        holder.device.setText(log.getWard() + "병동 " + log.getRoom() + "호실");

        String mDevice = log.getDevice();
        String mStat = log.getStat();
//        Log.d(TAG, "Device: " + mDevice);
        if (mDevice.equals("01") && mStat.equals("01")) {
            holder.icon1.setImageResource(R.drawable.em_on);
            holder.type.setText("긴급 호출 발생");
        } else if (mDevice.equals("01") && mStat.equals("00")) {
            holder.icon1.setImageResource(R.drawable.em_off);
            holder.type.setText("긴급 호출 취소");
        } else if (mDevice.equals("03") && mStat.equals("01")) {
            holder.icon1.setImageResource(R.drawable.real_on);
            holder.type.setText("재중 호출 발생");
        } else if (mDevice.equals("03") && mStat.equals("00")) {
            holder.icon1.setImageResource(R.drawable.real_off);
            holder.type.setText("재중 호출 취소");

        }


    }

    @Override
    public int getItemCount() {
        return this.mLogs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView type;
        private TextView device;
        private TextView date;
        private ImageView icon1;
        private CardView card;

        ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.listCard);
            type = view.findViewById(R.id.list_call_type);
            device = view.findViewById(R.id.list_call_device);
            date = view.findViewById(R.id.list_date);
            icon1 = view.findViewById(R.id.list_icon1);

//            tinyDB = new TinyDB(context);
//            card.setOnClickListener(view1 -> {
//                Dialog dialog = new Dialog(Objects.requireNonNull(context));
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.setContentView(R.layout.dialog_call_list);
//
//                final TextView title = dialog.findViewById(R.id.dialog_ward_title);
//
//                final Button call = dialog.findViewById(R.id.dialog_btn_call);
//                final Button delete = dialog.findViewById(R.id.dialog_btn_delete);
//
//                title.setText(device.getText().toString());
//
//                call.setOnClickListener(view2 -> {
//                    newOutgoingCall(number);
//                    dialog.dismiss();
//                });
//
//                delete.setOnClickListener(view2 -> {
////                    mCallback.roomSelect(selectedItem);
//                    dialog.dismiss();
//                });
//
//
//                dialog.show();
//            });


        }

    }

    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(context, "Address null");
        }
    }

    public void newOutgoingCall(String to) {
        if (to == null) return;
        Address address = core.interpretUrl(to);
        inviteAddress(address);
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
