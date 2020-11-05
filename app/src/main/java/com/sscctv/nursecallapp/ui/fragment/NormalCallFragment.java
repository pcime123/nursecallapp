package com.sscctv.nursecallapp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.AllExtItem;
import com.sscctv.nursecallapp.databinding.FragNormalCallBinding;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.NormalCallAdapter;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.media.AudioManager.STREAM_ALARM;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class NormalCallFragment extends Fragment {
    private static final String TAG = "NormalCallFragment";
    private MainActivity activity;
    private TinyDB tinyDB;
    private NormalCallAdapter normalCallAdapter;
    private Core core;
    private Dialog dialog;
    private ArrayList<AllExtItem> allExtItems, listExtItems;
    private ViewPager pager;
    private ToneGenerator toneGenerator;
    private AudioManager mAudioManager;
    private EditText inNum;
    private String passWord;
    private boolean isFirst;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        core = MainCallService.getCore();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = true;
        normalCallAdapter.notifyDataSetChanged();
        passWord = null;
        inNum.setText("");
    }

    @Override
    public void onPause() {
        isFirst = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (toneGenerator != null) {
            toneGenerator.stopTone();
            toneGenerator.release();
            toneGenerator = null;
        }
        super.onDestroy();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragNormalCallBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_normal_call, container, false);

        tinyDB = new TinyDB(getContext());

        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("전체보기")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("간호사용")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("보안용")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("기타")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("즐겨찾기")));
        layout.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        assert getFragmentManager() != null;

        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(new NormalViewAll());
        listFragments.add(new NormalViewBasic());
        listFragments.add(new NormalViewSecurity());
        listFragments.add(new NormalViewPathology());
        listFragments.add(new NormalViewMark());

        normalCallAdapter = new NormalCallAdapter(getChildFragmentManager(), listFragments);
        pager = layout.viewPager;
        layout.viewPager.setAdapter(normalCallAdapter);
        layout.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(layout.tabLayout));
        layout.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                Log.e(TAG, "Select: " + tab.getPosition());

                layout.viewPager.setCurrentItem(tab.getPosition());
//                normalCallAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        inNum = layout.intNum;
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));
//        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_MUSIC);
//        float percent = 14f;
//        int calVolume = (int) (maxVolume * percent);
        toneGenerator = new ToneGenerator(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));

        layout.num0.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("0");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
                return true;
            } else {
                toneGenerator.stopTone();
                return true;

            }
        });
        layout.num1.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("1");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_1);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });
        layout.num2.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("2");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_2);
            } else {
                toneGenerator.stopTone();

            }
            return true;
        });
        layout.num3.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("3");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_3);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });
        layout.num4.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("4");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_4);
            } else {
                toneGenerator.stopTone();

            }
            return true;
        });
        layout.num5.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("5");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_5);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });
        layout.num6.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("6");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_6);
                return true;
            } else {
                toneGenerator.stopTone();
                return true;

            }
        });
        layout.num7.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("7");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_7);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });
        layout.num8.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("8");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_8);
                return true;
            } else {
                toneGenerator.stopTone();
                return true;

            }
        });
        layout.num9.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("9");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_9);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });
        layout.btnStar.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("*");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_A);
                return true;
            } else {
                toneGenerator.stopTone();
                return true;

            }
        });
        layout.btnShap.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.intNum.append("#");
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_B);
            } else {
                toneGenerator.stopTone();
            }
            return false;
        });

        layout.goCall.setOnClickListener(view -> {
            if (layout.intNum.getText().length() > 0) {
                Core core = MainCallService.getCore();
                newOutgoingCall(layout.intNum.getText().toString());
                if (core.getCurrentCall() == null) {
                    NurseCallUtils.printShort(getContext(), "failed");
                }
            }
        });


        layout.backspace.setOnClickListener(view -> {
            int len = layout.intNum.getText().length();
            if (len > 0) {
                layout.intNum.getText().delete(len - 1, len);
            }
        });

        layout.backspace.setOnLongClickListener(view -> {
            int len = layout.intNum.getText().length();
            if (len > 0) {
                layout.intNum.getText().delete(0, len);
            }
            return true;
        });

        layout.intNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.d(TAG, "onTextChanged: " + i + ", " + i1 + ", " + i2);

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                Log.d(TAG, "After: " + editable.toString());
                if (editable.length() > 3) {
                    ArrayList<AllExtItem> temp = NurseCallUtils.getAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION);
                    for (int i = 0; i < temp.size(); i++) {
                        AllExtItem item = temp.get(i);

                        if (item.getName().contains("-")) {
                            if (editable.toString().equals(item.getNum())) {
                                String[] callerId = item.getName().split("-");
                                if (callerId.length == 4) {

                                    String model = callerId[0];
                                    String ward = callerId[1];
                                    String zero = callerId[2];
                                    String serial = callerId[3];
                                    Log.d(TAG, "Model: " + model);
                                    switch (model) {
                                        case "NCTB":
                                            model = "간호사 스테이션";
                                            layout.strNum.setText(String.format("%s병동 %s - %s", ward, model, serial));
                                            break;
                                        case "NCTS":
                                            model = "보안 스테이션";
                                            layout.strNum.setText(String.format("%s병동 %s - %s", ward, model, serial));
                                            break;
                                        case "NCTP":
                                            model = "병리실";
                                            layout.strNum.setText(String.format("%s병동 %s - %s", ward, model, serial));
                                            break;
                                        case "NCPB":
                                            model = "간호사 호출기";
                                            layout.strNum.setText(String.format("%s병동 %s호실 %s병상 %s", ward, zero.replaceAll("M", ""), serial, model));
                                            break;
                                        default:
                                            model = "주수신기";
                                            layout.strNum.setText(String.format("%s병동 %s - %s", ward, model, serial));
                                            break;
                                    }
                                    break;
                                }else {
                                    layout.strNum.setText(item.getName());
                                }
                            } else {
                                layout.strNum.setText("일치하는 번호가 없습니다");
                            }
                        } else {
                            layout.strNum.setText("PBX에 등록되지 않은 번호입니다.");

                        }

                    }
                } else {
                    layout.strNum.setText("");
                }

            }
        });

        return layout.getRoot();
    }


    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            NurseCallUtils.printShort(getContext(), "Address null");
        }
    }

    public void newOutgoingCall(String to) {
        if (to == null) return;
        Address address = core.interpretUrl(to);
        inviteAddress(address);
    }

}
