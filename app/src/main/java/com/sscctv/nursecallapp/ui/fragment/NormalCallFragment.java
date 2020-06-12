package com.sscctv.nursecallapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragNormalCallBinding;
import com.sscctv.nursecallapp.pbx.EndPoint;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.pbx.PbxData;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.adapter.AllExtItem;
import com.sscctv.nursecallapp.ui.fragment.adapter.NormalCallAdapter;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.media.AudioManager.STREAM_VOICE_CALL;
import static com.sscctv.nursecallapp.ui.utils.NurseCallUtils.postSync;

public class NormalCallFragment extends Fragment implements IOnBackPressed {
    private static final String TAG = "NormalCallFragment";
    private MainActivity activity;
    private TinyDB tinyDB;
    private NormalCallAdapter normalCallAdapter;
    private Core core;
    private Dialog dialog;
    private ArrayList<AllExtItem> allExtItems;
    private ViewPager pager;
    private ToneGenerator toneGenerator;
    private AudioManager mAudioManager;
    private EditText inNum;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Log.v(TAG, "onAttach()");
        activity = (MainActivity) getActivity();
        core = MainCallService.getCore();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Log.v(TAG, "onDetach()");
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        normalCallAdapter.notifyDataSetChanged();

        inNum.setText("");
//        Log.v(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragNormalCallBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_normal_call, container, false);

