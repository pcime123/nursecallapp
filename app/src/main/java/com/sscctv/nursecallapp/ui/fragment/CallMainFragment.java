package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.databinding.FragCallMainBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;

public class CallMainFragment extends Fragment {

    private static final String TAG = CallMainFragment.class.getSimpleName();

    private Call mCall;
    //    private CoreListenerStub mListener;
    private Core mCore;
    private boolean mIsMicMuted;
    private AudioManager mAudioManager;
    private TinyDB tinyDB;
    private boolean isSpeakMode = false;
    private String getCallMode;
    private FragCallMainBinding mBinding;
    private Timer mTimer;
    private boolean isCall;
    private boolean firstHook = true;
    private boolean isConnect = false;
    private boolean isPaused = false;
    private Call pauseCall;
    private Dialog dialog;
    private boolean cctvApp = false;

    public static CallMainFragment newInstance() {
        CallMainFragment fragment = new CallMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCore = MainCallService.getCore();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCall = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_call_main, container, false);
        tinyDB = new TinyDB(getContext());
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            getCallMode = bundle.getString("call");
            Log.d(TAG, "getCallMode: " + getCallMode);
        }

        mBinding.btnSpeakMode.setOnClickListener(view -> {

            isSpeakMode = !mAudioManager.isSpeakerphoneOn();
            Log.d(TAG, "Set Btn Speaker: " + isSpeakMode);
            outSpeakerMode(isSpeakMode);

        });

        mBinding.btnMicMute.setOnClickListener(view -> {
            mIsMicMuted = mCore.micEnabled();
            mBinding.btnMicMute.setSelected(mIsMicMuted);
            mCore.enableMic(!mIsMicMuted);
            if (mIsMicMuted) {
                mBinding.btnMicMute.setImageResource(R.drawable.ic_mic_on_40dp);
            } else {
                mBinding.btnMicMute.setImageResource(R.drawable.ic_mic_off_40dp);
            }
        });

