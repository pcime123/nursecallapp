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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BcItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectCall;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.List;

public class BroadCastAdapter extends RecyclerView.Adapter<BroadCastAdapter.ViewHolder> {
    private static final String TAG = BroadCastAdapter.class.getSimpleName();

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private List<BcItem> items;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private OnSelectCall mCallback;
    private int getSelected;

    public BroadCastAdapter(Context context, List<BcItem> items) {
        super();
        this.context = context;
        this.items = items;
        tinyDB = new TinyDB(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bc_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BcItem item = items.get(position);
        Log.d(TAG, "Num: " + item.getNum() + " Name: " + item.getName() + " pos: " + position);
        getSelected = position;
        if (position == 0) {
            holder.name.setVisibility(View.GONE);
            holder.allName.setVisibility(View.VISIBLE);
            if (item.getName().equals("all")) {
                holder.allName.setText("전체 병실 방송");
            } else {
                holder.allName.setText("전체 병실 등록 안됨");
            }
        } else {
            if (holder.name.getVisibility() == View.GONE) {
                holder.name.setVisibility(View.VISIBLE);
            }

            if (holder.allName.getVisibility() == View.VISIBLE) {
                holder.allName.setVisibility(View.GONE);
            }
            if (item.getName().contains("-")) {
                String[] names = item.getName().split("-");
                String ward = names[0];
                String room = names[1];
                holder.name.setText(ward + "병동 " + room + "병실");
            } else {
                holder.name.setText(item.getName());
            }

        }


        if (!item.isSetup()) {
            holder.roomLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.back_emergency));
        } else {
            if (isItemSelected(position)) {
                holder.roomLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.back_missed_call));
            } else {
                holder.roomLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.back_loon_crest));
            }
        }
//        holder.cardView.setSelected(isItemSelected(position));

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


    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, allName;
        private CardView cardView;
        private LinearLayout roomLayout;

        ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.item_bc_room);
            allName = view.findViewById(R.id.item_bc_all);
            cardView = view.findViewById(R.id.bc_Card);
            roomLayout = view.findViewById(R.id.layout_bc_room);

            cardView.setOnClickListener(view1 -> {
                int position = getAdapterPosition();
                clearSelectedItem();
                toggleItemSelected(position);
            });

            tinyDB = new TinyDB(context);
            cardView.setOnLongClickListener(view1 -> {
                Log.d(TAG, "Long~~~");
                return false;
            });
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

    public int getSelected() {
        return getSelected;
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


}
