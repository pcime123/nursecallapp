package com.sscctv.nursecallapp.ui.settings;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsVolumeBinding;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;

import static android.media.AudioManager.STREAM_ALARM;
import static android.media.AudioManager.STREAM_DTMF;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_NOTIFICATION;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_SYSTEM;
import static android.media.AudioManager.STREAM_VOICE_CALL;


public class SettingsVolume extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Core core;
    private TinyDB tinyDB;
    private SettingsVolumeBinding mBinding;
    private AudioManager mAudioManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_volume);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));

        setVolumeBar();
    }

    private void setVolumeBar() {

        mBinding.barAlarm.setMax(mAudioManager.getStreamMaxVolume(STREAM_ALARM));
        mBinding.barAlarm.setProgress(mAudioManager.getStreamVolume(STREAM_ALARM));
        mBinding.barAlarm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_ALARM, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barDtmf.setMax(mAudioManager.getStreamMaxVolume(STREAM_DTMF));
        mBinding.barDtmf.setProgress(mAudioManager.getStreamVolume(STREAM_DTMF));
        mBinding.barDtmf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_DTMF, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barMusic.setMax(mAudioManager.getStreamMaxVolume(STREAM_MUSIC));
        mBinding.barMusic.setProgress(mAudioManager.getStreamVolume(STREAM_MUSIC));
        mBinding.barMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_MUSIC, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barNoti.setMax(mAudioManager.getStreamMaxVolume(STREAM_NOTIFICATION));
        mBinding.barNoti.setProgress(mAudioManager.getStreamVolume(STREAM_NOTIFICATION));
        mBinding.barNoti.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_NOTIFICATION, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barRing.setMax(mAudioManager.getStreamMaxVolume(STREAM_RING));
        mBinding.barRing.setProgress(mAudioManager.getStreamVolume(STREAM_RING));
        mBinding.barRing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_RING, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barSystem.setMax(mAudioManager.getStreamMaxVolume(STREAM_SYSTEM));
        mBinding.barSystem.setProgress(mAudioManager.getStreamVolume(STREAM_SYSTEM));
        mBinding.barSystem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_SYSTEM, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBinding.barVoice.setMax(mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL));
        mBinding.barVoice.setProgress(mAudioManager.getStreamVolume(STREAM_VOICE_CALL));
        mBinding.barVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioManager.setStreamVolume(STREAM_VOICE_CALL, i, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
}