//        mBinding.btnRingMute.setOnClickListener(view -> {
//
//            mBinding.btnRingMute.setImageResource(R.drawable.ic_volume_mute_on_40dp);
//            mBinding.btnRingMute.setImageResource(R.drawable.ic_volume_mute_off_40dp);
//
//        });

        mBinding.outCallEnd.setOnClickListener(view -> {
            sendDecline();
        });
        mBinding.terminateCall.setOnClickListener(view -> {
            Log.d(TAG, "Click Terminate Call");
            if (mCore.getCallsNb() > 1) {
                callNewDecline();
            } else {
                sendDecline();
            }
        });

        mBinding.onCall.setOnClickListener(view -> {
            acceptCall(mCall);
        });

        mBinding.offCall.setOnClickListener(view -> {
            sendDecline();
        });

        mBinding.callPause.setOnClickListener(view -> {
            togglePause();
        });

        mBinding.btnCallVolume.setOnClickListener(view -> {
            if (mBinding.callVolume.getVisibility() == View.VISIBLE) {
                mBinding.callVolume.setVisibility(View.GONE);
                mBinding.btnCallVolume.setImageResource(R.drawable.ic_volume_off_40dp);
            } else {
                mBinding.callVolume.setVisibility(View.VISIBLE);
                mBinding.btnCallVolume.setImageResource(R.drawable.ic_volume_on_40dp);
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        ((MainActivity) MainActivity.context).leftMenuBlock(false);
        outSpeakerMode(!tinyDB.getBoolean(KeyList.CALL_HOOK));

        mCall = null;
        if (getCallMode.equals(NurseCallUtils.CALL_OUTGOING)) {
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
            Log.d(TAG, "get Call: " + mCall);
//            if (mCall == null) {
//                NurseCallUtils.printShort(getContext(), "없는 번호이거나 통화가 종료되었습니다.");
////                finish();
//            } else {
//                Address address = mCall.getRemoteAddress();
//                mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
//                mBinding.callWhere.setText(address.getUsername());
//            }
        } else if (getCallMode.contains(NurseCallUtils.CALL_INCOMING)) {
            if (!getCallMode.contains("+")) {
                for (Call call : mCore.getCalls()) {
                    if (Call.State.IncomingReceived == call.getState()
                            || Call.State.IncomingEarlyMedia == call.getState()) {
                        mCall = call;
                        break;
                    }
                }

                if (mCall != null) {
                    Address address = mCall.getRemoteAddress();
                    mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
                    mBinding.callWhere.setText(address.getUsername());
                    ((MainActivity) Objects.requireNonNull(getActivity())).normalStartCall(NurseCallUtils.putCallName(tinyDB, address.getUsername()));

                    Log.d(TAG, "Get User Name: " + address.getUsername());
                    if (address.getUsername().equals("3905")) {
                        ((MainActivity) Objects.requireNonNull(getActivity())).cctvAppCall();
                        cctvApp = true;
                    }
                }
            }
        } else if (getCallMode.contains(NurseCallUtils.CALL_NORMAL)) {
            isConnect = true;
            isCall = true;
        }
        changeLayout(getCallMode);

        mCore.enableMic(true);
        mIsMicMuted = false;

        MainTimerTask timerTask = new MainTimerTask();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 0, 500);

        tinyDB.putBoolean(KeyList.CALL_CONNECTED, isConnect);
        super.onResume();

    }


    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        ((MainActivity) MainActivity.context).leftMenuBlock(true);
        mTimer.cancel();
        super.onPause();
    }

    private class MainTimerTask extends TimerTask {
        public void run() {
            mHandler.post(isHookTask);
        }
    }

    private Handler mHandler = new Handler();

    private Runnable isHookTask;

    {
        isHookTask = new Runnable() {
            public void run() {

//                Log.d(TAG, "getHook: " + getHookStat() + " firstHook: " + firstHook + " isCall: " + isCall + " isConnect: " + isConnect + " isSpeakMode: " + isSpeakMode + " getSpeakStat: " + getSpeakStat());


                if (!isCall && !firstHook && isConnect) {
//                    Log.d(TAG, "Call Start!");
                    acceptCall(mCall);
                    isSpeakMode = false;
                    outSpeakerMode(firstHook);
                }

                if (getHookStat()) {
                    if (getSpeakStat() && !isSpeakMode) {
                        if (!isConnect) {
                            acceptCall(mCall);
                            outSpeakerMode(false);
                        }
                        outSpeakerMode(false);
                    }
                    if (!getSpeakStat() && isSpeakMode) {
                        outSpeakerMode(false);
                    }
                } else {
                    if (getSpeakStat() && !isSpeakMode) {
                        outSpeakerMode(firstHook);
                        isSpeakMode = false;
                    }
                    if (!getSpeakStat() && isConnect) {
                        callDecline();
                    }
                }

                if (mAudioManager.isSpeakerphoneOn()) {
                    mHandler.post(() -> {
                        mBinding.btnSpeakMode.setImageResource(R.drawable.ic_speak_on_36dp);
                        mBinding.txtSpeakMode.setText("스피커");
                    });
                } else {

                    mBinding.btnSpeakMode.setImageResource(R.drawable.ic_speak_off_36dp);
                    mBinding.txtSpeakMode.setText("수화기");
                }
            }
        };
    }


    private boolean getHookStat() {
        return tinyDB.getBoolean(KeyList.CALL_HOOK);
    }

    private boolean getSpeakStat() {
        return mAudioManager.isSpeakerphoneOn();
    }

    private void acceptCall(Call call) {
        if (call == null) {
            return;
        }

        isConnect = true;
        isCall = true;
        getCallMode = NurseCallUtils.CALL_NORMAL;
        changeLayout(NurseCallUtils.CALL_NORMAL);

        CallParams params = mCore.createCallParams(call);
        call.acceptWithParams(params);
    }

    public void acceptDialog(String msg) {
        int callNb = Integer.parseInt(msg.substring(msg.indexOf("+")));

        Call newCall = mCore.getCalls()[callNb - 1];
        Address address = newCall.getRemoteAddress();

        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_call);

        final TextView title = dialog.findViewById(R.id.dg_txt_nc_name);
        title.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));

        final ImageButton btnOn = dialog.findViewById(R.id.dg_btn_nc_on);
        btnOn.setOnClickListener(view -> {
            mCall.pause();


            dialog.dismiss();

            CallParams params = mCore.createCallParams(newCall);
            newCall.acceptWithParams(params);

            updateCallsList();

        });

        final ImageButton btnOff = dialog.findViewById(R.id.dg_btn_nc_off);
        btnOff.setOnClickListener(view -> {
            newCall.terminate();
            dialog.dismiss();
        });

        dialog.show();
    }

    public void closeDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        updateCallsList();
    }

    private void changeLayout(String mode) {
        mBinding.layoutTxtTimer.setVisibility(View.VISIBLE);
        setVolumeBar(mode);


        switch (mode) {
            case NurseCallUtils.CALL_OUTGOING:
                mBinding.layoutInCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutOutCallBottom.setVisibility(View.VISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutMenu.setVisibility(View.VISIBLE);
                break;
            case NurseCallUtils.CALL_NORMAL:
                mBinding.layoutInCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutOutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.VISIBLE);
                mBinding.layoutMenu.setVisibility(View.VISIBLE);
                mBinding.layoutTxtStat.setVisibility(View.INVISIBLE);
                mBinding.onCall.clearAnimation();
                Address address = mCore.getCurrentCall().getRemoteAddress();
                mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
                mBinding.callWhere.setText(address.getUsername());

                break;
            case NurseCallUtils.CALL_INCOMING:
                mBinding.layoutInCallBottom.setVisibility(View.VISIBLE);
                mBinding.layoutOutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutCallBottom.setVisibility(View.INVISIBLE);
                mBinding.layoutTxtStat.setVisibility(View.VISIBLE);
                mBinding.layoutMenu.setVisibility(View.VISIBLE);
                break;
        }
        updateCallsList();
    }

    private void setVolumeBar(String mode) {
        Log.d(TAG, "Mode: " + mode + " isSpeaker: " + isSpeakMode);
        if(mode.equals(NurseCallUtils.CALL_INCOMING)) {
            mBinding.txtVolume.setText("벨소리 음량");
            mBinding.barRingVolume.setVisibility(View.VISIBLE);
            mBinding.barHeadsetVolume.setVisibility(View.GONE);
            mBinding.barSpeakerVolume.setVisibility(View.GONE);
        } else {
            if(isSpeakMode) {
                mBinding.txtVolume.setText("스피커 음량");
                mBinding.barRingVolume.setVisibility(View.GONE);
                mBinding.barHeadsetVolume.setVisibility(View.GONE);
                mBinding.barSpeakerVolume.setVisibility(View.VISIBLE);
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);
            } else {
                mBinding.txtVolume.setText("수화기 음량");
                mBinding.barRingVolume.setVisibility(View.GONE);
                mBinding.barHeadsetVolume.setVisibility(View.VISIBLE);
                mBinding.barSpeakerVolume.setVisibility(View.GONE);
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);

            }
        }

        mBinding.barHeadsetVolume.setMax(30);
        mBinding.barHeadsetVolume.setProgress(tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME));
        mBinding.barHeadsetVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    seekBar.setProgress(1);
                    tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, 1);
                } else {
                    tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, i);
                }
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.barSpeakerVolume.setMax(30);
        mBinding.barSpeakerVolume.setProgress(tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));
        mBinding.barSpeakerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    seekBar.setProgress(1);
                    tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, 1);
                } else {
                    tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, i);
                }
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.barRingVolume.setMax(mAudioManager.getStreamMaxVolume(STREAM_RING));
        mBinding.barRingVolume.setProgress(tinyDB.getInt(KeyList.KEY_RING_VOLUME));
        mBinding.barRingVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tinyDB.putInt(KeyList.KEY_RING_VOLUME, i);
                mAudioManager.setStreamVolume(STREAM_RING, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateCallsList() {
        Call currentCall = mCore.getCurrentCall();
        if (currentCall != null) {
            setCurrentCallContactInformation();
        }

        boolean callThatIsNotCurrentFound = false;
        mBinding.callsList.removeAllViews();

//        boolean pausedConferenceDisplayed = false;
//        boolean conferenceDisplayed = false;
//        mBinding.conferenceList.removeAllViews();

        for (Call call : mCore.getCalls()) {

//            if (call != null && call.getConference() != null) {
//                if (mCore.isInConference()) {
//                    displayConferenceCall(call);
//                    conferenceDisplayed = true;
//                } else if (!pausedConferenceDisplayed) {
//                    displayPausedConference();
//                    pausedConferenceDisplayed = true;
//
//                }
//            } else

            if (call != null && call != currentCall) {
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
                callThatIsNotCurrentFound ? View.VISIBLE : View.GONE);
//        mActiveCallHeader.setVisibility(
//                currentCall != null && !conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceHeader.setVisibility(conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceList.setVisibility(mConferenceHeader.getVisibility());
    }

    private void setCurrentCallContactInformation() {
        Call call = mCore.getCurrentCall();
        if (call == null) return;

        updateCurrentCallTimer();

        Address address = call.getRemoteAddress();
        mBinding.callWhereBed.setText(NurseCallUtils.putCallName(tinyDB, address.getUsername()));
        mBinding.callWhere.setText(address.getUsername());
    }

    private void updateCurrentCallTimer() {

        Call call = mCore.getCurrentCall();
        if (call == null) return;

        mBinding.timerCall.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        mBinding.timerCall.start();

    }

//    private void displayConferenceCall(final Call call) {
//        LinearLayout conferenceCallView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.call_conference_cell, null, false);
//
//        TextView contactNameView = conferenceCallView.findViewById(R.id.contact_name);
//
//        Address address = call.getRemoteAddress();
//        contactNameView.setText(address.getDisplayName());
//        Chronometer timer = conferenceCallView.findViewById(R.id.call_timer);
//        timer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
//        timer.start();
//
//        ImageView removeFromConference =
//                conferenceCallView.findViewById(R.id.remove_from_conference);
//        removeFromConference.setOnClickListener(
//                v -> removeCallFromConference(call));
//
//        mBinding.conferenceList.addView(conferenceCallView);
//    }

//    private void removeCallFromConference(Call call) {
//        if (call == null || call.getConference() == null) {
//            return;
//        }
//        call.getConference().removeParticipant(call.getRemoteAddress());
//
//        if (call.getCore().getConferenceSize() <= 1) {
//            call.getCore().leaveConference();
//        }
//
//        updateCallsList();
//    }

    private void displayPausedConference() {
//        LinearLayout pausedConferenceView =
//                (LinearLayout)
//                        LayoutInflater.from(this)
//                                .inflate(R.mBinding.call_conference_paused_cell, null, false);
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
        LinearLayout callView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.call_inactive_row, null, false);

        TextView contactName = callView.findViewById(R.id.contact_name);
        Address address = call.getRemoteAddress();
        contactName.setText(address.getDisplayName());

        Chronometer timer = callView.findViewById(R.id.call_timer);
        timer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        timer.start();

        ImageView resumeCall = callView.findViewById(R.id.call_pause);
        resumeCall.setOnClickListener(
                v -> {
                    if (call == mCore.getCurrentCall()) {
//
                        Address curAddress = mCore.getCurrentCall().getRemoteAddress();
                        contactName.setText(curAddress.getDisplayName());

                        for (int i = 0; i < mCore.getCalls().length; i++) {
                            if (mCore.getCalls()[i].getState() == Call.State.Paused) {
                                mCore.getCalls()[i].resume();
                                resumeCall.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);

                            }
                        }

                        Address nextAddress = mCore.getCurrentCall().getRemoteAddress();
                        mBinding.callWhereBed.setText(nextAddress.getDisplayName());
                        mBinding.callWhere.setText(nextAddress.getUsername());
//
                    } else if (call.getState() == Call.State.Paused) {
                        Address curAddress = mCore.getCurrentCall().getRemoteAddress();
                        contactName.setText(curAddress.getDisplayName());

                        resumeCall.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);

                        call.resume();

                        Address nextAddress = mCore.getCurrentCall().getRemoteAddress();
                        mBinding.callWhereBed.setText(nextAddress.getDisplayName());
                        mBinding.callWhere.setText(nextAddress.getUsername());
                    }
                }
        );
        mBinding.callsList.addView(callView);
    }

    private void togglePause() {

        if (!isPaused) {
            isPaused = true;

            pauseCall = mCore.getCurrentCall();
            pauseCall.pause();

            mBinding.iconCallPause.setBackground(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            mBinding.txtCallPause.setText("Play");
        } else {
            isPaused = false;
            pauseCall.resume();

            mBinding.iconCallPause.setBackground(getResources().getDrawable(R.drawable.ic_phone_paused_black_24dp));
            mBinding.txtCallPause.setText("Pause");
        }


    }


    public void callDecline() {
        tinyDB.putBoolean(KeyList.CALL_CONNECTED, isConnect);

        int size = mCore.getCalls().length;
        for (int i = 0; i < size; i++) {
            mCore.getCalls()[i].terminate();
        }

        Call call = mCore.getCurrentCall();
        if (call != null) {
            call.terminate();
        }

        mBinding.timerCall.stop();
        mBinding.layoutTxtTimer.setVisibility(View.GONE);
        mBinding.layoutMenu.setVisibility(View.GONE);
        mBinding.callVolume.setVisibility(View.GONE);
        mBinding.layoutTxtStat.setVisibility(View.VISIBLE);
        mBinding.txtStat.setText("호출 종료 중..");

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        mBinding.txtStat.startAnimation(anim);

    }

    public void callNewDecline() {
        Call call = mCore.getCurrentCall();

        if (call != null) {
            call.terminate();
        }

        for (int i = 0; i < mCore.getCalls().length; i++) {
            if (mCore.getCalls()[i].getState() == Call.State.Paused) {
                mCore.getCalls()[i].resume();
            }
        }
        updateCallsList();
    }

    private void sendDecline() {
        Intent intent = new Intent("call_start");
        intent.putExtra("msg", "decline");
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext())).sendBroadcast(intent);
    }

    private void outSpeakerMode(boolean mode) {
        if (mode == mAudioManager.isSpeakerphoneOn()) {
            return;
        }
        setVolumeBar(getCallMode);

        mAudioManager.setSpeakerphoneOn(mode);
    }

    private String getNameExtension(String address) {
        ArrayList<AllExtItem> arrayList = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_LIST_EXTENSION);

        for (int i = 0; i < arrayList.size(); i++) {
            AllExtItem item = arrayList.get(i);
            if (item.getNum().equals(address)) {
                return item.getName();
            }
        }

        return address;
    }


}
