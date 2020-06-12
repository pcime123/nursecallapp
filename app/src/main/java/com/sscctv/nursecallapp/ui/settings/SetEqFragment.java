package com.sscctv.nursecallapp.ui.settings;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSetEqBinding;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.DataOutputStream;


public class SetEqFragment extends Fragment {

    private static final String TAG = SetEqFragment.class.getSimpleName();
    private TinyDB tinyDB;
    private EditText id, pw, domain;
    private AudioManager mAudioManager;
    private DataOutputStream opt;
    private int tempGain;
    private static final String ECHO_BYPASS = "0300";
    private static final String ECHO_SPEAKER_GAIN = "02A0";
    private static final String ECHO_MIC_GAIN = "02B2";
    private static final String ECHO_RESET = "0006";
    private TextView txtSpeaker;
    private Equalizer equalizer;

    static SetEqFragment newInstance() {
        return new SetEqFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragSetEqBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_set_eq, container, false);

        tinyDB = new TinyDB(getContext());

        try {
            equalizer = null;
            equalizer = new Equalizer(0, 0);
            equalizer.setEnabled(true);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

        layout.band1.setProgress(equalizer.getBandLevel((short) 0));
        layout.band2.setProgress(equalizer.getBandLevel((short) 1));
        layout.band3.setProgress(equalizer.getBandLevel((short) 2));
        layout.band4.setProgress(equalizer.getBandLevel((short) 3));
        layout.band5.setProgress(equalizer.getBandLevel((short) 4));

        layout.val1.setText(String.valueOf(layout.band1.getProgress()));
        layout.val2.setText(String.valueOf(layout.band2.getProgress()));
        layout.val3.setText(String.valueOf(layout.band3.getProgress()));
        layout.val4.setText(String.valueOf(layout.band4.getProgress()));
        layout.val5.setText(String.valueOf(layout.band5.getProgress()));

        layout.info1.setText(String.format("%s ~ %s", String.valueOf(equalizer.getBandFreqRange((short) 0)[0]), String.valueOf(equalizer.getBandFreqRange((short) 0)[1])));
        layout.info2.setText(String.format("%s ~ %s", String.valueOf(equalizer.getBandFreqRange((short) 1)[0]), String.valueOf(equalizer.getBandFreqRange((short) 1)[1])));
        layout.info3.setText(String.format("%s ~ %s", String.valueOf(equalizer.getBandFreqRange((short) 2)[0]), String.valueOf(equalizer.getBandFreqRange((short) 2)[1])));
        layout.info4.setText(String.format("%s ~ %s", String.valueOf(equalizer.getBandFreqRange((short) 3)[0]), String.valueOf(equalizer.getBandFreqRange((short) 3)[1])));
        layout.info5.setText(String.format("%s ~ %s", String.valueOf(equalizer.getBandFreqRange((short) 4)[0]), String.valueOf(equalizer.getBandFreqRange((short) 4)[1])));

        layout.band1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                equalizer.setBandLevel((short) 0, (short) (i - 1500));
                layout.val1.setText(String.valueOf(equalizer.getBandLevel((short) 0)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.band2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                equalizer.setBandLevel((short) 1, (short) (i - 1500));
                layout.val2.setText(String.valueOf(equalizer.getBandLevel((short) 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.band3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                equalizer.setBandLevel((short) 2, (short) (i - 1500));
                layout.val3.setText(String.valueOf(equalizer.getBandLevel((short) 2)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.band4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                equalizer.setBandLevel((short) 3, (short) (i - 1500));
                layout.val4.setText(String.valueOf(equalizer.getBandLevel((short) 3)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.band5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                equalizer.setBandLevel((short) 4, (short) (i - 1500));
                layout.val5.setText(String.valueOf(equalizer.getBandLevel((short) 4)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return layout.getRoot();
    }


}