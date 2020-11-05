package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BoardItem;
import com.sscctv.nursecallapp.ui.utils.OnSelectBoard;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.ViewHolder> {
    private static final String TAG = BoardListAdapter.class.getSimpleName();
    private static final String KEY_LIST = "board_list";

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private List<BoardItem> items;
    private Context context;
    private TinyDB tinyDB;
    private OnSelectBoard mCallback;

    public BoardListAdapter(Context context, List<BoardItem> items, OnSelectBoard mCallback) {
        super();
        this.context = context;
        this.items = items;
        this.mCallback = mCallback;
        tinyDB = new TinyDB(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BoardItem item = items.get(position);
//        String roomNum = item.getNum().substring(1);
        String[] str = item.getTitle().split("_@#@_");
        int pos = position;
        if(str[0].equals("t")) {
            if(holder.numHead.getVisibility() == View.VISIBLE) {
                holder.numHead.setVisibility(View.GONE);
            }
            holder.imgHead.setVisibility(View.VISIBLE);
            holder.back.setBackground(context.getResources().getDrawable(R.drawable.back_sky_board));
        } else {
            if(holder.imgHead.getVisibility() == View.VISIBLE) {
                holder.imgHead.setVisibility(View.GONE);
            }
            holder.numHead.setVisibility(View.VISIBLE);
            holder.numHead.setText(String.valueOf(getItemCount() - pos));
            holder.back.setBackground(context.getResources().getDrawable(R.drawable.back_white_board));
        }

        holder.title.setText(str[1]);
        holder.writer.setText(str[2].replace(".txt",""));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss", Locale.getDefault());

        holder.time.setText(dateFormat.format(item.getDate()));

        if(isItemSelected(position)) {
            Log.d(TAG, "File Name: " + item.getTitle());
        }

        holder.back.setOnClickListener(view -> {
            Log.d(TAG, "setOnClickListener Name: " + item.getTitle());
            mCallback.boardSelect(item.getTitle(), String.valueOf(getItemCount() - pos), dateFormat.format(item.getDate()));

        });
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

//        holder.cardView.getLayoutParams().height = tinyDB.getInt(KeyList.VIEW_HEIGHT) / 6;

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout back;
        private ImageView imgHead;
        private TextView numHead;

        private TextView title;
        private TextView writer;
        private TextView time;

        ViewHolder(View view) {
            super(view);
            tinyDB = new TinyDB(context);

            back = view.findViewById(R.id.back_layout);
            imgHead = view.findViewById(R.id.head_import);
            numHead = view.findViewById(R.id.head_num);
            title = view.findViewById(R.id.item_board_title);
            writer = view.findViewById(R.id.writer);
            time = view.findViewById(R.id.item_board_time);

        }

    }

    private void toggleItemSelected(int position) {
        Log.d(TAG, "Select: " + mSelectedItems.get(position));
        if (mSelectedItems.get(position)) {
            notifyItemChanged(position);
        } else {
            mSelectedItems.put(position, true);
            notifyItemChanged(position);
        }
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

//    private String getSelectRoom() {
//        return tinyDB.getString(KEY_SELECT);
//    }

    private String compareRoomList(String str) {
        for (int i = 0; i < NurseCallUtils.getRoomList(tinyDB, KEY_LIST).size(); i++) {
            if (str.equals(NurseCallUtils.getRoomList(tinyDB, KEY_LIST).get(i).getNum())) {
                return str;
            }
        }
        return "false";
    }

}
