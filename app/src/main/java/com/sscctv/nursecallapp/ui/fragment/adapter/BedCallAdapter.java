package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.RoomItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.List;

public class BedCallAdapter extends RecyclerView.Adapter<BedCallAdapter.ViewHolder> {
    private static final String TAG = BedCallAdapter.class.getSimpleName();
    private static final String KEY_LIST = "room_list";
    private static final String KEY_ROOM_DB = "room$";
    private static final String KEY_SELECT = "room_select";

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private List<RoomItem> items;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private OnSelectCall mCallback;

    public BedCallAdapter(Context context, List<RoomItem> items, OnSelectCall listener) {
        super();
        this.context = context;
        this.items = items;
        this.mCallback = listener;
        tinyDB = new TinyDB(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RoomItem item = items.get(position);
        String roomNum = item.getNum().substring(1);

        holder.num.setText(String.format("%s", roomNum));
//
        if(isItemSelected(position)) {
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
            holder.roomLayout.setBackground(context.getResources().getDrawable(R.drawable.back_fly_high));

            tinyDB.putString(KEY_SELECT, item.getNum());
            mCallback.roomSelect(position);
        } else {
//            holder.cardView.setCardBackgroundColor(context.getResources().getDrawable(R.drawable.back_loon_crest));
            holder.roomLayout.setBackground(context.getResources().getDrawable(R.drawable.back_loon_crest));
        }
        holder.cardView.setSelected(isItemSelected(position));
//        if (selectedItem == position) {
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
//            tinyDB.putString(KEY_SELECT, item.getNum());
//            mCallback.roomSelect();
//        } else {
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.gray));
//        }
//
//        holder.itemView.setTag(items.get(position));
//        holder.itemView.setOnClickListener(view -> {
//            int previousItem = selectedItem;
//            selectedItem = position;
//            notifyItemChanged(previousItem);
//            notifyItemChanged(position);
//        });

        holder.cardView.getLayoutParams().height = tinyDB.getInt(KeyList.VIEW_HEIGHT) / 6;

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView num;
        private CardView cardView;
        private LinearLayout roomLayout;

        ViewHolder(View view) {
            super(view);

            num = view.findViewById(R.id.roomNum);
            cardView = view.findViewById(R.id.roomCard);
            roomLayout = view.findViewById(R.id.layout_roomNum);
            cardView.setOnClickListener(view1 -> {
                int position = getAdapterPosition();
                clearSelectedItem();
                toggleItemSelected(position);
            });
            tinyDB = new TinyDB(context);
        }

    }

    private void toggleItemSelected(int position) {
        Log.d(TAG, "Select: " + mSelectedItems.get(position));
        if (!mSelectedItems.get(position)) {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    private boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clearSelectedItem() {
        int position;

        for (int i = 0; i < mSelectedItems.size(); i++) {
            position = mSelectedItems.keyAt(i);
            mSelectedItems.put(position, false);
            notifyItemChanged(position);
        }

        mSelectedItems.clear();
    }


    public void clear() {
        int size = items.size();
        if (size > 0) {
            items.subList(0, size).clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    private String getSelectRoom() {
        return tinyDB.getString(KEY_SELECT);
    }

    private String compareRoomList(String str) {
        for (int i = 0; i < NurseCallUtils.getRoomList(tinyDB, KEY_LIST).size(); i++) {
            if (str.equals(NurseCallUtils.getRoomList(tinyDB, KEY_LIST).get(i).getNum())) {
                return str;
            }
        }
        return "false";
    }

}
