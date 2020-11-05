package com.sscctv.nursecallapp.ui.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.DpItem;
import com.sscctv.nursecallapp.databinding.FragSetDpBoardBinding;
import com.sscctv.nursecallapp.service.CallDisplayClient;
import com.sscctv.nursecallapp.ui.settings.adapter.AdapterSetDpBoard;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.seeeyes.nursecallapp.calldisplay.Location;


public class SetDpBoardFragment extends Fragment {

    private static final String TAG = SetDpBoardFragment.class.getSimpleName();
    private final int gRPC_Port = 50054;
    private final int tcpPort = 59009;
    private final String STRING_SPLIT = "_@#@_";
    private TinyDB tinyDB;
    private FragSetDpBoardBinding mBinding;
    private ArrayList<DpItem> dpItems;
    private boolean isRunning;
    private Timer timer;
    private TimerTask timerTask;
    private AdapterSetDpBoard mainAdapter, dialogAdapter;
    private Dialog dialog;
    private Handler handler = new Handler();
    private static int percent;
    private ArrayList<DpItem> tempList;
    private String result;

    static SetDpBoardFragment newInstance() {
        return new SetDpBoardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        isRunning = true;

        dpItems = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);
        mainAdapter = new AdapterSetDpBoard(getContext(), dpItems);
        mainAdapter.setChkVisible(false);
        mBinding.dpList.setAdapter(mainAdapter);

