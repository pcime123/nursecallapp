package com.sscctv.nursecallapp.ui.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityCallBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import static android.media.AudioManager.STREAM_VOICE_CALL;

public class noneCallActivity extends AppCompatActivity {

    private static final String TAG = "noneCallActivity";

    private CoreListenerStub mCoreListener;
    //    private AudioManager mAudioManager;
    private Chronometer mCallTimer;
    private TextView mContactName;
    private Button btnMute, btnCallMute;
    private Core mCore;
    private AudioManager mAudioManager;

    private LinearLayout mCallsList, mConferenceList;
    private ActivityCallBinding mBinding;
    private Handler mHandler;
    private TinyDB tinyDB;
    private boolean mAudioFocused;

    private boolean callStat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_call);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_call);

        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        tinyDB = new TinyDB(this);
        mCallsList = findViewById(R.id.calls_list);
        mConferenceList = findViewById(R.id.conference_list);

        mContactName = findViewById(R.id.current_contact_name);
        mCallTimer = findViewById(R.id.current_call_timer);
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (state == Call.State.End || state == Call.State.Released) {
                    if (core.getCallsNb() == 0) {
                        finish();
                    }
                }
                updateCallsList();
            }
        };

        setVolumeBar();
        mHandler = new Handler();
        mBinding.btnVolumeControl.setOnClickListener(view -> {
            if (getVolumeBarVisible() == View.INVISIBLE) {
                mBinding.barVolume.setVisibility(View.VISIBLE);
                autoControlVolumeBar();
            }
        });
        mBinding.btnVolumeMute.setOnClickListener(view -> {
//            mCore.addAllToConference();
            //Speaker 모드
            mAudioManager.setSpeakerphoneOn(true);
//            toggleSpeaker();
        });

        mBinding.btnMicMute.setOnClickListener(view -> {
//            toggleMic();
            mAudioManager.setSpeakerphoneOn(false);
        });

        mBinding.btnModeChange.setOnClickListener(view -> {
//            Log.d(TAG, "Mode: " + mAudioManager.getMode() + "getRingMode: " + mAudioManager.getRingerMode() );
        });

