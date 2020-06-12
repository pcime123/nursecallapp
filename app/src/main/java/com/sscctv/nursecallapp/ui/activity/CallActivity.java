package com.sscctv.nursecallapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityOutcallBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.adapter.CallLogItem;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Reason;

import java.util.ArrayList;
import java.util.Objects;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = CallActivity.class.getSimpleName();
    private Call mCall;
    private CoreListenerStub mListener;
    private Core mCore;
    private boolean mIsMicMuted;
    private AudioManager mAudioManager;
    private TinyDB tinyDB;
    private ActivityOutcallBinding mBinding;
    private boolean isUsedSpeaker, isHook;
    private String getCallMode;
    private String startCallMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_outcall);
        tinyDB = new TinyDB(this);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        getCallMode = Objects.requireNonNull(intent.getExtras()).getString("call");
        callChangeMode(getCallMode);
        startCallMode = getCallMode;
        Log.d(TAG, "Change ---> CallMode: " + getCallMode);

        mBinding.outSpeaker.setOnClickListener(view -> {
            mBinding.outSpeaker.setSelected(tinyDB.getBoolean(KeyList.CALL_MODE));
            outSpeakerMode(tinyDB.getBoolean(KeyList.CALL_MODE));

            if (tinyDB.getBoolean(KeyList.CALL_MODE)) {
                tinyDB.putBoolean(KeyList.CALL_MODE, false);
                isUsedSpeaker = true;
                mBinding.txtOutSpeaker.setText("스피커");
            } else {
                tinyDB.putBoolean(KeyList.CALL_MODE, true);
                isUsedSpeaker = false;
                mBinding.txtOutSpeaker.setText("수화기");
            }

        });

        mBinding.outMicMute.setOnClickListener(view -> {
            mIsMicMuted = mCore.micEnabled();
            mBinding.outMicMute.setSelected(mIsMicMuted);
            mCore.enableMic(!mIsMicMuted);
        });

        mBinding.outSpeakerMute.setOnClickListener(view -> {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
//            }
            Log.d(TAG, "Selected Mute: " + mAudioManager.getMode());

            if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                mBinding.outSpeakerMute.setSelected(true);
            } else {
                while (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                    Log.d(TAG, "???");
                    // 모드 변경 시 2번 이상 설정해야 변경되는 버그..
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                }
                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                mBinding.outSpeakerMute.setSelected(false);
            }


        });

        mBinding.outCallEnd.setOnClickListener(view -> decline());
        mBinding.terminateCall.setOnClickListener(view -> decline());
        mListener =
                new CoreListenerStub() {
                    @Override
                    public void onCallStateChanged(
                            Core core, Call call, Call.State state, String message) {
                        Log.d(TAG, "Status: " + state);
                        if (state == Call.State.Connected) {
                            if (!getCallMode.equals("incoming")) {
                                changeLayout("call");
                            }
                            updateCallsList();
                        }
                        if (state == Call.State.Error) {
                            if (call.getErrorInfo().getReason() == Reason.Declined) {
                                NurseCallUtils.printShort(getApplicationContext(), "Call declined");
                            } else if (call.getErrorInfo().getReason() == Reason.NotFound) {
                                NurseCallUtils.printShort(getApplicationContext(), "User not found");
                            } else if (call.getErrorInfo().getReason() == Reason.NotAcceptable) {
                                NurseCallUtils.printShort(getApplicationContext(), "Call declined");
                            } else if (call.getErrorInfo().getReason() == Reason.Busy) {
                                NurseCallUtils.printShort(getApplicationContext(), "User busy");


                            } else if (message != null) {
                                NurseCallUtils.printShort(getApplicationContext(), "Unknown error: " + message);
                            }
                        } else if (state == Call.State.End) {
                            if (call.getErrorInfo().getReason() == Reason.Declined) {
                                NurseCallUtils.printShort(getApplicationContext(), "Call declined");
                            }
                        }

                        if (state == Call.State.End || state == Call.State.Released) {
                            finish();
                            tinyDB.putBoolean(KeyList.CALL_MODE, false);
                        }
                    }
                };

        mBinding.onCall.setOnClickListener(view -> {
            acceptCall(mCall);
        });

        mBinding.offCall.setOnLongClickListener(view -> {
            decline();
            return false;
        });

        mBinding.callPause.setOnClickListener(view -> {
            for (Call call : mCore.getCalls()) {
                Log.d(TAG, "call: " + call);
                togglePause(call);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        mCore = MainCallService.getCore();
        if (mCore != null) {
            mCore.addListener(mListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "isSpeaker: " + mAudioManager.isSpeakerphoneOn());

        mAudioManager.setSpeakerphoneOn(!tinyDB.getBoolean(KeyList.CALL_MODE));

        if (mAudioManager.isSpeakerphoneOn()) {
            mBinding.outSpeaker.setSelected(true);
            mBinding.txtOutSpeaker.setText("스피커");
        } else {
            mBinding.outSpeaker.setSelected(false);
            mBinding.txtOutSpeaker.setText("수화기");
        }

        mCall = null;
        if (getCallMode.equals("outgoing")) {
            if (mCore != null) {
                for (Call call : mCore.getCalls()) {
                    Call.State callState = call.getState();
                    if (Call.State.OutgoingInit == callState
                            || Call.State.OutgoingProgress == callState
                            || Call.State.OutgoingRinging == callState
                            || Call.State.OutgoingEarlyMedia == callState) {
                        mCall = call;
                        break;
                    }
                }
            }
            if (mCall == null) {
                NurseCallUtils.printShort(this, "없는 번호이거나 통화가 종료되었습니다.");
                finish();
            } else {
                Address address = mCall.getRemoteAddress();
//            String id = "sip:" + address.getUsername() + "@" + address.getDomain();
                mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
//                mBinding.callWhere.setText(address.getUsername());
            }
        } else {
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
//                String id = "sip:" + address.getUsername() + "@" + address.getDomain();
                mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
//                mBinding.callWhere.setText(address.getUsername());
            }
        }

        mCore.enableMic(true);
        isUsedSpeaker = false;
        mIsMicMuted = false;
        isHook = false;

    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        if (mCore != null) {
            mCore.removeListener(mListener);
        }
//        Address address = mCall.getRemoteAddress();
//        ArrayList<CallLogItem> items = new ArrayList<>();
//        items.add(new CallLogItem("normal", startCallMode, address.getUsername(), mBinding.currentCallTimer.getText().toString(), String.valueOf(System.currentTimeMillis()), true));
//        NurseCallUtils.putCallLog(tinyDB, KeyList.CALL_LOG, items);
//        Log.d(TAG, "Call Time: " + mBinding.currentCallTimer.getText().toString());

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        mCall = null;
        mListener = null;
        super.onDestroy();
    }

    private void callChangeMode(String mode) {

        changeLayout(mode);

    }

    private void acceptCall(Call call) {
//        if (mAlreadyAcceptedOrDeniedCall) {
//            return;
//        }
//        mAlreadyAcceptedOrDeniedCall = true;

        if (call == null) {
            return;
        }

        CallParams params = mCore.createCallParams(call);
        boolean isLowBandwidthConnection = !isHighBandwidthConnection(getApplicationContext());
        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        }
        call.acceptWithParams(params);

        changeLayout("call");
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


    private void changeLayout(String mode) {


        switch (mode) {
            case "outgoing":
                mBinding.layoutOnlyIn.setVisibility(View.INVISIBLE);

                mBinding.layoutOutCallBottom.setVisibility(View.VISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutInCallBottom.setVisibility(View.INVISIBLE);

                mBinding.menu.setVisibility(View.VISIBLE);
                mBinding.currentCallTimer.setVisibility(View.INVISIBLE);
                break;
            case "call":
                mBinding.layoutOnlyIn.setVisibility(View.INVISIBLE);

                mBinding.layoutOutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.VISIBLE);
                mBinding.layoutInCallBottom.setVisibility(View.INVISIBLE);

                mBinding.menu.setVisibility(View.VISIBLE);
                mBinding.currentCallTimer.setVisibility(View.VISIBLE);
                mBinding.activeCallInfo.setVisibility(View.VISIBLE);

                ConstraintSet callConstraintSet = new ConstraintSet();
                callConstraintSet.clone(mBinding.constraintCall);
                callConstraintSet.setVerticalBias(R.id.layout_call_where, (float) 0.5);
                callConstraintSet.applyTo(mBinding.constraintCall);
                break;
            case "incoming":
                mBinding.layoutOnlyIn.setVisibility(View.VISIBLE);
                mBinding.layoutOutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutInCallBottom.setVisibility(View.VISIBLE);

                mBinding.menu.setVisibility(View.INVISIBLE);
                mBinding.activeCallInfo.setVisibility(View.INVISIBLE);

                ConstraintSet inComingConstraintSet = new ConstraintSet();
                inComingConstraintSet.clone(mBinding.constraintCall);
                inComingConstraintSet.setVerticalBias(R.id.layout_call_where, (float) 0.4);
                inComingConstraintSet.applyTo(mBinding.constraintCall);

                break;
        }

    }


    private void updateCallsList() {
        Call currentCall = mCore.getCurrentCall();
        if (currentCall != null) {
            setCurrentCallContactInformation();
        }

        boolean callThatIsNotCurrentFound = false;
        boolean pausedConferenceDisplayed = false;
        boolean conferenceDisplayed = false;
        Log.d(TAG, "List: " + mBinding.callsList);
        mBinding.callsList.removeAllViews();
        mBinding.conferenceList.removeAllViews();

        for (Call call : mCore.getCalls()) {
            if (call != null && call.getConference() != null) {
                if (mCore.isInConference()) {
                    displayConferenceCall(call);
                    conferenceDisplayed = true;
                } else if (!pausedConferenceDisplayed) {
                    displayPausedConference();
                    pausedConferenceDisplayed = true;
                }
            } else if (call != null && call != currentCall) {
                Call.State state = call.getState();
                if (state == Call.State.Paused
                        || state == Call.State.PausedByRemote
                        || state == Call.State.Pausing) {
                    displayPausedCall(call);
                    callThatIsNotCurrentFound = true;
                }
            }
        }


        mBinding.callsList.setVisibility(
                pausedConferenceDisplayed || callThatIsNotCurrentFound ? View.VISIBLE : View.GONE);
//        mActiveCallHeader.setVisibility(
//                currentCall != null && !conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceHeader.setVisibility(conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceList.setVisibility(mConferenceHeader.getVisibility());
    }

    private void setCurrentCallContactInformation() {
        updateCurrentCallTimer();

        Call call = mCore.getCurrentCall();
        if (call == null) return;
        Address address = call.getRemoteAddress();
        mBinding.currentContactName.setText(address.getUsername());
    }

    private void updateCurrentCallTimer() {
        Call call = mCore.getCurrentCall();
        if (call == null) return;

        mBinding.currentCallTimer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        mBinding.currentCallTimer.start();
    }

    private void displayConferenceCall(final Call call) {
        LinearLayout conferenceCallView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.call_conference_cell, null, false);

        TextView contactNameView = conferenceCallView.findViewById(R.id.contact_name);

        Address address = call.getRemoteAddress();
        contactNameView.setText(address.getDisplayName());
        Chronometer timer = conferenceCallView.findViewById(R.id.call_timer);
        timer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        timer.start();

        ImageView removeFromConference =
                conferenceCallView.findViewById(R.id.remove_from_conference);
        removeFromConference.setOnClickListener(
                v -> removeCallFromConference(call));

        mBinding.conferenceList.addView(conferenceCallView);
    }

    public void removeCallFromConference(Call call) {
        if (call == null || call.getConference() == null) {
            return;
        }
        call.getConference().removeParticipant(call.getRemoteAddress());

        if (call.getCore().getConferenceSize() <= 1) {
            call.getCore().leaveConference();
        }

        updateCallsList();
    }

    private void displayPausedConference() {
//        LinearLayout pausedConferenceView =
//                (LinearLayout)
//                        LayoutInflater.from(this)
//                                .inflate(R.layout.call_conference_paused_cell, null, false);
//
//        ImageView conferenceResume = pausedConferenceView.findViewById(R.id.conference_resume);
//        conferenceResume.setSelected(true);
//        conferenceResume.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        LinphoneManager.getCallManager().resumeConference();
//                        updateCallsList();
//                    }
//                });
//
//        mCallsList.addView(pausedConferenceView);
    }

    private void displayPausedCall(final Call call) {
        LinearLayout callView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.call_inactive_row, null, false);

        TextView contactName = callView.findViewById(R.id.contact_name);
        Address address = call.getRemoteAddress();
//        contactName.setText(address.getDisplayName());
        contactName.setText(address.getDisplayName());

        Chronometer timer = callView.findViewById(R.id.call_timer);
        timer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        timer.start();

        ImageView resumeCall = callView.findViewById(R.id.call_pause);
        resumeCall.setOnClickListener(
                v -> togglePause(call));

        mBinding.callsList.addView(callView);
    }

    private void togglePause(Call call) {
        if (call == null) return;

        if (call == mCore.getCurrentCall()) {
            call.pause();
            mBinding.iconCallPause.setBackground(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            mBinding.textCallPause.setText("Play");
        } else if (call.getState() == Call.State.Paused) {
            call.resume();
            mBinding.iconCallPause.setBackground(getResources().getDrawable(R.drawable.ic_phone_paused_black_24dp));
            mBinding.textCallPause.setText("Pause");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();

        if (flag == 40) {
            return true;
        }

        Log.d(TAG, "Code: " + keyCode + " Speaker: " + mAudioManager.isSpeakerphoneOn() + " CallMode: " + tinyDB.getBoolean(KeyList.CALL_MODE));
        if (keyCode == KeyEvent.KEYCODE_F8) {
            if (action == KeyEvent.ACTION_DOWN) {
                if(getCallMode.equals("incoming") && mAudioManager.isSpeakerphoneOn()) {
                    acceptCall(mCall);
                }
                if (!mAudioManager.isSpeakerphoneOn()) {

                    return true;
                } else {
                    if (!tinyDB.getBoolean(KeyList.CALL_MODE)) {
                        if (!isUsedSpeaker) {
                            tinyDB.putBoolean(KeyList.CALL_MODE, true);
                            outSpeakerMode(false);
                            mBinding.outSpeaker.setSelected(!mBinding.outSpeaker.isSelected());
                            mBinding.txtOutSpeaker.setText("수화기");
                        }
                    }
                }
            } else if (action == KeyEvent.ACTION_UP) {
                if (tinyDB.getBoolean(KeyList.CALL_MODE)) {
                    tinyDB.putBoolean(KeyList.CALL_MODE, false);
                    outSpeakerMode(true);
                    decline();
                } else {
                    isUsedSpeaker = false;
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void decline() {
        Log.w(TAG, "Call Decline!!!!");
        mCall.terminate();
        finish();
    }

    private void outSpeakerMode(boolean mode) {
        if (mode == mAudioManager.isSpeakerphoneOn()) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(mode);
    }
}