        tinyDB = new TinyDB(getContext());

        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("전체보기")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("간호사 스테이션")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("보안 스테이션")));
        layout.tabLayout.addTab((layout.tabLayout.newTab().setText("병리실")));
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
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL);
        float percent = 14f;
        int calVolume = (int) (maxVolume * percent);
        toneGenerator = new ToneGenerator(STREAM_VOICE_CALL, calVolume);
        toneGenerator.stopTone();
        layout.num0.setOnClickListener(view -> {
            layout.intNum.append("0");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);
        });
        layout.num1.setOnClickListener(view -> {
            layout.intNum.append("1");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 300);
        });
        layout.num2.setOnClickListener(view -> {
            layout.intNum.append("2");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 300);

        });
        layout.num3.setOnClickListener(view -> {
            layout.intNum.append("3");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 300);

        });
        layout.num4.setOnClickListener(view -> {
            layout.intNum.append("4");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 300);

        });
        layout.num5.setOnClickListener(view -> {
            layout.intNum.append("5");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 300);

        });
        layout.num6.setOnClickListener(view -> {
            layout.intNum.append("6");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, 300);

        });
        layout.num7.setOnClickListener(view -> {
            layout.intNum.append("7");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_7, 300);

        });
        layout.num8.setOnClickListener(view -> {
            layout.intNum.append("8");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, 300);

        });
        layout.num9.setOnClickListener(view -> {
            layout.intNum.append("9");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_9, 300);

        });
        layout.btnStar.setOnClickListener(view -> {
            layout.intNum.append("*");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_A, 300);
        });
        layout.btnShap.setOnClickListener(view -> {
            layout.intNum.append("#");
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_B, 300);

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

        layout.btnGetExt.setOnClickListener(view -> {
            goSearch();
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

                        if (editable.toString().equals(item.getNum())) {
                            String[] callerId = item.getName().split("-");
                            String model = callerId[0];
                            String ward = callerId[1];
                            String zero = callerId[2];
                            String serial = callerId[3];

                            switch (model) {
                                case "NCTB":
                                    model = "간호사 스테이션";
                                    break;
                                case "NCTS":
                                    model = "보안 스테이션";
                                    break;
                                case "NCTP":
                                    model = "병리실";
                                    break;

                                default:
                                    model = "주수신기";
                                    break;
                            }
                            layout.strNum.setText(String.format("%s병동 %s - %s", ward, model, serial));
                            break;
                        } else {
                            layout.strNum.setText("일치하는 번호가 없습니다");
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

    @Override
    public boolean onBackPressed() {
        NurseCallUtils.sendStatus(getContext(), 0);
        return true;
    }

    private void goSearch() {
        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_login);
        final EditText pw = dialog.findViewById(R.id.dialog_pbx_pw);
        final Button login = dialog.findViewById(R.id.dialog_pbx_login);

        pw.setText("Sscctv12");
        login.setOnClickListener(view1 -> {
            if (pw.getText().toString().isEmpty()) {
                NurseCallUtils.printShort(getContext(), "패스워드가 입력되지 않았습니다.");
                return;
            }

            if (pingTest()) {
                new goLogin().execute();
            } else {
                NurseCallUtils.printShort(getContext(), "네트워크 연결 상태를 확인하세요.");
            }
        });
        dialog.show();

    }

    private class goLogin extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String ip = "175.195.153.235";
            String port = "8088";
            String id = "sscctv";
            String str_pw = "Sscctv12";

            OpenApi mApi = new OpenApi();
            OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
            try {
                mLoginApi.username = id;
                mLoginApi.password = NurseCallUtils.getEncMD5(str_pw);
                mLoginApi.port = "";
                mLoginApi.version = "";
                mLoginApi.url = "";
                mLoginApi.urltag = "0";
            } catch (Exception e) {
                e.printStackTrace();
            }

            String url = EndPoint.PROTOCOL + ip + ":" + port + EndPoint.LOGIN;
            String json = new Gson().toJson(mLoginApi, OpenApi.LoginApi.class);
            return postSync(url, json);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d(TAG, "Success: " + result);
                JsonElement element = new JsonParser().parse(result);
                String result1 = element.getAsJsonObject().get("status").getAsString();
                if (result1.equals("Success")) {
                    PbxData.token = element.getAsJsonObject().get("token").getAsString();
                    dialog.dismiss();
                    new getExtension().execute();

                } else {
                    PbxData.token = null;
                    NurseCallUtils.printShort(getContext(), "실패했습니다.");
                }
            }
        }
    }


    private class getExtension extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);

                if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
                    JsonElement js = element.getAsJsonObject().get("extlist");
                    OpenApi.ExtList[] mExtListArray = new Gson().fromJson(js, OpenApi.ExtList[].class);
                    if (mExtListArray != null) {
                        List<OpenApi.ExtList> items = Arrays.asList(mExtListArray);
                        allExtItems = new ArrayList<>();
                        for (int i = 0; i < items.size(); i++) {
                            final OpenApi.ExtList item = items.get(i);
                            if (compareType(item.type) && compareName(item.username) && compareDevice(item.username)) {
                                Log.d(TAG, "Ext Number: " + item.extnumber + " Name: " + item.username);
                                allExtItems.add(new AllExtItem(item.extnumber, item.username, false));
                            }
                        }

                        NurseCallUtils.putAllExtList(tinyDB, KeyList.KEY_ALL_EXTENSION, allExtItems);
                        NurseCallUtils.putAllExtList(tinyDB, KeyList.KEY_FAVORITE_EXTENSION, new ArrayList<>());

                        if (allExtItems.size() == 0) {
                            errorSearch();
                        }
                    }
                }

                new goLogOut().execute();

            }
        }
    }

    private class goLogOut extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            String url = EndPoint.PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.LOGOUT + "?token=" + PbxData.token;
            return postSync(url, "");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                JsonElement element = new JsonParser().parse(result);
                String str = element.getAsJsonObject().get("status").getAsString();
                Log.w(TAG, "Result: " + str);
                if (str.equals("Success")) {
                    pager.setCurrentItem(0);
                    ((NormalViewAll) normalCallAdapter.listFragment.get(0)).refresh();
                    NurseCallUtils.printShort(getContext(), "주소 연동이 완료되었습니다.");
                } else {
                    NurseCallUtils.printShort(getContext(), "주소 연동을 실패했습니다.");
                }
            }
        }
    }

    private boolean compareType(String type) {
        return type.equals("SIP");
    }

    private boolean compareName(String item) {
        return item.contains("-");
    }

    private boolean compareDevice(String name) {
        if (name.contains(KeyList.MODEL_TELEPHONE_MASTER) || name.contains(KeyList.MODEL_TELEPHONE_SECURITY)
                || name.contains(KeyList.MODEL_TELEPHONE_PUBLIC) || name.contains(KeyList.MODEL_TELEPHONE_OPERATION)) {
            return true;
        }
        return false;
    }

    private boolean pingTest() {
        Runtime runtime = Runtime.getRuntime();

        String host = "175.195.153.235";
        String cmd = "ping -c 1 -W 3 " + host;

        Process process = null;

        try {
            process = runtime.exec(cmd);
        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        try {
            assert process != null;
            process.waitFor();
        } catch (InterruptedException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        int result = process.exitValue();

        return result == 0;
    }

    private void errorSearch() {
        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_error);
        final TextView error = dialog.findViewById(R.id.dialog_txt_pbx_error);
        final Button close = dialog.findViewById(R.id.dialog_pbx_close);
        close.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
        dialog.show();

    }
}