        TaskDpServer(new DpServer());
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
        new Thread(new DiscoveryClient("stop")).start();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_set_dp_board, container, false);

        tinyDB = new TinyDB(getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.dpList.setLayoutManager(layoutManager);
        dialog = new Dialog(Objects.requireNonNull(getContext()));


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mBinding.dpList.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.recycler_divider_gray)));
        mBinding.dpList.addItemDecoration(dividerItemDecoration);

        mBinding.dpBtnSetup.setOnClickListener(view -> netSetup());

        mBinding.btnAddAuto.setOnClickListener(view -> {
            isRunning = true;
            autoSearch();
        });

        mBinding.dpBtnLocation.setOnClickListener(view -> {
            if (netCheck(mainAdapter.getSelectData())) {
                btnLocationSetup(mainAdapter.getSelectData());
            } else {
                NurseCallUtils.printShort(getContext(), "전광판 IP 주소를 확인해주세요.");
            }
        });

        mBinding.dpBtnNetwork.setOnClickListener(view -> {
            if (netCheck(mainAdapter.getSelectData())) {
                btnNetworkSetup(mainAdapter.getSelectData());
            } else {
                NurseCallUtils.printShort(getContext(), "전광판 IP 주소를 확인해주세요.");
            }
        });

        mBinding.dpBtnPhrase.setOnClickListener(view -> {
            if (netCheck(mainAdapter.getSelectData())) {
                btnPhraseSetup(mainAdapter.getSelectData());
            } else {
                NurseCallUtils.printShort(getContext(), "전광판 IP 주소를 확인해주세요.");
            }
        });

        mBinding.dpBtnTime.setOnClickListener(view -> {
//            netCheck(mainAdapter.getSelectData());
//
//            String ip = mainAdapter.getSelectData().getIpAddr();
//            CallDisplayClient client = new CallDisplayClient(ip, gRPC_Port);
//            client.setTime();
//            client.shutdown();
        });

        mBinding.dpBtnDel.setOnClickListener(view -> {

            ArrayList<DpItem> delList = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

            if (delList.size() == 0) {
                NurseCallUtils.printShort(getContext(), "등록된 장비가 없습니다.");
                return;
            }

            delList.remove(mainAdapter.getSelectPosition());
            NurseCallUtils.putDpList(tinyDB, KeyList.KEY_DP_LIST, delList);
            mBinding.dpList.setAdapter(new AdapterSetDpBoard(getContext(), delList));
            Log.d(TAG, "Position: " + mainAdapter.getSelectPosition());
        });

        return mBinding.getRoot();
    }

    private boolean netCheck(DpItem item) {
        if (mainAdapter.getItemCount() == 0) {
            return false;
        }
        return pingTest(item.getIpAddr());
    }

    @SuppressLint("StaticFieldLeak")
    private class listPingCheck extends AsyncTask<Void, String, Void> {


        protected Void doInBackground(Void... params) {
            while (true) {
                ArrayList<DpItem> dpItems = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

                for (int i = 0; i < dpItems.size(); i++) {

                    if (pingTest(dpItems.get(i).getIpAddr())) {
                        publishProgress("ok", dpItems.get(i).getMacAddr());
                    } else {
                        publishProgress("fail", dpItems.get(i).getMacAddr());
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... strings) {

        }
    }

    private void TaskDpServer(DpServer asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class DpServer extends AsyncTask<Void, String, String> {

        protected String doInBackground(Void... params) {
            try {
                Log.w(TAG, "<< ...Discovery Server running... >>");
                MulticastSocket socket = new MulticastSocket(1235);
                InetAddress address = InetAddress.getByName(tinyDB.getString(KeyList.KEY_MULTICAST_IP));
                socket.joinGroup(address);

                tempList = new ArrayList<>();

                while (isRunning) {
                    byte[] buffer = new byte[100];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    publishProgress(msg);
                    Log.d(TAG, "Receive Message: " + msg);
                    Thread.sleep(500);
                }

                Log.w(TAG, "<< ...Discovery Server Stop... >>");

                socket.leaveGroup(address);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            getListUpdate(values[0]);
//            mBinding.btnAddAuto.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.w(TAG, "<< ...Discovery Service End... >>");
        }
    }

    private void getListUpdate(String str) {
        String[] val = str.split(NurseCallUtils.STRING_SPLIT);
        Log.d(TAG, "List: " + str);
        if (val[0].equals("get")) {

            String model = val[1];
            String mac = transMacAddr(val[2].toUpperCase());
            String ip = val[3];
            if (val.length > 4) {
                String location = val[4];
                tempList.add(new DpItem(false, model, ip, mac, location));
            } else {
                tempList.add(new DpItem(false, model, ip, mac, ""));

            }

            for (int i = 0; i < tempList.size(); i++) {
                Log.d(TAG, "TempList Ip: " + tempList.get(i).getIpAddr());
            }
        } else if (val[0].equals("ok")) {
            Log.d(TAG, "SUCCESS: " + val[0]);
            String mac = transMacAddr(val[1].toUpperCase());
            String ip = val[2];
            ArrayList<DpItem> updateList = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

            for (int i = 0; i < updateList.size(); i++) {
                DpItem item = updateList.get(i);

                Log.d(TAG, "List MAC: " + item.getMacAddr() + " In MAC: " + mac);
                if (item.getMacAddr().equals(mac)) {
                    Log.d(TAG, "List IP: " + item.getIpAddr());
                    item.setIpAddr(ip);
                    Log.d(TAG, "List SET IP: " + item.getIpAddr());
                }
            }

            Log.d(TAG, "Complete IP: " + updateList.get(0).getIpAddr());
            mainAdapter = new AdapterSetDpBoard(getContext(), updateList);
            mBinding.dpList.setAdapter(mainAdapter);
            NurseCallUtils.putDpList(tinyDB, KeyList.KEY_DP_LIST, updateList);
            mainAdapter.notifyDataSetChanged();

        }

    }

    private String transMacAddr(String mac) {

        if (tinyDB.getBoolean(KeyList.KEY_DP_DEMO)) {
            return mac.toUpperCase();
        }
        if (mac.length() != 12) {
            return "MAC ERROR!";
        }

        String mac1 = mac.substring(0, 2) + ":";
        String mac2 = mac.substring(2, 4) + ":";
        String mac3 = mac.substring(4, 6) + ":";
        String mac4 = mac.substring(6, 8) + ":";
        String mac5 = mac.substring(8, 10) + ":";
        String mac6 = mac.substring(10, 12);

        return mac1 + mac2 + mac3 + mac4 + mac5 + mac6;
    }


    private void autoSearch() {

        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_auto);
        dialog.setCanceledOnTouchOutside(false);
        final RecyclerView list = dialog.findViewById(R.id.dg_list_auto);
        final LinearLayout layoutProgress = dialog.findViewById(R.id.dg_layout_progress);
        final ProgressBar progressBar = dialog.findViewById(R.id.dg_progress_auto);
        final Button apply = dialog.findViewById(R.id.dg_btn_auto_add);
        final Button search = dialog.findViewById(R.id.dg_btn_search);
        final Button close = dialog.findViewById(R.id.dg_btn_close);

        // RecyclerView Setup
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);

        list.setAdapter(new AdapterSetDpBoard(getContext(), new ArrayList<>()));

        // ProgressBar Setup
        layoutProgress.setVisibility(View.GONE);
        Random random = new Random();
        search.setOnClickListener(view -> new Thread(() -> {

            handler.post(() -> {
                tempList = new ArrayList<>();

                percent = 0;
                dialogAdapter = new AdapterSetDpBoard(getContext(), new ArrayList<>());
                list.setAdapter(dialogAdapter);

                if (layoutProgress.getVisibility() == View.GONE) {
                    layoutProgress.setVisibility(View.VISIBLE);
                    apply.setEnabled(false);
                    search.setEnabled(false);
                }
            });

            try {
                for (int i = 0; i < 5; i++) {
                    if (!isRunning) break;

                    new Thread(new DiscoveryClient("get")).start();
                    Thread.sleep(1000);
                    percent += random.nextInt(20) + 10;
                    handler.post(() -> progressBar.setProgress(percent));

                }


                handler.post(() -> {
                    if (layoutProgress.getVisibility() == View.VISIBLE) {
                        layoutProgress.setVisibility(View.GONE);
                        progressBar.setProgress(0);
                        apply.setEnabled(true);
                        search.setEnabled(true);
                    }

                    dialogAdapter = new AdapterSetDpBoard(getContext(), compareList(tempList));
                    dialogAdapter.setChkVisible(true);
                    list.setAdapter(dialogAdapter);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start());

        apply.setOnClickListener(view -> {
            Log.d(TAG, "Run Apply..");
            ArrayList<DpItem> applyList = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

            ArrayList<DpItem> selList = dialogAdapter.getSelectedItem();
            for (int i = 0; i < selList.size(); i++) {
                DpItem item = selList.get(i);
                Log.d(TAG, item.getIpAddr() + " ," + item.getMacAddr());
                applyList.add(new DpItem(false, item.getModel(), item.getIpAddr(), item.getMacAddr(), item.getLocation()));
            }

            for (int a = 0; a < applyList.size(); a++) {
                Log.d(TAG, applyList.get(a).getMacAddr());
            }

            NurseCallUtils.putDpList(tinyDB, KeyList.KEY_DP_LIST, applyList);
            mainAdapter = new AdapterSetDpBoard(getContext(), applyList);
            mBinding.dpList.setAdapter(mainAdapter);
            dialog.dismiss();
        });


        close.setOnClickListener(view1 -> {
            stopClient();
//            isRunning = false;
//            new Thread(new DiscoveryClient("stop")).start();
            dialog.dismiss();
        });


        dialog.show();
    }

    private void stopClient() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;

        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private ArrayList<DpItem> compareList(ArrayList<DpItem> inList) {

        ArrayList<DpItem> dpList = new ArrayList<>();
        Set<String> macList = new HashSet<>();

        if (inList != null) {
            for (DpItem item : inList) {
                Log.d(TAG, "Compare: " + item.getMacAddr());
                if (macList.add(item.getMacAddr())) {
                    dpList.add(item);
                }
            }
        }
//        putDpList(tinyDB, KeyList.KEY_DP_LIST, dpList);
        return dpList;
    }

    private void netSetup() {

        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dp_setup);

        final EditText ip = dialog.findViewById(R.id.dp_txt_ip);
        final EditText port = dialog.findViewById(R.id.dp_txt_port);
        final Button ping = dialog.findViewById(R.id.dp_btn_ping);
        final Button apply = dialog.findViewById(R.id.dp_btn_apply);
        final CheckBox chk = dialog.findViewById(R.id.dg_dp_chk);

        ip.setText(tinyDB.getString(KeyList.KEY_MULTICAST_IP));
        port.setText(tinyDB.getString(KeyList.KEY_MULTICAST_PORT));
        chk.setChecked(tinyDB.getBoolean(KeyList.KEY_DP_DEMO));

        ping.setOnClickListener(view -> {
            if (pingTest(ip.getText().toString())) {
                NurseCallUtils.printShort(getContext(), "PING 테스트 성공!");
            } else {
                NurseCallUtils.printShort(getContext(), "PING 테스트 실패: IP 주소를 다시 확인해주세요.");
            }
        });

        apply.setOnClickListener(view -> {
            tinyDB.putString(KeyList.KEY_MULTICAST_IP, ip.getText().toString());
            tinyDB.putString(KeyList.KEY_MULTICAST_PORT, port.getText().toString());
            tinyDB.putBoolean(KeyList.KEY_DP_DEMO, chk.isChecked());
            dialog.dismiss();
        });
        dialog.show();
    }

    private void btnNetworkSetup(DpItem item) {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dp_network);

        final TextView curIp = dialog.findViewById(R.id.dg_dp_cur_ip);
        final TextView curMac = dialog.findViewById(R.id.dg_dp_cur_mac);

        final EditText ip = dialog.findViewById(R.id.dp_net_edit_ip);
        final EditText mask = dialog.findViewById(R.id.dp_net_edit_mask);
        final EditText gate = dialog.findViewById(R.id.dp_net_edit_gw);

        final Button auto = dialog.findViewById(R.id.dp_net_btn_auto);

        final Button apply = dialog.findViewById(R.id.dg_net_btn_apply);
        final Button close = dialog.findViewById(R.id.dg_net_btn_close);

        curIp.setText(item.getIpAddr());
        curMac.setText(item.getMacAddr());

        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) + destTxt.substring(dend);
                if (!resultingTxt.matches("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (int i = 0; i < splits.length; i++) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        ip.setFilters(filters);
        mask.setFilters(filters);
        gate.setFilters(filters);

        auto.setOnClickListener(view -> {
            if (isIpAddress(ip.getText().toString())) {

                String[] strIp = ip.getText().toString().split("\\.");
                String ip1 = strIp[0];
                String ip2 = strIp[1];
                String ip3 = strIp[2];

                mask.setText("255.255.255.0");
                gate.setText(String.format("%s.%s.%s.1", ip1, ip2, ip3));
            } else {
                NurseCallUtils.printShort(getContext(), "IP 주소를 확인해주세요.");
            }
        });

        apply.setOnClickListener(view -> {
            String sIp, sMask, sGate;
            if (isIpAddress(ip.getText().toString())) {
                sIp = ip.getText().toString();
            } else {
                NurseCallUtils.printShort(getContext(), "IP 주소를 다시 확인해주세요.");
                return;
            }

            if (isIpAddress(mask.getText().toString())) {
                sMask = mask.getText().toString();
            } else {
                NurseCallUtils.printShort(getContext(), "서브넷 마스크를 다시 확인해주세요.");
                return;
            }

            if (isIpAddress(gate.getText().toString())) {
                sGate = gate.getText().toString();
            } else {
                NurseCallUtils.printShort(getContext(), "게이트웨이를 다시 확인해주세요.");
                return;
            }

            Log.d(TAG, "LAST " +
                    ".MAC: " + item.getMacAddr());
            String result = "set" + STRING_SPLIT + item.getMacAddr() + STRING_SPLIT + sIp + STRING_SPLIT + sMask + STRING_SPLIT + sGate;
            new Thread(new DiscoveryClient(result)).start();

            dialog.dismiss();
        });

        close.setOnClickListener(view -> {

            dialog.dismiss();
        });

        dialog.show();
    }

    private void btnLocationSetup(DpItem item) {

        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dp_location);

        final TextView curLocation = dialog.findViewById(R.id.dg_dp_cur_location);
        final EditText inputLocation = dialog.findViewById(R.id.dp_loc_edit);

        final Button apply = dialog.findViewById(R.id.dg_loc_btn_apply);
        final Button close = dialog.findViewById(R.id.dg_loc_btn_close);

        curLocation.setText(item.getLocation());

        final OutputStream[] os = {null};
        final InputStream[] is = {null};

        apply.setOnClickListener(view -> {
            if (!tinyDB.getBoolean(KeyList.KEY_DP_DEMO)) {
                CallDisplayClient client = new CallDisplayClient(item.getIpAddr(), gRPC_Port);
                Location res = client.getLocation(inputLocation.getText().toString());
                client.shutdown();

                String result = res.getLoc();
                ArrayList<DpItem> updateList = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

                for (int i = 0; i < updateList.size(); i++) {
                    DpItem items = updateList.get(i);

                    if (items.getMacAddr().equals(item.getMacAddr())) {
                        items.setLocation(result.replaceAll("res:", ""));
                    }
                }
                mainAdapter = new AdapterSetDpBoard(getContext(), updateList);
                mBinding.dpList.setAdapter(mainAdapter);
                NurseCallUtils.putDpList(tinyDB, KeyList.KEY_DP_LIST, updateList);
                mainAdapter.notifyDataSetChanged();
            } else {
                new Thread(() -> {
                    try {
                        Socket socket = new Socket(item.getIpAddr(), tcpPort);

                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                        out.println("#loc" + NurseCallUtils.STRING_SPLIT + inputLocation.getText().toString());
                        out.flush();

                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String str = in.readLine();
                        Log.d(TAG, "Get Data: " + str);

                        handler.post(() -> {
                            ArrayList<DpItem> updateList = NurseCallUtils.getDpList(tinyDB, KeyList.KEY_DP_LIST);

                            for (int i = 0; i < updateList.size(); i++) {
                                DpItem items = updateList.get(i);

                                if (items.getMacAddr().equals(item.getMacAddr())) {
                                    items.setLocation(str);
                                }
                            }
                            mainAdapter = new AdapterSetDpBoard(getContext(), updateList);
                            mBinding.dpList.setAdapter(mainAdapter);
                            NurseCallUtils.putDpList(tinyDB, KeyList.KEY_DP_LIST, updateList);
                            mainAdapter.notifyDataSetChanged();
                        });

                        in.close();
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            dialog.dismiss();
        });

        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();
    }


    private void btnPhraseSetup(DpItem item) {

        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dp_phrase);

        final int color = 0;
        final TextView getPhrase = dialog.findViewById(R.id.dg_dp_get_phrase);
        final TextView setPhrase = dialog.findViewById(R.id.dg_dp_set_phrase);
        final TextView colorPhrase = dialog.findViewById(R.id.dg_dp_color_phrase);

        final Spinner spinner = dialog.findViewById(R.id.dg_dp_speed_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tinyDB.putInt(KeyList.KEY_DP_STRING_SPEED, adapterView.getSelectedItemPosition());
                Log.d(TAG, "Speed: " + tinyDB.getInt(KeyList.KEY_DP_STRING_SPEED));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String[] str = getResources().getStringArray(R.array.dp_speed);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_normal, R.id.spinnerText, str);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(dataAdapter);

        colorPhrase.setTextColor(tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR));

        final Button apply = dialog.findViewById(R.id.dg_dp_setup);
        final Button ok = dialog.findViewById(R.id.dg_btn_ok);
        final Button cancel = dialog.findViewById(R.id.dg_btn_close);

        final Button btnColorPhrase = dialog.findViewById(R.id.dg_dp_color_btn);
        final LinearLayout layoutColor = dialog.findViewById(R.id.dg_dp_color_layout);
        final ColorPickerView colorPickerView = dialog.findViewById(R.id.dp_colorPicker);
        final Button btnColorOk = dialog.findViewById(R.id.dp_color_ok);
        final Button btnColorCancel = dialog.findViewById(R.id.dp_color_cancel);

        final CheckBox chkPhrase = dialog.findViewById(R.id.dg_dp_chk_phrase);
        final CheckBox chkColor = dialog.findViewById(R.id.dg_dp_chk_color);
        final CheckBox chkSpeed = dialog.findViewById(R.id.dg_dp_chk_speed);

        chkPhrase.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                setPhrase.setText("문구 변경 적용 안함");
            } else {
                setPhrase.setText("");
            }
            setPhrase.setEnabled(!b);
        });
        chkColor.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                colorPhrase.setText("문구 색상 변경 적용 안함");
            } else {
                colorPhrase.setText("색상 변경 확인 문구 입니다.");
            }
            btnColorPhrase.setEnabled(!b);
        });
        chkSpeed.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                spinner.setSelection(5);
            } else {
                spinner.setSelection(tinyDB.getInt(KeyList.KEY_DP_STRING_SPEED));
            }
            spinner.setEnabled(!b);
        });

        chkPhrase.setChecked(false);
        chkColor.setChecked(true);
        chkSpeed.setChecked(true);

        comDpPhrase(getPhrase, item.getIpAddr());

        btnColorPhrase.setOnClickListener(view -> {
            if (layoutColor.getVisibility() == View.GONE) {
                layoutColor.setVisibility(View.VISIBLE);
            }
            if (tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR) == 0) {
                tinyDB.putInt(KeyList.KEY_DP_STRING_COLOR, Color.WHITE);
            }

            colorPickerView.setColor(tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR), true);

            btnColorOk.setOnClickListener(view1 -> {
                tinyDB.putInt(KeyList.KEY_DP_STRING_COLOR, colorPickerView.getColor());
                colorPhrase.setTextColor(colorPickerView.getColor());
                layoutColor.setVisibility(View.GONE);
            });

            btnColorCancel.setOnClickListener(view1 -> layoutColor.setVisibility(View.GONE));
        });


        apply.setOnClickListener(view -> {
            final String strPhrase, strColor, strSpeed;
            final String strResult;
            if (!chkPhrase.isChecked() && setPhrase.getText().toString().isEmpty()) {
                NurseCallUtils.printShort(getContext(), "문구가 입력되지 않았습니다. 변경을 원하지 않으면 우측 체크박스를 활성화 해주세요.");
                return;
            }
            if (chkPhrase.isChecked()) {
                strPhrase = "none";
            } else {
                strPhrase = setPhrase.getText().toString();
            }
            if (chkColor.isChecked()) {
                strColor = "none";
            } else {
                strColor = String.valueOf(tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR));
            }
            if (chkSpeed.isChecked()) {
                strSpeed = "none";
            } else {
                strSpeed = String.valueOf(tinyDB.getInt(KeyList.KEY_DP_STRING_SPEED));
            }

            strResult = "#setup" + NurseCallUtils.STRING_SPLIT + strPhrase + NurseCallUtils.STRING_SPLIT + strColor + NurseCallUtils.STRING_SPLIT
                    + strSpeed;

            new Thread(() -> {
                try {
                    Socket socket = new Socket(item.getIpAddr(), tcpPort);

                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println(strResult);
                    out.flush();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    result = in.readLine();
                    if (result != null) {
                        handler.post(() -> {
                            getPhrase.setText(result);
                        });
                    }
                    in.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        ok.setOnClickListener(view -> {
            final String strPhrase, strColor, strSpeed;
            final String strResult;
            if (!chkPhrase.isChecked() && setPhrase.getText().toString().isEmpty()) {
                NurseCallUtils.printShort(getContext(), "문구가 입력되지 않았습니다. 변경을 원하지 않으면 우측 체크박스를 활성화 해주세요.");
                return;
            }
            if (chkPhrase.isChecked()) {
                strPhrase = "none";
            } else {
                strPhrase = setPhrase.getText().toString();
            }
            if (chkColor.isChecked()) {
                strColor = "none";
            } else {
                strColor = String.valueOf(tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR));
            }
            if (chkSpeed.isChecked()) {
                strSpeed = "none";
            } else {
                strSpeed = String.valueOf(tinyDB.getInt(KeyList.KEY_DP_STRING_SPEED));
            }

            strResult = "#setup" + NurseCallUtils.STRING_SPLIT + strPhrase + NurseCallUtils.STRING_SPLIT + strColor + NurseCallUtils.STRING_SPLIT
                    + strSpeed;

            new Thread(() -> {
                try {
                    Socket socket = new Socket(item.getIpAddr(), tcpPort);

                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println(strResult);
                    out.flush();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    result = in.readLine();
                    if (result != null) {
                        handler.post(() -> {
                            getPhrase.setText(result);
                        });
                    }
                    in.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            dialog.dismiss();

        });
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void comDpPhrase(TextView view, String ip) {

        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, tcpPort);


                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                out.println("#getPhrase");
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                result = in.readLine();
                if (result != null) {
                    handler.post(() -> {
                        view.setText(result);
                    });
                }
                Log.d(TAG, "Get Data: " + result);
                in.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private String setPhraseText(TextView view) {
        return "#setup" + NurseCallUtils.STRING_SPLIT + view.getText().toString() + NurseCallUtils.STRING_SPLIT + tinyDB.getInt(KeyList.KEY_DP_STRING_COLOR) + NurseCallUtils.STRING_SPLIT
                + tinyDB.getInt(KeyList.KEY_DP_STRING_SPEED);
    }

    public static boolean isIpAddress(String ipAddress) {
//        Log.d(TAG, "isIpAddress: " + ipAddress);
        boolean returnValue = false;

        if (ipAddress.isEmpty()) {
            return false;
        }

        String regex = "^([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3})$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(ipAddress);

        if (m.matches()) {
            returnValue = true;
        }
        return returnValue;
    }

    private boolean pingTest(String ip) {
        Runtime runtime = Runtime.getRuntime();

        String cmd = "ping -c 1 -W 1 " + ip;

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


}
