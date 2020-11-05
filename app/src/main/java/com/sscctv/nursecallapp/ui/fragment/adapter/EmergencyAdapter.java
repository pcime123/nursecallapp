package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.EmListItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {
    private static final String TAG = EmergencyAdapter.class.getSimpleName();

    private List<EmListItem> items;
    private Context context;
    private int selectedItem;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private OnSelectCall mCallback;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);

    public EmergencyAdapter(Context context, List<EmListItem> items, OnSelectCall mCallback) {
        super();
        this.context = context;
        this.items = items;
        selectedItem = -1;
        disableSelectItem = 0;
        this.mCallback = mCallback;
        tinyDB = new TinyDB(context);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        String strModel = null;
        EmListItem item = items.get(position);
        holder.roomNum.setText(item.getRoom());

        holder.bedNum.setText("위급 호출 발생!");

        holder.cardView.setOnClickListener(view -> {
            mCallback.roomSelect(position);
        });
        if (isItemSelected(position)) {
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
        } else {
//            holder.cardView.setCardBackgroundColor(context.getResources().getDrawable(R.drawable.back_loon_crest));
        }
//        holder.addr.setText(item.getAddr());
//
//        switch (item.getModel()) {
//            case KeyList.MODEL_PAGER_BASIC:
//                strModel = "간호사 호출기";
//                break;
//            case KeyList.MODEL_PAGER_EXTENTION:
//                strModel = "간호사 호출기 3P";
//                break;
//            case KeyList.MODEL_PAGER_OPERATING_01:
//                strModel = "수술실 호출기";
//                break;
//        }
//        holder.model.setText(strModel);

//        holder.bedNum.setTextColor(context.getResources().getColor(R.color.Brown_high));
//        holder.addr.setTextColor(context.getResources().getColor(R.color.Brown_high));
//        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
//        holder.bedColor.setBackgroundColor(context.getResources().getColor(R.color.main));

//        if (selectedItem == position) {
////            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.LightGray));
////            holder.bedNum.setTextColor(context.getResources().getColor(R.color.black));
////            holder.addr.setTextColor(context.getResources().getColor(R.color.black));
////            holder.bedColor.setBackgroundColor(context.getResources().getColor(R.color.Brown_middle));
////            String room = tinyDB.getString(KeyList.KEY_SELECT);
////            ArrayList BedList = NurseCallUtils.getBedList(tinyDB, room);
//            if (!holder.addr.getText().toString().equals("None")) {
//                NurseCallUtils.newOutgoingCall(context, holder.addr.getText().toString());
//            } else {
//                NurseCallUtils.printShort(context, "번호를 입력해주세요");
//            }
//        }
//
//        holder.itemView.setTag(items.get(position));
//        holder.itemView.setOnClickListener(view -> {
//            int previousItem = selectedItem;
//            selectedItem = position;
//            notifyItemChanged(previousItem);
//            notifyItemChanged(position);
//        });
//
//        holder.bedColor.setBackgroundColor(item.getColor());

//        holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_1));
//
//        if (0 == Integer.valueOf(item.getRoom()) % 2) {
//            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_2));
//        }
//        if (0 == Integer.valueOf(item.getRoom()) % 3) {
//            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_3));
//        }
//        if (0 == Integer.valueOf(item.getRoom()) % 4) {
//            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_4));
//        }
//        if (0 == Integer.valueOf(item.getRoom()) % 5) {
//            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_5));
//        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView roomNum;
        private TextView bedNum;
        private TextView addr;
        private TextView model;


        ViewHolder(View view) {
            super(view);
            roomNum = view.findViewById(R.id.emergency_room);
            bedNum = view.findViewById(R.id.emergency_txt);
            Typeface face = ResourcesCompat.getFont(context, R.font.britannic);
            roomNum.setTypeface(face);
            cardView = view.findViewById(R.id.emergency_card);
//            cardView.setOnLongClickListener(view1 -> {
//                if (!addr.getText().toString().equals("")) {
//                    NurseCallUtils.newOutgoingCall(context, addr.getText().toString());
//                }
//                return false;
//            });

//            cardView.setOnClickListener(view1 -> {
////                Toast.makeText(context, "안녕하세요", Toast.LENGTH_SHORT).show();
//                Intent popupIntent = new Intent(context, DialogErActivity.class);
//                popupIntent.putExtra("room", roomNum.getText().toString());
//                popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                PendingIntent pie = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                try{
//                    pie.send();
//                } catch (PendingIntent.CanceledException e) {
//                    e.printStackTrace();
//                }
//            });

        }


    }

    private boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

//    private void toastShow(String message) {
//
//        if (toast == null) {
//            toast = makeText(context, message, Toast.LENGTH_SHORT);
//        } else {
//            toast.setText(message);
//        }
//        toast.show();
//    }
//
//    public void clear() {
//        TinyDB tinyDB = new TinyDB(context);
//        int size = items.size();
//        if (size > 0) {
//            items.subList(0, size).clear();
//            notifyItemRangeRemoved(0, size);
//            NurseCallUtils.putBedList(tinyDB, KEY, (ArrayList<BedItem>) items);
//            notifyDataSetChanged();
//        }
//    }
}