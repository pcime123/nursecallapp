package com.sscctv.nursecallapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.MainActivity;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

public class InComingFragment extends Fragment {

    private static final String TAG = "InComingFragment";
    MainActivity activity;
    private TextView mName, mNumber;
    private ImageButton mOnCall, mOffCall;
    private Call mCall;
    private CoreListenerStub mListener;
    private Core core;
    private AudioManager mAudioManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        activity = (MainActivity) getActivity();
        mAudioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        mCall = null;
        core = MainCallService.getCore();
        core.addListener(mListener);

        if (core != null) {

            for (Call call : core.getCalls()) {
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()) {
                    mCall = call;
                    break;
                }
            }

        }


    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_incoming, container, false);
        mName = view.findViewById(R.id.in_name);
        mNumber = view.findViewById(R.id.in_num);

        mOnCall = view.findViewById(R.id.onCall);
        mOnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accptCall(mCall);
            }
        });
        mOffCall = view.findViewById(R.id.offCall);
        mOffCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCall.terminate();
                sendStatus("-1");
            }
        });

        if (mCall == null) {
            sendStatus("-1");
        } else {

            Address address = mCall.getRemoteAddress();
            Log.d(TAG, "Jinseop:" + address.getDisplayName() + " , " + address.getUsername() + " , " + address.getDomain() + " , " + address.getMethodParam()
                    + " , " + address.getPassword() + " , " + address.getTransport() + " , " + address.getPort()
            );
            CallParams params = mCall.getCurrentParams();
            Log.d(TAG, "Session: " + params.getSessionName());

            String id = "sip:" + address.getUsername() + "@" + address.getDomain();
            mName.setText(String.format("%s %s", address.getUsername(), address.getDisplayName()));
            mNumber.setText(id);


            mListener =
                    new CoreListenerStub() {
                        @Override
                        public void onCallStateChanged(
                                Core core, Call call, Call.State state, String message) {
                            if (state == Call.State.End || state == Call.State.Released) {
                                mCall = null;
                                sendStatus("-1");
                            }
                        }
                    };
        }
//        Button btn = rootView.findViewById(R.id.button2);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                activity.onFragmentChange(0);
//            }
//        });
        return view;
    }

    private void accptCall(Call call) {
        if (call == null) {
            return;
        }

        CallParams params = core.createCallParams(call);

        boolean isLowBandwidthConnection = !isHighBandwidthConnection(getContext());

        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        }
        call.acceptWithParams(params);
    }

    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null
                && info.isConnected()
                && isConnectionFast(info.getType(), info.getSubtype()));
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
        // in doubt, assume connection is good.
        return true;
    }

    private void sendStatus(String msg) {
        Intent intent = new Intent("call");
        intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