//        mBinding.btnNewCall.setOnClickListener(view -> {
//            EditText edit_num = findViewById(R.id.edit_new_call);
//            newOutgoingCall(edit_num.getText().toString());
//        });
//
//        mBinding.btnTransCall.setOnClickListener(view -> {
//            EditText edit_num = findViewById(R.id.edit_trans_call);
//            if (mCore.getCurrentCall() == null) {
//                return;
//            }
//            mCore.getCurrentCall().transfer(edit_num.getText().toString());
//        });

        mBinding.terminateCall.setOnClickListener(view -> {
            Core core = MainCallService.getCore();
            if (core.getCallsNb() > 0) {
                Call call = core.getCurrentCall();
                if (call == null) {
                    call = core.getCalls()[0];
                }
                call.terminate();
            }
            finish();
        });

        mBinding.callPause.setOnClickListener(view -> {

//            Log.d(TAG, "getCall: " +mCore.getCurrentCall() );
//            Log.d(TAG, "getCallsNb: " +mCore.getCallsNb() );
//            for (Call call : mCore.getCalls()) {
//                Log.d(TAG, "call: " +call );
//                togglePause(call);
//            }
//            mAudioManager.getParameters("speaker");
//            mAudioManager.getParameters("sub_mic");
//       Log.i(TAG, "Speaker: " + mAudioManager.getParameters("speaker") + " Mic: " + mAudioManager.getParameters("sub_mic")) ;

        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Mode: " + mAudioManager.isSpeakerphoneOn());
//        if (!mAudioManager.isSpeakerphoneOn()) {
//            tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, true);
//            tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, false);
//        } else {
//            tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, false);
//            tinyDB.putBoolean(KeyList.BTN_SPEAKER_STATUS, true);
//        }

        mCore = MainCallService.getCore();
        if (mCore != null) {
            mCore.addListener(mCoreListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCallsList();
//        mAudioManager.setMode(MODE_IN_COMMUNICATION);
//        if(tinyDB.getBoolean(KeyList.BTN_SPEAKER_STATUS)) {
//            mAudioManager.setSpeakerphoneOn(true);
//            Log.d(TAG, "Call --- Speaker");
//        } else {
//            mAudioManager.setSpeakerphoneOn(false);
//            Log.d(TAG, "Call --- HandSet");
//
//        }

    }

    @Override
    protected void onPause() {
        Call call = mCore.getCurrentCall();
        if (call == null) {
            mCore.removeListener(mCoreListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Core core = MainCallService.getCore();
        if (core != null) {
            core.removeListener(mCoreListener);
            core.setNativeVideoWindowId(null);
            core.setNativePreviewWindowId(null);
        }
        mCallTimer.stop();
        mCallTimer = null;

        mCoreListener = null;
        if (mAudioManager.isSpeakerphoneOn()) {
            tinyDB.putBoolean(KeyList.CALL_MODE, false);
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (mAudioManager.onKeyVolumeAdjust(keyCode)) return true;
//        return super.onKeyDown(keyCode, event);
//    }


    private void updateCurrentCallTimer() {
        Call call = mCore.getCurrentCall();
        if (call == null) return;

        mCallTimer.setBase(SystemClock.elapsedRealtime() - 1000 * call.getDuration());
        mCallTimer.start();
    }

    private void setCurrentCallContactInformation() {
        updateCurrentCallTimer();

        Call call = mCore.getCurrentCall();
        if (call == null) return;
        Address address = call.getRemoteAddress();
        mContactName.setText(address.getUsername());
    }


    private void updateCallsList() {
        Call currentCall = mCore.getCurrentCall();
        if (currentCall != null) {
            setCurrentCallContactInformation();
        }

        boolean callThatIsNotCurrentFound = false;
        boolean pausedConferenceDisplayed = false;
        boolean conferenceDisplayed = false;
        Log.d(TAG, "List: " + mCallsList);
        mCallsList.removeAllViews();
        mConferenceList.removeAllViews();

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


        mCallsList.setVisibility(
                pausedConferenceDisplayed || callThatIsNotCurrentFound ? View.VISIBLE : View.GONE);
//        mActiveCallHeader.setVisibility(
//                currentCall != null && !conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceHeader.setVisibility(conferenceDisplayed ? View.VISIBLE : View.GONE);
//        mConferenceList.setVisibility(mConferenceHeader.getVisibility());
    }


//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_F8:
//                if (tinyDB.getBoolean(KeyList.BTN_HANDSET_STATUS)) {
//                   decline();
//                }
//                tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, false);
//
//                return false;
//
//        }
//        return super.onKeyUp(keyCode, event);
//    }
//
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        Log.d(TAG, "onKeyDown=" + keyCode + " event: " + event);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_F8:
//                tinyDB.putBoolean(KeyList.BTN_HANDSET_STATUS, true);
//                return true;
//
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();

        if(flag == 40) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_F8:
                if (action == KeyEvent.ACTION_DOWN) {
                    if (!tinyDB.getBoolean(KeyList.CALL_MODE)) {
                        tinyDB.putBoolean(KeyList.CALL_MODE, true);
                    }
                } else if (action == KeyEvent.ACTION_UP) {
                    if (flag != KeyEvent.FLAG_CANCELED) {
                        if (tinyDB.getBoolean(KeyList.CALL_MODE)) {
                            decline();
                            tinyDB.putBoolean(KeyList.CALL_MODE, false);
                        }
                    }
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
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
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeCallFromConference(call);
                    }
                });

        mConferenceList.addView(conferenceCallView);
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

        mCallsList.addView(callView);
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

    Runnable hide = new Runnable() {
        @Override
        public void run() {
            mBinding.barVolume.setVisibility(View.INVISIBLE);
        }
    };

    private void autoControlVolumeBar() {
        if (getVolumeBarVisible() == View.VISIBLE) {
            mHandler.removeCallbacks(hide);
            mHandler.postDelayed(hide, 5000);
        }
    }

    private int getVolumeBarVisible() {
        return mBinding.barVolume.getVisibility();
    }

    private void setVolumeBar() {
        if (mAudioManager.isVolumeFixed()) {
            Log.e(TAG, "[Audio Manager] Can't adjust volume, device has it fixed...");

        }
        int max = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
        int cur = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);

        mBinding.barVolume.setMax(max);
        mBinding.barVolume.setProgress(cur);
        mBinding.barVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_VOICE_CALL, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                mHandler.removeCallbacks(hide);
                mHandler.postDelayed(hide, 5000);
            }
        });

//        mAudioManager.adjustStreamVolume(STREAM_VOICE_CALL, i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    private void toggleMic() {
        mCore.enableMic(!mCore.micEnabled());
//        mMicro.setSelected(!mCore.micEnabled());
    }
//
//    private void toggleSpeaker() {
//        if (mAudioManager.isAudioRoutedToSpeaker()) {
//            mAudioManager.routeAudioToEarPiece();
//        } else {
//            mAudioManager.routeAudioToSpeaker();
//        }
////        mSpeaker.setSelected(mAudioManager.isAudioRoutedToSpeaker());
//    }

    private void decline() {
        Log.d(TAG, "decline!!!!");
        Core core = MainCallService.getCore();
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                call = core.getCalls()[0];
            }
            call.terminate();
        }

        if(mAudioManager.isSpeakerphoneOn()) {
            tinyDB.putBoolean(KeyList.CALL_MODE, false);
        } else {
            tinyDB.putBoolean(KeyList.CALL_MODE, false);
        }
        finish();
    }
}
