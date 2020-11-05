package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.CallLogItem;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.ViewHolder> {
    private static final String TAG = CallListAdapter.class.getSimpleName();
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private final ArrayList<CallLogItem> mLogs;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int selectedItem;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private String number;
    private Core core;

    public CallListAdapter(Context context, ArrayList<CallLogItem> logs) {
        super();
        this.context = context;
        mLogs = logs;
        selectedItem = -1;
        disableSelectItem = 0;
        core = MainCallService.getCore();
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
        CallLogItem log = mLogs.get(position);
        selectedItem = position;
        holder.type.setText(String.format("%s %s", log.getCallDir(), log.getCallStaus()));
        holder.date.setText(log.getCallDate());
        holder.time.setText(log.getCallTime());

        String mDevice = log.getCallerId();
//        Log.d(TAG, "Device: " + mDevice);
        if (mDevice.contains("NCTB")) {
            holder.icon1.setImageResource(R.drawable.main_device);
        } else if (mDevice.contains("NCTS")) {
            holder.icon1.setImageResource(R.drawable.security_device);
        } else if (mDevice.contains("NCTP")) {
            holder.icon1.setImageResource(R.drawable.etc_device);
        } else if (mDevice.contains("NCPB")) {
            holder.icon1.setImageResource(R.drawable.call_device);
        } else if(mDevice.contains("NCTO")) {
            holder.icon1.setImageResource(R.drawable.public_device);
        }else {
            holder.icon1.setImageResource(R.drawable.null_device);
        }

        String mCall = log.getCallDir();
        String mStatus = log.getCallStaus();
        if (mStatus.equals("Aborted")) {
            holder.icon2.setImageResource(R.drawable.call_status_miss);
        } else {
            switch (mCall) {
                case "Outgoing":
                    holder.icon2.setImageResource(R.drawable.call_status_out);
                    break;
                case "Incoming":
                    holder.icon2.setImageResource(R.drawable.call_status_in);
                    break;
            }
        }
        holder.device.setText(NurseCallUtils.putDeviceName(mDevice));

//        holder.card.setOnClickListener(view -> {
//            mCallback.roomSelect(position);
//        });
        number = log.getCallNumber();
//        Log.d(TAG, "CallDir: " + log.getCallNumber());
    }

    @Override
    public int getItemCount() {
        return this.mLogs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView type;
        private TextView device;
        private TextView time;
        private TextView date;
        private ImageView icon1;
        private ImageView icon2;
        private CheckBox btn;
        private CardView card;

        ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.listCard);
            type = view.findViewById(R.id.list_call_type);
            device = view.findViewById(R.id.list_call_device);
            time = view.findViewById(R.id.list_time);
            date = view.findViewById(R.id.list_date);
            icon1 = view.findViewById(R.id.list_icon1);
            icon2 = view.findViewById(R.id.list_icon2);

            tinyDB = new TinyDB(context);


            card.setOnClickListener(view1 -> {
                Dialog dialog = new Dialog(Objects.requireNonNull(context));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_call_list);

                final TextView title = dialog.findViewById(R.id.dialog_ward_title);

                final Button call = dialog.findViewById(R.id.dialog_btn_call);
                final Button delete = dialog.findViewById(R.id.dialog_btn_delete);

                title.setText(device.getText().toString());

                call.setOnClickListener(view2 -> {
                    newOutgoingCall(number);
                    dialog.dismiss();
                });

                delete.setOnClickListener(view2 -> {
//                    mCallback.roomSelect(selectedItem);
                    dialog.dismiss();
                });


                dialog.show();
            });



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
