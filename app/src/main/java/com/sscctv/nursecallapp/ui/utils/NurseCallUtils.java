package com.sscctv.nursecallapp.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.data.BcItem;
import com.sscctv.nursecallapp.data.BedItem;
import com.sscctv.nursecallapp.data.CSVItem;
import com.sscctv.nursecallapp.data.CallLogItem;
import com.sscctv.nursecallapp.data.DpItem;
import com.sscctv.nursecallapp.data.EmCallLogItem;
import com.sscctv.nursecallapp.data.EmListItem;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.data.RoomItem;
import com.sscctv.nursecallapp.service.CallDisplayClient;
import com.sscctv.nursecallapp.service.MainCallService;

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

    public static final String STRING_SPLIT= "_@#@_";
    public static final String CALL_INCOMING = "incoming";
    public static final String CALL_DECLINE = "decline";
    public static final String CALL_OUTGOING = "outgoing";
    public static final String CALL_NORMAL = "call";
    public static final String CALL_NEW_INCOMING = "incoming+";
    public static final String CALL_NEW_DECLINE = "list_end";
    public static final String CALL_CALL_END = "end_call";
    public static final String CALL_CONNECTED = "call_connected";

    public static final String ECHO_BYPASS = "0300";
    public static final String ECHO_SPEAKER_GAIN = "02A0";
    public static final String ECHO_MIC_GAIN = "02B2";
    public static final String ECHO_RESET = "0006";
    public NurseCallUtils() {
    }

    public static void getMyCallNumber() {

    }

    public static void sendCallBoard(boolean mode) {

        String host = "175.195.153.233";
        int port = 50054;
        CallDisplayClient client = new CallDisplayClient(host, port);

        if(mode) {
            client.displayCallPhrase("0", "0", "101", "3");
        } else {
            client.stopCallPhrase();
        }
        client.shutdown();
    }




    public static void sendSetTime() {
        String host = "175.195.153.241";
        int port = 50054;
        CallDisplayClient client = new CallDisplayClient(host, port);
        client.setTime();
//        SystemInfo systemInfo = client.getSystem();
//        if (systemInfo == null) {
//            Log.d("CallDisplayClient", "getSystem error");
//
//            client.shutdown();
//            return;
//        }
//        Log.d("CallDisplayClient", "Model      : " + systemInfo.getModel());
//        Log.d("CallDisplayClient", "Version    : " + systemInfo.getVersion());
//        Log.d("CallDisplayClient", "VolumeMax  : " + systemInfo.getVolumeMax());
//        Log.d("CallDisplayClient", "VolumeMin  : " + systemInfo.getVolumeMin());
//        Log.d("CallDisplayClient", "VolumeStep : " + systemInfo.getVolumeStep());
//        Log.d("CallDisplayClient", "VolumeCur  : " + systemInfo.getVolumeCur().getVolume());
//        Log.d("CallDisplayClient", "TimeCur    : " + systemInfo.getTimeCur().getTime());
//        Log.d("CallDisplayClient", "CommonPhrase : " + systemInfo.getCommonPhrase().getPhrase());
//        Log.d("CallDisplayClient", "Network    : " + systemInfo.getNetInfoList().toString());

//                    client.shutdown();
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

    public static void printShortCenter(Context context, String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();




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


    public static void sendRefreshTimer(Context context, int mode) {
        Log.w("Send", "Context: " + context);
        Intent bufferIntentSendCode = new Intent(BROADCAST_BUFFER_SEND_CODE);
        bufferIntentSendCode.putExtra("mode", mode);
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

    public static void putBcList(TinyDB tinyDB, String key, ArrayList<BcItem> bcItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (BcItem bcItem : bcItems) {
            objString.add(gson.toJson(bcItem));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<BcItem> getBcList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<BcItem> bcItems = new ArrayList<>();

        for (String jObjString : objStrings) {
            BcItem bcItem = gson.fromJson(jObjString, BcItem.class);
            bcItems.add(bcItem);
        }
        return bcItems;
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


    public static String putCallName(TinyDB tinyDB, String num) {

        ArrayList<AllExtItem> temp = getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);

        for (int i = 0; i < temp.size(); i++) {
            AllExtItem item = temp.get(i);
//            Log.d("putCallName", "GetNum: " + item.getNum());
            if (item.getNum().equals(num)) {
                return item.getName();
            }
        }

        return num;
    }


    public static void putCallLog(TinyDB tinyDB, String key, ArrayList<CallLogItem> logItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (logItems == null) {
            ArrayList<String> callList = new ArrayList<>();
            callList.add("");
            tinyDB.putListString(key, callList);
        } else {
            for (CallLogItem logItem : logItems) {
                objString.add(gson.toJson(logItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<CallLogItem> getCallLog(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<CallLogItem> callList = new ArrayList<>();

        for (String jObjString : objStrings) {
            CallLogItem callLogItem = gson.fromJson(jObjString, CallLogItem.class);
            callList.add(callLogItem);
        }
        return callList;
    }

    public static void putEmLog(TinyDB tinyDB, String key, ArrayList<EmCallLogItem> logItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (logItems == null) {
            ArrayList<String> emCallList = new ArrayList<>();
            emCallList.add("");
            tinyDB.putListString(key, emCallList);
        } else {
            for (EmCallLogItem logItem : logItems) {
                objString.add(gson.toJson(logItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<EmCallLogItem> getEmLog(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<EmCallLogItem> emCallList = new ArrayList<>();

        for (String jObjString : objStrings) {
            EmCallLogItem emCallLogItem = gson.fromJson(jObjString, EmCallLogItem.class);
            emCallList.add(emCallLogItem);
        }
        return emCallList;
    }

    public static ArrayList<EmListItem> getEmAllList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<EmListItem> emList = new ArrayList<>();

        for (String jObjString : objStrings) {
            EmListItem emLogItem = gson.fromJson(jObjString, EmListItem.class);
            emList.add(emLogItem);
        }
        return emList;
    }

    public static void putEmCallList(TinyDB tinyDB, String key, ArrayList<EmListItem> logItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (logItems == null) {
            ArrayList<String> emList = new ArrayList<>();
            emList.add("");
            tinyDB.putListString(key, emList);
        } else {
            for (EmListItem logItem : logItems) {
                objString.add(gson.toJson(logItem));
            }
            tinyDB.putListString(key, objString);
        }
    }



    public static void putDpList(TinyDB tinyDB, String key, ArrayList<DpItem> dpItems) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        if (dpItems == null) {
            ArrayList<String> nullList = new ArrayList<>();
            nullList.add("");
            tinyDB.putListString(key, nullList);
        } else {
            for (DpItem dpItem : dpItems) {
                objString.add(gson.toJson(dpItem));
            }
            tinyDB.putListString(key, objString);
        }
    }

    public static ArrayList<DpItem> getDpList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<DpItem> dpItems = new ArrayList<>();

        for (String jObjString : objStrings) {
            DpItem dpItem = gson.fromJson(jObjString, DpItem.class);
            dpItems.add(dpItem);
        }
        return dpItems;
    }

    public static String putDeviceName(String device) {

        if(!device.contains("-")){
            return device;
        }

        String model = null;
        String index1 = null;
        String index2 = null;
        String index3 = null;

        String[] callerId = device.split("-");
        if(callerId.length == 4) {
            model = callerId[0];
            index1 = callerId[1];
            index2 = callerId[2];
            index3 = callerId[3];

            if (device.contains(KeyList.MODEL_TELEPHONE_MASTER)) {
                // 기기모델명 + 병동 + 0 + 일련번호 (2자리)
                return index1 + "병동 간호사 스테이션 - " + index3;
            } else if (device.contains(KeyList.MODEL_TELEPHONE_SECURITY)) {
                return index1 + "병동 보안 스테이션 - " + index3;
            } else if (device.contains(KeyList.MODEL_TELEPHONE_PUBLIC)) {
                return index1 + "병동 병리실 - " + index3;
            } else if (device.contains(KeyList.MODEL_PAGER_BASIC) || device.contains(KeyList.MODEL_PAGER_EXTENTION)
                    || device.contains(KeyList.MODEL_PAGER_BASIC_WALL) || device.contains(KeyList.MODEL_PAGER_BASIC_STAND)) {
                // 기기모델명 + 병동 + 병실 + 병상
                return index1 + "병동 " + index2.replaceAll("M", "") + "호 " + index3 + "번 간호사 호출기";
            } else if (device.contains(KeyList.MODEL_PAGER_PUBLIC_STAND) || device.contains(KeyList.MODEL_PAGER_PUBLIC_WALL)) {
                return index1 + "병동 공용통화자기 - " + index3;

            } else if (device.contains(KeyList.MODEL_PAGER_OPERATING_01) || device.contains(KeyList.MODEL_PAGER_OPERATING_02) ||
                    device.contains(KeyList.MODEL_PAGER_OPERATING_03)) {
                return index2 + " 수슬실 호출기 - " + index3;

            }
        } else {
            return device;
        }
        return device;
    }

    public static String telePhoneDetail(String name) {
        //NCTB-10-0-01
        return name;
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
