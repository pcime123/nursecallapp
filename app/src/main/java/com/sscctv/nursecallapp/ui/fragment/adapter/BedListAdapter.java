package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.BedItem;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class BedListAdapter extends RecyclerView.Adapter<BedListAdapter.ViewHolder> {
    private static final String TAG = BedListAdapter.class.getSimpleName();
    private static final String KEY = KeyList.KEY_BED_LIST;

    private List<BedItem> items;
    private Context context;
    private static int lastClieckedPostition = -1;
    private int selectedItem;
    private int disableSelectItem;
    private TinyDB tinyDB;
    private Toast toast = null;
    String _str;
    String _str1;
    int _val = 0;
    int count = 0;
    boolean chk = false;

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
        }
        holder.model.setText(strModel);

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

        holder.itemView.setTag(items.get(position));
        holder.itemView.setOnClickListener(view -> {
            int previousItem = selectedItem;
            selectedItem = position;
            notifyItemChanged(previousItem);
            notifyItemChanged(position);
        });

        holder.bedColor.setBackgroundColor(item.getColor());

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

            cardView.setOnLongClickListener(view1 -> {
                if (!addr.getText().toString().equals("")) {
                    NurseCallUtils.newOutgoingCall(context, addr.getText().toString());
                }
                return true;
            });

            cardView.setOnClickListener(view1 -> {
                toastShow(roomNum.getText().toString() + "호 " + bedNum.getText().toString() + "번 병상을 호출 하려면 길게 누르세요.");
            });
        }


    }

    private void toastShow(String message) {

        if (toast == null) {
            toast = makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
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
