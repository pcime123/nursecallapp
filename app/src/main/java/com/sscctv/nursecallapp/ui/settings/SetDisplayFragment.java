package com.sscctv.nursecallapp.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragSetDisplayBinding;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class SetDisplayFragment extends Fragment {

    private static final String TAG = SetDisplayFragment.class.getSimpleName();
    private TinyDB tinyDB;
    private EditText id, pw, domain;
    private TextView txtSpeaker;

    private Handler sen_light_Handelr;
    private Timer sen_light_Timer;
    private FragSetDisplayBinding mBinding;

    static SetDisplayFragment newInstance() {
        return new SetDisplayFragment();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        sen_light_Timer.cancel();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_set_display, container, false);

        tinyDB = new TinyDB(getContext());

        try {

            mBinding.txtSetDpLight.setText(String.format("%s%%", Settings.System.getInt(Objects.requireNonNull(getActivity()).getContentResolver(), "screen_brightness") / 2.5));
            mBinding.barLight.setMax(250);
            mBinding.barLight.setProgress(Settings.System.getInt(getActivity().getContentResolver(), "screen_brightness"));
            mBinding.barLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    changeDisplayLight(i);
                    mBinding.txtSetDpLight.setText(String.format("%s%%", String.valueOf(i / 2.5)));

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

//        Log.d(TAG, layout.modeTime.getSelectedItem().toString());
        mBinding.modeTime.setSelection(tinyDB.getInt(KeyList.SCREEN_CHANGE_POS));
        mBinding.modeTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d(TAG, "" + adapterView.getItemAtPosition(i));
                tinyDB.putInt(KeyList.SCREEN_CHANGE_TIME, calScreenChangeTime(adapterView.getItemAtPosition(i).toString()));
                tinyDB.putInt(KeyList.SCREEN_CHANGE_POS, i);
                NurseCallUtils.sendRefreshTimer(Objects.requireNonNull(getContext()), 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        sen_light_Handelr = new Handler();
        TimerTask hookTask = new TimerTask() {
            @Override
            public void run() {
                sen_light_Handelr.post(
                        () -> {
                            mBinding.val.setText(tinyDB.getString(KeyList.SENSOR_LIGHT));
                        });
            }
        };
        sen_light_Timer = new Timer("Light Sensor");
        sen_light_Timer.schedule(hookTask, 0, 1000);

        mBinding.btnDpBack.setOnClickListener(view -> {
            NurseCallUtils.printShort(getContext(), "현재 지원하지 않습니다.");
        });
        return mBinding.getRoot();
    }

    private int calScreenChangeTime(String val) {
        if (val.contains("초")) {
            return Integer.valueOf(val.replaceAll("초", "")) * 1000;
        } else if (val.contains("분")) {
            return Integer.valueOf(val.replaceAll("분", "")) * 60000;
        } else {
            return 0;
        }
    }

    private void changeDisplayLight(int i) {
        Settings.System.putInt(getActivity().getContentResolver(), "screen_brightness", i);

        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.screenBrightness = i;
        getActivity().getWindow().setAttributes(layoutParams);
    }

}