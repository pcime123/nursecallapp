package com.sscctv.nursecallapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.linphone.core.Call.State;
import org.linphone.core.Reason;

public class OutGoingFragment extends Fragment {

    private static final String TAG = "OutGoingFragment";
    MainActivity activity;
    private TextView mName, mNumber;
    private ImageView mMic, mSpeaker, mDecline;
    private Call mCall;
    private CoreListenerStub mListener;
    private Core core;
    private boolean mIsMicMuted, mIsSpeakerEnabled;
    private AudioManager mAudioManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        activity = (MainActivity) getActivity();
        mAudioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        mCall = null;
        core = MainCallService.getCore();
        core.addListener(mListener);

        mIsMicMuted = false;
        mIsSpeakerEnabled = false;




    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_outgoing, container, false);
        mName = view.findViewById(R.id.out_name);
        mNumber = view.findViewById(R.id.out_num);

        mMic = view.findViewById(R.id.micro);
        mMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsMicMuted = !mIsMicMuted;
                mMic.setSelected(mIsMicMuted);
                core.enableMic(!mIsMicMuted);
            }
        });
        mSpeaker = view.findViewById(R.id.speaker);
        mSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsSpeakerEnabled = !mIsSpeakerEnabled;
                mSpeaker.setSelected(mIsSpeakerEnabled);
                if(mIsSpeakerEnabled) {
                    mAudioManager.setSpeakerphoneOn(true);
                } else {
                    mAudioManager.setSpeakerphoneOn(false);
                }
            }
        });
        mDecline = view.findViewById(R.id.outgoing_hang_up);
        mDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              mCall.terminate();
              sendStatus(0);
            }
        });

        if(core != null) {
            for(Call call : core.getCalls()) {
                State cstate = call.getState();
                Log.d(TAG, "Call get State: " + cstate);
                if (State.OutgoingInit == cstate
                        || State.OutgoingProgress == cstate
                        || State.OutgoingRinging == cstate
                        || State.OutgoingEarlyMedia == cstate) {
                    mCall = call;
                    break;
                }
            }
        }

        if(mCall == null) {
            sendStatus(0);
        } else {

            Address address = mCall.getRemoteAddress();
            Log.d(TAG, "Jinseop:" + address.getDisplayName() + " , " + address.getUsername() + " , " + address.getDomain() + " , " + address.getMethodParam()
                    + " , " + address.getPassword() + " , " + address.getTransport() + " , " + address.getPort()
            );
            CallParams params = mCall.getCurrentParams();
            Log.d(TAG, "Session: " + params.getSessionName());

            String id = "sip:" + address.getUsername() + "@" + address.getDomain();
            mName.setText(address.getUsername());
            mNumber.setText(id);


            mListener =
                    new CoreListenerStub() {
                        @Override
                        public void onCallStateChanged(
                                Core core, Call call, Call.State state, String message) {
                            Log.v(TAG, "State: " + state);
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

    private void sendStatus(int msg) {
        Intent intent = new Intent("call");
        intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
