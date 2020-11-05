package com.sscctv.nursecallapp.ui.settings;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
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
import com.sscctv.nursecallapp.databinding.FragSetAudioBinding;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static android.media.AudioManager.STREAM_ALARM;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_SYSTEM;
import static android.media.AudioManager.STREAM_VOICE_CALL;


public class SetAudioFragment extends Fragment {

    private static final String TAG = SetAudioFragment.class.getSimpleName();
    private static final int[] MAX_STREAM_VOLUME = null;
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
    private FragSetAudioBinding mBinding;

    static SetAudioFragment newInstance() {
        return new SetAudioFragment();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_set_audio, container, false);

        tinyDB = new TinyDB(getContext());
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));

        Log.d(TAG, "Get Speaker Volume: " + tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));
        mBinding.barSpeaker.setMax(30);
        mBinding.barSpeaker.setProgress(tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));
        mBinding.barSpeaker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, i);
                mAudioManager.setStreamVolume(STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Log.d(TAG, "Get Headset Volume: " + tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME));
        mBinding.barHeadset.setMax(15);
        mBinding.barHeadset.setProgress(tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME));
        mBinding.barHeadset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.barRing.setMax(mAudioManager.getStreamMaxVolume(STREAM_RING));
        mBinding.barRing.setProgress(tinyDB.getInt(KeyList.KEY_RING_VOLUME));
        mBinding.barRing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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



//        /* 에코 캔슬러 동작 부분
        GpioPortSet();

        mBinding.barSpGain.setMax(3);
        int gain = calSpeakerGain(getSpeakerGain());
        mBinding.barSpGain.setProgress(gain);
        switch (gain) {
            case 0:
                mBinding.txtSpGain.setText("0.25x");
                break;
            case 1:
                mBinding.txtSpGain.setText("0.33x");
                break;
            case 2:
                mBinding.txtSpGain.setText("0.50x");
                break;
            case 3:
                mBinding.txtSpGain.setText("1.00x");
                break;
        }
        mBinding.barSpGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tempGain = i;
                switch (i) {
                    case 0:
                        mBinding.txtSpGain.setText("0.25x");
                        break;
                    case 1:
                        mBinding.txtSpGain.setText("0.33x");
                        break;
                    case 2:
                        mBinding.txtSpGain.setText("0.50x");
                        break;
                    case 3:
                        mBinding.txtSpGain.setText("1.00x");
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSpeakerGain(tempGain);

            }
        });

        mBinding.barMicGain.setMax(8);
        int mGain = calMicGain(getMicGain());
        mBinding.barMicGain.setProgress(mGain);
        mBinding.txtMicGain.setText(String.valueOf(mGain));
        mBinding.barMicGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tempGain = i;
                mBinding.txtMicGain.setText(String.valueOf(i) + ".");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setMicGain(tempGain);

            }
        });


        mBinding.settingsVolume4.setOnClickListener(view -> {
            if (mBinding.txtResult2.getText().toString().equals("▲")) {
                mBinding.txtResult2.setText("▼");
                mBinding.settingsVolume5.setVisibility(View.VISIBLE);
                if (getEchoBypass().equals("C0 8")) {
                    mBinding.btnBypass.setText("OFF");
                    mBinding.btnBypass.setBackgroundResource(R.drawable.button_red_ra10);
                } else {
                    mBinding.btnBypass.setText("ON");
                    mBinding.btnBypass.setBackgroundResource(R.drawable.button_blue_ra10);
                }
            } else {
                mBinding.txtResult2.setText("▲");
                mBinding.settingsVolume5.setVisibility(View.INVISIBLE);
            }
        });

        mBinding.btnBypass.setOnClickListener(view -> {
            if (getEchoBypass().equals("C0 8")) {
                mBinding.btnBypass.setText("ON");
                mBinding.btnBypass.setBackgroundResource(R.drawable.button_blue_ra10);

                try {
                    opt.writeBytes("echo 0300 c00a > /proc/hbi/dev_145/write_reg\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, true);

            } else {
                mBinding.btnBypass.setText("OFF");
                mBinding.btnBypass.setBackgroundResource(R.drawable.button_red_ra10);

                try {
                    opt.writeBytes("echo 0300 c008 > /proc/hbi/dev_145/write_reg\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, false);

            }
        });

        mBinding.settingsVolume4.setVisibility(View.VISIBLE);
//        */
//        Log.d(TAG, "Bypass: " + getEchoBypass() + " Speaker: " + getSpeakerGain() + " Mic: " + getMicGain());
        return mBinding.getRoot();
    }


    private void GpioPortSet() {
        try {
            Runtime command = Runtime.getRuntime();
            Process proc;

            proc = command.exec("su");
            opt = new DataOutputStream(proc.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException();
        }
    }

    private void readyEcho(String val) {
        try {
            opt.writeBytes("echo " + val + " 2 > /proc/hbi/dev_145/read_reg\n");
            Thread.sleep(300);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private String getEchoReadReg() {
        String line;
        String reLine = null;
        try {
            Process p = Runtime.getRuntime().exec("cat /proc/hbi/dev_145/read_reg\n");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                reLine = line;
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (reLine != null) {
            return reLine.trim().replaceAll("\\s+", " ");
        } else {
            return "Failed";
        }
    }

    private String getEchoBypass() {
        readyEcho(ECHO_BYPASS);
        return getEchoReadReg();
    }

    private String getSpeakerGain() {
        readyEcho(ECHO_SPEAKER_GAIN);
        return getEchoReadReg();
    }

    private int calSpeakerGain(String gain) {

        switch (gain) {
            case "C0 0":
                return 0;
            case "C0 5":
                return 1;
            case "C0 A":
                return 2;
            case "C0 F":
                return 3;
        }
        return 0;
    }

    private String getMicGain() {
        readyEcho(ECHO_MIC_GAIN);
        return getEchoReadReg();
    }

    private int calMicGain(String gain) {
        Log.d(TAG, gain);
        switch (gain) {
            case "0 0":
                return 0;
            case "0 1":
                return 1;
            case "0 2":
                return 2;
            case "0 3":
                return 3;
            case "0 4":
                return 4;
            case "0 5":
                return 5;
            case "0 6":
                return 6;
            case "0 7":
                return 7;

        }
        return 0;
    }

    private void setSpeakerGain(int i) {
        String val = "C00F";

        switch (i) {
            case 0:
                val = "C000";
                break;
            case 1:
                val = "C005";
                break;
            case 2:
                val = "C00A";
                break;
            case 3:
                val = "C00F";
                break;
        }

        try {
            opt.writeBytes("echo " + ECHO_SPEAKER_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        tinyDB.putString(KeyList.ECHO_FRONT_SPEAKER, val);
        resetEcho();
    }

    private void setMicGain(int i) {
        String val = String.format("%04d", i);

        try {
            Log.d(TAG,"echo " + ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n" );
            opt.writeBytes("echo " + ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        tinyDB.putString(KeyList.ECHO_FRONT_MIC, val);

        resetEcho();
    }

    private void resetEcho() {
        try {
            opt.writeBytes("echo " + ECHO_RESET + " 0002 > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}