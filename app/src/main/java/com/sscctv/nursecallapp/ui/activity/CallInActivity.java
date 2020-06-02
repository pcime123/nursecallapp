package com.sscctv.nursecallapp.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.util.ArrayList;


public class CallInActivity extends AppCompatActivity {

    private static final String TAG = "CallInActivity";

    private TextView mName, mNumber;
    private Call mCall;
    private CoreListenerStub mListener;
    private Core mCore;
    private boolean mAlreadyAcceptedOrDeniedCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incall);

        NurseCallUtils.setShowWhenLocked(this, false);
        NurseCallUtils.setTurnScreenOn(this, false);

//        mName = findViewById(R.id.in_name);
//        mNumber = findViewById(R.id.in_num);

        ImageButton mOnCall = findViewById(R.id.onCall);
        mOnCall.setOnClickListener(view -> acceptCall(mCall));
        ImageButton mOffCall = findViewById(R.id.offCall);
        mOffCall.setOnClickListener(view -> {
            decline();
        });

        mListener =
                new CoreListenerStub() {
                    @Override
                    public void onCallStateChanged(
                            Core core, Call call, Call.State state, String message) {
                        Log.d(TAG, "Call Stat: " + state);
                        if (state == Call.State.End || state == Call.State.Released) {
                            mCall = null;
                            finish();
                        }
                    }
                };


    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestCallPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlreadyAcceptedOrDeniedCall = false;

        mCall = null;
        mCore = MainCallService.getCore();
        if (mCore != null) {
            mCore.addListener(mListener);
        }
        if (mCore != null) {
            for (Call call : mCore.getCalls()) {
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()) {
                    mCall = call;
                    break;
                }
            }
        }

        if (mCall == null) {
            finish();
        } else {
            Address address = mCall.getRemoteAddress();
            String id = "sip:" + address.getUsername() + "@" + address.getDomain();
//            mName.setText(String.format("%s %s", address.getUsername(), address.getDisplayName()));
//            mNumber.setText(id);
        }
    }

    @Override
    protected void onPause() {
        if (mCore != null) {
            mCore.removeListener(mListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mName = null;
        mNumber = null;
        mCall = null;
        mListener = null;
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (MainCallService.isReady() && (keyCode == KeyEvent.KEYCODE_BACK) && mCall != null) {
            decline();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
//        if (MainCallService.isReady() && mCall != null) {
//            decline();
//        }
    }

    private void acceptCall(Call call) {
        if (mAlreadyAcceptedOrDeniedCall) {
            return;
        }
        mAlreadyAcceptedOrDeniedCall = true;

        if (call == null) {
            return;
        }

        CallParams params = mCore.createCallParams(call);
        boolean isLowBandwidthConnection = !isHighBandwidthConnection(getApplicationContext());
        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        }
        call.acceptWithParams(params);
    }

    private void decline() {
        if (mAlreadyAcceptedOrDeniedCall) {
            return;
        }
        mAlreadyAcceptedOrDeniedCall = true;

        if (mCall != null) mCall.terminate();
        finish();
    }


    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        return true;
    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();

        int recordAudio = getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        Log.i(TAG, "[Permission] Record audio permission is " + (recordAudio == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        int readPhoneState = getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName());
        Log.i(TAG, "[Permission] Read phone state permission is " + (readPhoneState == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "[Permission] Asking for record audio");
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (readPhoneState != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "[Permission] Asking for read phone state");
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        }
    }
}
