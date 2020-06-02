package com.sscctv.nursecallapp.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.Collections;
import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {
    private static final String TAG = "RoomListAdapter";
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

    public RoomListAdapter(Context context, List<RoomItem> items, OnSelectCall listener) {
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
        if(isItemSelected(position)) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
            tinyDB.putString(KEY_SELECT, item.getNum());
            mCallback.roomSelect();
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.gray));
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

        holder.cardView.getLayoutParams().height = tinyDB.getInt(KeyList.VIEW_HEIGHT) / 7;
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private TextView num;
        private CardView cardView;

        ViewHolder(View view) {
            super(view);

            num = view.findViewById(R.id.roomNum);
            cardView = view.findViewById(R.id.roomCard);
            cardView.setOnClickListener(view1 -> {
                int position = getAdapterPosition();
                clearSelectedItem();
                toggleItemSelected(position);
            });
//            view.setOnCreateContextMenuListener(this);
            tinyDB = new TinyDB(context);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            disableSelectItem = getAdapterPosition();
            Log.d(TAG, "First Position: " + getAdapterPosition());

            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_menu_room);

            final TextView title = dialog.findViewById(R.id.dialog_menu_title);
            final ConstraintLayout layout = dialog.findViewById(R.id.dialog_menu_room);
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(500);

            final LinearLayout menuLayout = dialog.findViewById(R.id.layout_edit_menu);
            final TextView edit = dialog.findViewById(R.id.dialog_edit_room);
            final TextView delete = dialog.findViewById(R.id.dialog_delete_room);
            final TextView deleteAll = dialog.findViewById(R.id.dialog_delete_all);

            final LinearLayout editLayout = dialog.findViewById(R.id.layout_edit_room);
            final EditText getNum = dialog.findViewById(R.id.edit_roomNum);
            final Button apply = dialog.findViewById(R.id.btn_edit_apply);

            title.setText(String.format("Room %s", items.get(getAdapterPosition()).getNum()));

            edit.setOnClickListener(view1 -> {
                layout.setAnimation(animation);

                menuLayout.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);

                apply.setOnClickListener(view2 -> {
                    for (int i = 0; i < items.size(); i++) {
                        if (getNum.getText().toString().equals(items.get(i).getNum())) {
                            NurseCallUtils.printShort(context, "Warning: Same Room Number.");
                            return;
                        }
                    }

                    RoomItem roomItem = new RoomItem(getNum.getText().toString());
//                    items.set(disableSelectItem, roomItem);
                    notifyItemRangeChanged(getAdapterPosition(), items.size());
                    Collections.sort(items, (roomItem1, t1) -> roomItem1.getNum().compareTo(t1.getNum()));
//                    NurseCallUtils.putRoomList(tinyDB, KEY_LIST, (ArrayList<RoomItem>) items);
                    notifyDataSetChanged();
                    dialog.dismiss();
                });
            });

            final LinearLayout deleteLayout = dialog.findViewById(R.id.layout_delete_room);
            final TextView txtDelete = dialog.findViewById(R.id.text_delete_roomNum);
            final TextView txtAllDelete = dialog.findViewById(R.id.text_all_delete_roomNum);
            final Button yes = dialog.findViewById(R.id.btn_delete_yes);
            final Button no = dialog.findViewById(R.id.btn_delete_no);

            delete.setOnClickListener(view1 -> {
                layout.setAnimation(animation);

                menuLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.VISIBLE);
                txtDelete.setVisibility(View.VISIBLE);

                yes.setOnClickListener(view2 -> {

                    tinyDB.remove(items.get(disableSelectItem).getNum());
                    items.remove(disableSelectItem);
                    notifyItemRemoved(disableSelectItem);
                    notifyItemRangeChanged(disableSelectItem, items.size());
//                    NurseCallUtils.putRoomList(tinyDB, KEY_LIST, (ArrayList<RoomItem>) items);
                    notifyDataSetChanged();
                    mCallback.refresh();
                    dialog.dismiss();

                });

                no.setOnClickListener(view2 -> {
                    dialog.dismiss();

                });


            });

            deleteAll.setOnClickListener(view1 -> {
                layout.setAnimation(animation);

                menuLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.VISIBLE);
                txtAllDelete.setVisibility(View.VISIBLE);

                yes.setOnClickListener(view2 -> {
                    clear();
                    mCallback.roomAllClear();
//                    NurseCallUtils.putRoomList(tinyDB, KEY_LIST, (ArrayList<RoomItem>) items);
                    notifyDataSetChanged();
                    dialog.dismiss();
                });

                no.setOnClickListener(view2 -> {
                    dialog.dismiss();
                });

            });

            dialog.show();

        }

    }

    private void toggleItemSelected(int position) {
        Log.d(TAG, "Select: " + mSelectedItems.get(position));
        if (mSelectedItems.get(position)) {
//            mSelectedItems.delete(position);
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
