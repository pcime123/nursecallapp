/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sscctv.nursecallapp.ui.fragment.settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.ui.activity._SettingsActivity;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.service.MainPreferences;
import com.sscctv.nursecallapp.ui.utils.BasicSetting;
import com.sscctv.nursecallapp.ui.utils.ListSetting;
import com.sscctv.nursecallapp.ui.utils.SettingListenerBase;
import com.sscctv.nursecallapp.ui.utils.SwitchSetting;
import com.sscctv.nursecallapp.ui.utils.TextSetting;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.PayloadType;

import java.util.Objects;

import static android.media.AudioManager.STREAM_VOICE_CALL;

public class AudioSettingsFragment extends SettingsFragment {

    private static final String TAG = "AudioSettingsFragment";
    private View mRootView;
    private MainPreferences mPrefs;

    private SwitchSetting mEchoCanceller, mAdaptiveRateControl;
    private TextSetting mMicGain, mSpeakerGain;
    private ListSetting mCodecBitrateLimit;
    private BasicSetting mEchoCalibration, mEchoTester;
    private LinearLayout mAudioCodecs;
    private Core core;
    private boolean mEchoTesterIsRunning;
    private boolean mAudioFocused;
    private AudioManager mAudioManager;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_audio, container, false);
        mEchoTesterIsRunning = false;

        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));
        core = MainCallService.getCore();
        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPrefs = MainPreferences.instance();

        updateValues();
    }

    private void loadSettings() {
        mEchoCanceller = mRootView.findViewById(R.id.pref_echo_cancellation);

        mAdaptiveRateControl = mRootView.findViewById(R.id.pref_adaptive_rate_control);

        mMicGain = mRootView.findViewById(R.id.pref_mic_gain_db);
        mMicGain.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mSpeakerGain = mRootView.findViewById(R.id.pref_playback_gain_db);
        mSpeakerGain.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mCodecBitrateLimit = mRootView.findViewById(R.id.pref_codec_bitrate_limit);

        mEchoCalibration = mRootView.findViewById(R.id.pref_echo_canceller_calibration);

        mEchoTester = mRootView.findViewById(R.id.pref_echo_tester);

        mAudioCodecs = mRootView.findViewById(R.id.pref_audio_codecs);
    }

    private void setListeners() {
        mEchoCanceller.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.setEchoCancellation(newValue);
                    }
                });

        mAdaptiveRateControl.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.enableAdaptiveRateControl(newValue);
                    }
                });

        mMicGain.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setMicGainDb(Float.valueOf(newValue));
                    }
                });

        mSpeakerGain.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setPlaybackGainDb(Float.valueOf(newValue));
                    }
                });

        mCodecBitrateLimit.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onListValueChanged(int position, String newLabel, String newValue) {
                        int bitrate = Integer.valueOf(newValue);
                        mPrefs.setCodecBitrateLimit(bitrate);

                        Core core = MainCallService.getCore();
                        for (final PayloadType pt : core.getAudioPayloadTypes()) {
                            if (pt.isVbr()) {
                                pt.setNormalBitrate(bitrate);
                            }
                        }
                    }
                });

        mEchoCalibration.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        mEchoCalibration.setSubtitle(getString(R.string.ec_calibrating));

                        int recordAudio =
                                getActivity()
                                        .getPackageManager()
                                        .checkPermission(
                                                Manifest.permission.RECORD_AUDIO,
                                                getActivity().getPackageName());
                        if (recordAudio == PackageManager.PERMISSION_GRANTED) {
                            startEchoCancellerCalibration();
                        } else {
                            ((_SettingsActivity) getActivity())
                                    .requestPermissionIfNotGranted(
                                            Manifest.permission.RECORD_AUDIO);
                        }
                    }
                });

        mEchoTester.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        int recordAudio =
                                getActivity()
                                        .getPackageManager()
                                        .checkPermission(
                                                Manifest.permission.RECORD_AUDIO,
                                                getActivity().getPackageName());
                        if (recordAudio == PackageManager.PERMISSION_GRANTED) {
                            if (getEchoTesterStatus()) {
                                stopEchoTester();
                            } else {
                                startEchoTester();
                            }
                        } else {
                            ((_SettingsActivity) getActivity())
                                    .requestPermissionIfNotGranted(
                                            Manifest.permission.RECORD_AUDIO);
                        }
                    }
                });
    }

    private void updateValues() {
        mEchoCanceller.setChecked(mPrefs.echoCancellationEnabled());

        mAdaptiveRateControl.setChecked(mPrefs.adaptiveRateControlEnabled());

        mMicGain.setValue(mPrefs.getMicGainDb());

        mSpeakerGain.setValue(mPrefs.getPlaybackGainDb());

        mCodecBitrateLimit.setValue(mPrefs.getCodecBitrateLimit());

        if (mPrefs.echoCancellationEnabled()) {
            mEchoCalibration.setSubtitle(
                    String.format(
                            getString(R.string.ec_calibrated),
                            String.valueOf(mPrefs.getEchoCalibration())));
        }

        populateAudioCodecs();

        setListeners();
    }

    private void populateAudioCodecs() {
        mAudioCodecs.removeAllViews();
        Core core = MainCallService.getCore();
        if (core != null) {
            for (final PayloadType pt : core.getAudioPayloadTypes()) {
                final SwitchSetting codec = new SwitchSetting(getActivity());
                codec.setTitle(pt.getMimeType());
                /* Special case */
                if (pt.getMimeType().equals("mpeg4-generic")) {
                    codec.setTitle("AAC-ELD");
                }

                codec.setSubtitle(pt.getClockRate() + " Hz");
                if (pt.enabled()) {
                    // Never use codec.setChecked(pt.enabled) !
                    codec.setChecked(true);
                }
                codec.setListener(
                        new SettingListenerBase() {
                            @Override
                            public void onBoolValueChanged(boolean newValue) {
                                pt.enable(newValue);
                            }
                        });

                mAudioCodecs.addView(codec);
            }
        }
    }



    private void startEchoCancellerCalibration() {
        if (getEchoTesterStatus()) stopEchoTester();
        MainCallService.getCore()
                .addListener(
                        new CoreListenerStub() {
                            @Override
                            public void onEcCalibrationResult(
                                    Core core, EcCalibratorStatus status, int delayMs) {
                                if (status == EcCalibratorStatus.InProgress) return;
                                core.removeListener(this);
                                routeAudioToEarPiece();

                                if (status == EcCalibratorStatus.DoneNoEcho) {
                                    mEchoCalibration.setSubtitle(getString(R.string.no_echo));
                                } else if (status == EcCalibratorStatus.Done) {
                                    mEchoCalibration.setSubtitle(
                                            String.format(
                                                    getString(R.string.ec_calibrated),
                                                    String.valueOf(delayMs)));
                                } else if (status == EcCalibratorStatus.Failed) {
                                    mEchoCalibration.setSubtitle(getString(R.string.failed));
                                }
                                mEchoCanceller.setChecked(status != EcCalibratorStatus.DoneNoEcho);
                                ((AudioManager)
                                                getActivity()
                                                        .getSystemService(Context.AUDIO_SERVICE))
                                        .setMode(AudioManager.MODE_NORMAL);
                            }
                        });
        startEcCalibration();
    }

    public void routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true);
    }

    public void routeAudioToEarPiece() {
        routeAudioToSpeakerHelper(false);
    }

    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        Log.w(TAG, "[Audio Manager] Routing audio to " + (speakerOn ? "speaker" : "earpiece"));
        mAudioManager.setSpeakerphoneOn(speakerOn);
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w(TAG, "[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        Log.d(TAG, "[Audio Manager] Mode: MODE_IN_COMMUNICATION");
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res =
                    mAudioManager.requestAudioFocus(
                            null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d(TAG, "[Audio Manager] Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }


    public void startEchoTester() {
        Core core = MainCallService.getCore();
        if (core == null) {
            return;
        }
        mEchoTester.setSubtitle("Is running");

        routeAudioToSpeaker();
        setAudioManagerInCallMode();
        org.linphone.core.tools.Log.i("[Audio Manager] Set audio mode on 'Voice Communication'");
        requestAudioFocus(STREAM_VOICE_CALL);
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
        int sampleRate;
        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
        String sampleRateProperty =
                mAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        sampleRate = Integer.parseInt(sampleRateProperty);
        core.startEchoTester(sampleRate);
        mEchoTesterIsRunning = true;
    }

    public void stopEchoTester() {
        Core core = MainCallService.getCore();
        if (core == null) {
            return;
        }
        mEchoTester.setSubtitle("Is stopped");
        mEchoTesterIsRunning = false;
        core.stopEchoTester();
        routeAudioToEarPiece();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        org.linphone.core.tools.Log.i("[Audio Manager] Set audio mode on 'Normal'");
    }

    public boolean getEchoTesterStatus() {
        return mEchoTesterIsRunning;
    }

    public void startEcCalibration() {
        Core core = MainCallService.getCore();
        if (core == null) {
            return;
        }

        routeAudioToSpeaker();
        setAudioManagerInCallMode();
        Log.i(TAG,"[Audio Manager] Set audio mode on 'Voice Communication'");
        requestAudioFocus(STREAM_VOICE_CALL);
        int oldVolume = mAudioManager.getStreamVolume(STREAM_VOICE_CALL);
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, maxVolume, 0);
        core.startEchoCancellerCalibration();
        mAudioManager.setStreamVolume(STREAM_VOICE_CALL, oldVolume, 0);
    }
}
