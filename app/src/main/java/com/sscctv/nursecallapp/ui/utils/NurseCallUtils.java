package com.sscctv.nursecallapp.ui.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.adapter.BedItem;
import com.sscctv.nursecallapp.ui.adapter.CSVItem;
import com.sscctv.nursecallapp.ui.adapter.ExtItem;
import com.sscctv.nursecallapp.ui.adapter.RoomItem;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.security.MessageDigest;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NurseCallUtils {

    private Context mContext;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    public static final String BROADCAST_BUFFER_SEND_CODE = "com.sscctv.nursecallapp.utils.NurseCallUtils";
    public static final String SERVICE_CODE = "com.sscctv.nursecallapp.service.PersistentService";

    public NurseCallUtils() {
    }

    public static Dialog getDialog(Context context, String text) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable d = new ColorDrawable(ContextCompat.getColor(context, R.color.gray));
        d.setAlpha(200);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow()
                .setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(d);

        TextView customText = dialog.findViewById(R.id.dialog_message);
        customText.setText(text);
        return dialog;
    }


    public static String getAddressDisplayName(Address address) {
        if (address == null) return null;

        String displayName = address.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.getUsername();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.asStringUriOnly();
        }
        return displayName;
    }

    public static String getDisplayableAddress(Address addr) {
        return "sip:" + addr.getUsername() + "@" + addr.getDomain();
    }

    public static void sendStatus(Context context, int msg) {
        Intent intent = new Intent("activity_call");
        intent.putExtra("msg", msg);
    }

    public static void printShort(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void printLong(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static void setShowWhenLocked(Activity activity, boolean enable) {
        if (enable) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }

    public static void setTurnScreenOn(Activity activity, boolean enable) {
        if (enable) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    public static String getEncMD5(String txt) throws Exception {

        StringBuffer sbuf = new StringBuffer();

        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(txt.getBytes());

        byte[] msgStr = mDigest.digest();

        for (int i = 0; i < msgStr.length; i++) {
            String tmpEncTxt = Integer.toHexString((int) msgStr[i] & 0x00ff);
            sbuf.append(tmpEncTxt);
        }
        return sbuf.toString();
    }

//    public static void sendFlag(Context context) {
//        sendRefreshTimer(context);
//
////        Intent intent = new Intent(context, PersistentService.class);
////        IntentFilter intentFilter = new IntentFilter(SERVICE_CODE);
////        Log.i("sendFlag","intentFilter: " + intentFilter);
////        context.stopService(intent);
//    }


    public static void sendRefreshTimer(Context context) {
        Log.w("Send", "Context: " + context);
        Intent bufferIntentSendCode = new Intent(BROADCAST_BUFFER_SEND_CODE);
        bufferIntentSendCode.putExtra("time", 120 * 100);
        context.sendBroadcast(bufferIntentSendCode);
    }

    public static void startIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    public static void startNewIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void putRoomList(TinyDB tinyDB, String key, ArrayList<RoomItem> roomItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (RoomItem roomItem : roomItems) {
            objString.add(gson.toJson(roomItem));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<RoomItem> getRoomList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<RoomItem> roomList = new ArrayList<>();

        for (String jObjString : objStrings) {
            RoomItem roomItem = gson.fromJson(jObjString, RoomItem.class);
            roomList.add(roomItem);
        }
        return roomList;
    }

    public static void putBedList(TinyDB tinyDB, String key, ArrayList<BedItem> bedItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (bedItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (BedItem bedItem : bedItems) {
                objString.add(gson.toJson(bedItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<BedItem> getBedList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<BedItem> bedList = new ArrayList<>();

        for (String jObjString : objStrings) {
            BedItem bedItem = gson.fromJson(jObjString, BedItem.class);
            bedList.add(bedItem);
        }
        return bedList;
    }

    public static void putCSVList(TinyDB tinyDB, String key, ArrayList<CSVItem> csvItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (csvItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (CSVItem csvItem : csvItems) {
                objString.add(gson.toJson(csvItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<CSVItem> getCSVList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<CSVItem> csvList = new ArrayList<>();

        for (String jObjString : objStrings) {
            CSVItem csvItem = gson.fromJson(jObjString, CSVItem.class);
            csvList.add(csvItem);
        }
        return csvList;
    }

    public static void putExtList(TinyDB tinyDB, String key, ArrayList<ExtItem> extItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (extItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (ExtItem extItem : extItems) {
                objString.add(gson.toJson(extItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<ExtItem> getExtList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ExtItem> extList = new ArrayList<>();

        for (String jObjString : objStrings) {
            ExtItem extItem = gson.fromJson(jObjString, ExtItem.class);
            extList.add(extItem);
        }
        return extList;
    }

    public static void putAllExtList(TinyDB tinyDB, String key, ArrayList<AllExtItem> extItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (extItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (AllExtItem extItem : extItems) {
                objString.add(gson.toJson(extItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<AllExtItem> getAllExtList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<AllExtItem> extList = new ArrayList<>();

        for (String jObjString : objStrings) {
            AllExtItem extItem = gson.fromJson(jObjString, AllExtItem.class);
            extList.add(extItem);
        }
        return extList;
    }


    public static String putCallName (TinyDB tinyDB, String num) {

        ArrayList<AllExtItem> temp = getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);

        for(int i = 0; i< temp.size(); i++ ) {
            AllExtItem item =  temp.get(i);
//            Log.d("putCallName", "GetNum: " + item.getNum());
            if(item.getNum().equals(num)) {
                return item.getName();
            }
        }

        return num;
    }

    private static void inviteAddress(Context context, Address address) {
        Core core = MainCallService.getCore();
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(context, "Address null");
        }
    }

    public static void newOutgoingCall(Context context, String to) {
        Core core = MainCallService.getCore();
        Log.d("Call", "To: " + to + " Core: " + core);
        if (to == null) return;
        Address address = core.interpretUrl(to);
//        Address address = core.interpretUrl("1004");
        inviteAddress(context, address);
    }

    public static String postSync(String requestURL, String jsonMessage) {

        String message = null;

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestURL)
                    .post(RequestBody.create(jsonMessage, MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Response response = client.newCall(request).execute();
            message = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    public static void dispatchOnUIThread(Runnable r) {
        sHandler.post(r);
    }
}
