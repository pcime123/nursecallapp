package com.sscctv.nursecallapp.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityMainBinding;
import com.sscctv.nursecallapp.databinding.TestPageBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.service.MainPreferences;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;


public class test extends AppCompatActivity {

    private static final String TAG = "test";
    private Core core;
    private int curView;
    private MainPreferences mPrefs;
    private TinyDB tinyDB;

    private TestPageBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.test_page);
        tinyDB = new TinyDB(this);
        core = MainCallService.getCore();

        mBinding.btnFirstCall.setOnClickListener(view -> {
            newOutgoingCall(mBinding.firstCall.getText().toString());
        });

        mBinding.btnSecondCall.setOnClickListener(view -> newOutgoingCall(mBinding.secondCall.getText().toString()));

        mBinding.btnThirdCall.setOnClickListener(view -> newOutgoingCall(mBinding.thirdCall.getText().toString()));

        mBinding.btnResume.setOnClickListener(view -> core.addAllToConference());

        mBinding.btnPause.setOnClickListener(view -> {
            resumeConference();
            newOutgoingCall(mBinding.firstCall.getText().toString());
            core.addAllToConference();

            newOutgoingCall(mBinding.secondCall.getText().toString());
            Call currentCall = core.getCurrentCall();
            for (Call call : core.getCalls()) {
                Log.d(TAG, "Call: " + call.getRemoteAddress().getUsername() + " State: " + currentCall);
//                if (call != null && call.getConference() != null) {
//
//                    if (core.isInConference()) {
//                        displayConferenceCall(call);
//                        conferenceDisplayed = true;
//                    } else if (!pausedConferenceDisplayed) {
//                        displayPausedConference();
//                        pausedConferenceDisplayed = true;
//                    }
//                } else if (call != null && call != currentCall) {
//                    Call.State state = call.getState();
//                    if (state == Call.State.Paused
//                            || state == Call.State.PausedByRemote
//                            || state == Call.State.Pausing) {
//                        displayPausedCall(call);
//                        callThatIsNotCurrentFound = true;
//                    }
//                }
            }
//            core.addToConference()
        });
    }


    public void newOutgoingCall(String to) {
        if (to == null) return;
        Address address = core.interpretUrl(to);

        inviteAddress(address);
    }


    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(getApplicationContext(), "Address null");
        }
    }

    public void pauseConference() {

        if (core == null) return;
        if (core.isInConference()) {
            Log.i(TAG, "[Call Manager] Pausing conference");
            core.leaveConference();
        } else {
            Log.w(TAG, "[Call Manager] Core isn't in a conference, can't pause it");
        }
    }

    public void resumeConference() {

        if (core == null) return;
        if (!core.isInConference()) {
            Log.i(TAG, "[Call Manager] Resuming conference");
            core.enterConference();
        } else {
            Log.w(TAG, "[Call Manager] Core is already in a conference, can't resume it");
        }
    }
}

