package com.sscctv.nursecallapp.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BedListAdapter extends RecyclerView.Adapter<BedListAdapter.ViewHolder> {
    private static final String TAG = "BedListAdapter";
    private static final String KEY = KeyList.KEY_BED_LIST;

    private List<BedItem> items;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int selectedItem;
    private int disableSelectItem;
    private TinyDB tinyDB;

    public BedListAdapter(Context context, List<BedItem> items) {
        super();
        this.context = context;
        this.items = items;
        selectedItem = -1;
        disableSelectItem = 0;
        tinyDB = new TinyDB(context);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bed, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String strModel = null;
        BedItem item = items.get(position);
        holder.roomNum.setText(item.getRoom());
        holder.bedNum.setText(item.getNum());
        holder.addr.setText(item.getAddr());

        switch (item.getModel()) {
            case KeyList.MODEL_PAGER_BASIC:
                strModel = "간호사 호출기";
                break;
            case KeyList.MODEL_PAGER_EXTENTION:
                strModel = "간호사 호출기 3P";
                break;
            case KeyList.MODEL_PAGER_OPERATING_01:
                strModel = "수술실 호출기";
                break;
            case KeyList.MODEL_PAGER_PUBLIC:
                strModel = "공용 호출기";
                break;
        }
        holder.model.setText(strModel);

//        holder.bedNum.setTextColor(context.getResources().getColor(R.color.Brown_high));
//        holder.addr.setTextColor(context.getResources().getColor(R.color.Brown_high));
//        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
//        holder.bedColor.setBackgroundColor(context.getResources().getColor(R.color.main));

        if (selectedItem == position) {
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.LightGray));
//            holder.bedNum.setTextColor(context.getResources().getColor(R.color.black));
//            holder.addr.setTextColor(context.getResources().getColor(R.color.black));
//            holder.bedColor.setBackgroundColor(context.getResources().getColor(R.color.Brown_middle));
//            String room = tinyDB.getString(KeyList.KEY_SELECT);
//            ArrayList BedList = NurseCallUtils.getBedList(tinyDB, room);
            if (!holder.addr.getText().toString().equals("None")) {
                NurseCallUtils.newOutgoingCall(context, holder.addr.getText().toString());
            } else {
                NurseCallUtils.printShort(context, "번호를 입력해주세요");
            }
        }

        holder.itemView.setTag(items.get(position));
        holder.itemView.setOnClickListener(view -> {
            int previousItem = selectedItem;
            selectedItem = position;
            notifyItemChanged(previousItem);
            notifyItemChanged(position);
        });

        holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_1));

        if (0 == Integer.valueOf(item.getRoom().substring(1)) % 2) {
            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_2));
        }
        if (0 == Integer.valueOf(item.getRoom().substring(1)) % 3) {
            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_3));
        }
        if (0 == Integer.valueOf(item.getRoom().substring(1)) % 4) {
            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_4));
        }
        if (0 == Integer.valueOf(item.getRoom().substring(1)) % 5) {
            holder.bedColor.setBackgroundColor(tinyDB.getInt(KeyList.BED_COLOR_5));
        }


    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private CardView cardView;
        private TextView roomNum;
        private TextView bedNum;
        private TextView addr;
        private TextView model;

        private FrameLayout bedColor;

        ViewHolder(View view) {
            super(view);
            roomNum = view.findViewById(R.id.room_num);
            bedNum = view.findViewById(R.id.bed_num);
            addr = view.findViewById(R.id.bed_addr);
            model = view.findViewById(R.id.bed_model);
            cardView = view.findViewById(R.id.bedCard);
            bedColor = view.findViewById(R.id.bedColor);
//            view.setOnCreateContextMenuListener(this);

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

                    BedItem bedItem = new BedItem("", getNum.getText().toString(), "", "");
                    items.set(disableSelectItem, bedItem);
                    notifyItemRangeChanged(getAdapterPosition(), items.size());
                    Collections.sort(items, (roomItem1, t1) -> roomItem1.getNum().compareTo(t1.getNum()));
                    NurseCallUtils.putBedList(tinyDB, KEY, (ArrayList<BedItem>) items);
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
                    items.remove(disableSelectItem);
                    notifyItemRemoved(disableSelectItem);
                    notifyItemRangeChanged(disableSelectItem, items.size());
                    NurseCallUtils.putBedList(tinyDB, KEY, (ArrayList<BedItem>) items);
                    notifyDataSetChanged();
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

                    dialog.dismiss();
                });

                no.setOnClickListener(view2 -> {
                    dialog.dismiss();
                });

            });

            dialog.show();

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
