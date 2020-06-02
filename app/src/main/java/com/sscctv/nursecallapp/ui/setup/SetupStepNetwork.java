package com.sscctv.nursecallapp.ui.setup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivitySetupNetworkBinding;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupStepNetwork extends AppCompatActivity {

    private static final String TAG = "SetupStepType";

    private ActivitySetupNetworkBinding mBinding;
    private InputMethodManager imm;
    private EthernetManager mEthernetManager;
    private StaticIpConfiguration mStaticIpConfiguration;
    private IpConfiguration mIpConfiguration;
    private int prefixLength;
    private int step = 0;
    private TinyDB tinyDB;

    private String ethMode;
    private InetAddress gatewayAddr;
    private Inet4Address inetAddr;
    private Inet4Address dnsAddr;
    private InetAddress netAddr;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_network);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEthernetManager = (EthernetManager) getSystemService("ethernet");
        tinyDB = new TinyDB(this);

        ipInput();

        Intent intent = getIntent();
        boolean val = intent.getBooleanExtra("first", false);

        mBinding.selectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.btnDhcp) {
                    mBinding.layoutStatic.setVisibility(View.GONE);
                } else if (i == R.id.btnStatic) {
                    mBinding.layoutStatic.setVisibility(View.VISIBLE);
                }
            }
        });

        mBinding.btnNext.setOnClickListener(view -> {
//            if (!connectionCheck()) {
//                mBinding.resultError.setText(R.string.network_disable);
//            } else {
                mBinding.resultError.setText("");
                CheckTypesTask task = new CheckTypesTask();
                task.execute();
//            }

        });

        mBinding.btnPrev.setOnClickListener(view -> finish());
    }

    private void hideKeyboard() {
//        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEthernetManager != null) {
            retrieveInfo();
        }
    }

    private boolean connectionCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void ipInput() {
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
        mBinding.setupIp.setFilters(filters);
        mBinding.setupSubnet.setFilters(filters);
        mBinding.setupGate.setFilters(filters);

    }

    private String getEthMode() {
        String mode = mEthernetManager.getConfiguration().toString();
        Log.d(TAG, "getEthMode: " + mode);
        int iDHCP = mode.indexOf("DHCP");

        if (iDHCP == -1) {
            ethMode = "STATIC";
        } else {
            ethMode = "DHCP";
        }
        return ethMode;
    }

    private String getIpAddress() {
        String str = mEthernetManager.getConfiguration().toString();
        int value = str.indexOf("address ") + 8;
        int value2 = str.indexOf("/");
//        Log.d(TAG, "value: " + value + " value2: " + value2);
        return str.substring(value, value2);
    }

    private String getNetMask() {
        String str = mEthernetManager.getConfiguration().getStaticIpConfiguration().toString();
        int val = str.indexOf("address ") + 8;
        int val1 = str.indexOf(" Gateway");
        String str1 = str.substring(val, val1);
        String[] parts = str1.split("/");
        String ip = parts[0];

        int prefix;
        if (parts.length < 2) {
            prefix = 0;
        } else {
            prefix = Integer.parseInt(parts[1]);
        }

        int mask = 0xffffffff << (32 - prefix);
//        Log.d(TAG, "Prefix: " + prefix);
//        Log.d(TAG, "IP: " + ip);

        int value = mask;
        byte[] bytes = new byte[]{
                (byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};

        try {
            netAddr = InetAddress.getByAddress(bytes);
//            Log.d(TAG, "Mask: " + netAddr.getHostAddress());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return netAddr.getHostAddress();

    }


    private String getGateWay() {
        String str = mEthernetManager.getConfiguration().toString();
        int value = str.indexOf("Gateway ") + 8;
        int value2 = str.indexOf("DNS");
//        Log.d(TAG, "value: " + value + " value2: " + value2);
        String str2 = str.substring(value, value2);
        return str2.replaceAll(" ", "");
    }


    @SuppressLint("StaticFieldLeak")
    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(SetupStepNetwork.this);

        boolean value = false;

        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "Start CheckTypesTask onPreExecute");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();

            killDhcp();
            dnsAddr = getIPv4Address("8.8.8.8");

            if (mBinding.btnDhcp.isChecked()) {
                ethMode = "DHCP";
            } else {
                ethMode = "STATIC";
            }

            if (ethMode.equals("STATIC")) {
//                Log.d(TAG, "CheckTypesTask EthernetMode: STATIC");
                mBinding.btnStatic.setChecked(true);
                try {
                    netcfgEthDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mStaticIpConfiguration = new StaticIpConfiguration();
                if (isIpAddress(mBinding.setupIp.getText().toString())) {
                    inetAddr = getIPv4Address(mBinding.setupIp.getText().toString());
                    value = true;
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_ip), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    value = false;
                    return;
                }
                if (isIpAddress(mBinding.setupSubnet.getText().toString())) {
                    prefixLength = maskStr2InetMask(mBinding.setupSubnet.getText().toString());
                    value = true;
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_mask), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    value = false;
                    return;
                }
                if (isIpAddress(mBinding.setupGate.getText().toString())) {
                    gatewayAddr = getIPv4Address(mBinding.setupGate.getText().toString());
                    value = true;
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_gate), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    value = false;
                    return;
                }

            } else {
//                Log.d(TAG, "CheckTypesTask EthernetMode: DHCP");
                mBinding.btnDhcp.setChecked(true);
                clearData();
                value = true;
                try {
                    dhcpcdDhcp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


//            Log.d(TAG, "Stop CheckTypesTask onPreExecute");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            Log.d(TAG, "Start CheckTypesTask doInBackground");
            if (ethMode.equals("STATIC")) {
                Log.d(TAG, "CheckTypesTask doInBackground: STATIC");
                if (value) {
                    try {
                        netcfgEthUp();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    setStaticIP();
                }
            } else if (ethMode.equals("DHCP")) {
                Log.d(TAG, "CheckTypesTask doInBackground: DHCP");
                if (value) {
                    try {
                        netcfgEthDown();
                        netcfgEthUp();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null);
                    mEthernetManager.setConfiguration(mIpConfiguration);
                }
            }
//            Log.d(TAG, "Stop CheckTypesTask doInBackground");
            return null;
        }

        @Override
        @SuppressLint("DefaultLocale")
        protected void onPostExecute(Void aVoid) {
//            Log.d(TAG, "Start CheckTypesTask onPostExecute");
            step = 0;
            new Thread(() -> {
                while (true) {
                    if (value) {
                        step++;
//                    Log.d(TAG, "CheckTypesTask onPostExecute Step: " + step);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                        }

                        if (getEnableIP()) {
                            try {
                                Thread.sleep(2000);
                                retrieveInfo();
                                progressDialog.dismiss();


                                Intent intent1 = new Intent(SetupStepNetwork.this, SetupStepDevice.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                            break;
                        }

                        if (step == 20) {

                            progressDialog.dismiss();
//                        Log.d(TAG, "CheckTypesTask onPostExecute 20th Failed Network Setup");
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show());
//                            runOnUiThread(SetupStepNetwork.this::clearData);
                            break;
                        }
                    }
                }

            }).start();
//            Log.d(TAG, "Stop CheckTypesTask onPostExecute");
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("SetTextI18n")
    private void retrieveInfo() {
//        Log.d(TAG, "Ethernet Retrieve Information..");
        runOnUiThread(() -> {
            String sValue;
            String dns;
            String mode;

            mode = getEthMode();
            switch (mode) {
                case "DHCP":
                    Log.d(TAG, "DHCP MODE");
                    mBinding.btnDhcp.setChecked(true);
//                    addBtn.setEnabled(false);
//
                    try {
                        Process p = Runtime.getRuntime().exec("getprop");
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((sValue = input.readLine()) != null) {

                            if (sValue.contains("[dhcp.eth0.gateway]:")) {
                                Pattern pDHCPGateway = Pattern.compile("\\[dhcp.eth0.gateway\\]: \\[(.+?)\\]");
                                Matcher m = pDHCPGateway.matcher(sValue);
                                if (m.find()) {
                                    mBinding.setupGate.setText(m.group(1));
                                    Log.d(TAG, "GateWay---------" + m.group(1));
                                }

                            } else if (sValue.contains("[dhcp.eth0.ipaddress]:")) {
                                Pattern pDHCPIPAddress = Pattern.compile("\\[dhcp.eth0.ipaddress\\]: \\[(.+?)\\]");
                                Matcher m = pDHCPIPAddress.matcher(sValue);
                                if (m.find()) {
                                    mBinding.setupIp.setText(m.group(1));
                                    Log.d(TAG, "IP---------" + m.group(1));
                                }
                            } else if (sValue.contains("[dhcp.eth0.mask]:")) {
                                Pattern pDHCPIPAddress = Pattern.compile("\\[dhcp.eth0.mask\\]: \\[(.+?)\\]");
                                Matcher m = pDHCPIPAddress.matcher(sValue);
                                if (m.find()) {
                                    mBinding.setupSubnet.setText(m.group(1));
                                    Log.d(TAG, "Mask---------" + m.group(1));
                                }
                            }
                        }
                        input.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "STATIC":
                    mBinding.btnStatic.setChecked(true);
                    Log.d(TAG, "Static MODE");
                    Log.d(TAG, "IP Address: " + getIpAddress());
                    Log.d(TAG, "Mask Address: " + getNetMask());
                    Log.d(TAG, "GateWay: " + getGateWay());
                    mBinding.setupIp.setText(getIpAddress());
                    mBinding.setupSubnet.setText(getNetMask());
                    mBinding.setupGate.setText(getGateWay());
                    break;

            }
        });
//        Log.d(TAG, "Finish retrieveInfo...");
    }

    private static boolean getEnableIP() {
        String sValue = "";
        boolean bValue;

        try {
            Process p = Runtime.getRuntime().exec("su -c ifconfig eth0");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String sLine;
            while ((sLine = input.readLine()) != null) {
                sValue = sLine;
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bValue = sValue.contains("eth0");
//        Log.d(TAG, "getEnableIP bValue: " + bValue);
        return bValue;
    }

    private void setStaticIP() {
        if (Arrays.toString(inetAddr.getAddress()).isEmpty() || prefixLength == 0 || gatewayAddr.toString().isEmpty() || dnsAddr.toString().isEmpty()) {
            return;
        }

        Class<?> clazz = null;

        try {
            clazz = Class.forName("android.net.LinkAddress");
        } catch (Exception e) {
            e.getMessage();
        }

        Class[] cl = new Class[]{InetAddress.class, int.class};
        Constructor<LinkAddress> cons = null;

        try {
            cons = (Constructor<LinkAddress>) clazz.getConstructor(cl);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Object[] x = {inetAddr, prefixLength};

        String dnsStr2 = "8.8.8.4";

        try {
            mStaticIpConfiguration.ipAddress = cons.newInstance(x);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        mStaticIpConfiguration.gateway = gatewayAddr;
        mStaticIpConfiguration.dnsServers.add(dnsAddr);

        if (!dnsStr2.isEmpty()) {
            mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
        }
        Log.d(TAG, "mStatic: " + mStaticIpConfiguration);

        mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
        mEthernetManager.setConfiguration(mIpConfiguration);

    }

    private void killDhcp() {
        List<String> listPID = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec("ps");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String sLine;

            while ((sLine = input.readLine()) != null) {
                Pattern pattenDHCP = Pattern.compile("dhcp\\s+([0-9]+)\\s.+dhcpcd");
                Matcher m = pattenDHCP.matcher(sLine);
                if (m.find()) {
                    listPID.add(m.group(1));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String sPID : listPID) {
            Log.d(TAG, "kill " + sPID);
            try {
                Process pKillDHCP = Runtime.getRuntime().exec("su -c kill " + sPID);
                BufferedReader inputKillDHCP = new BufferedReader(new InputStreamReader(pKillDHCP.getInputStream()));
                String sLineKillDHCP;
                Log.d(TAG, "su kill " + sPID);
                while ((sLineKillDHCP = inputKillDHCP.readLine()) != null) {
                    Log.d(TAG, sLineKillDHCP);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//    @SuppressLint("StaticFieldLeak")
//    private class pingConnection extends AsyncTask<Void, Void, Void> {
//        ProgressDialog progressDialog = new ProgressDialog(SetupStepType.this);
//        private boolean stat;
//        private String ip;
//        int result;
//
//        @Override
//        protected void onPreExecute() {
//            ip = editIp.getText().toString();
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.setCancelable(false);
//            progressDialog.setMessage("IP 주소 중복을 확인합니다...");
//            progressDialog.show();
//
//
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            Log.d(TAG, "Start pingConnection doInBackground: " + stat);
//                try {
//                    InetAddress pingCheck = InetAddress.getByName(ip);
//                    if (pingCheck.isReachable(1000)) {
//                        stat = false;
//                    } else {
//                        stat = true;
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            return null;
//        }
//
//        @Override
//        @SuppressLint("DefaultLocale")
//        protected void onPostExecute(Void aVoid) {
//            if (!stat) {
//                ipError.setTextColor(getResources().getColor(R.color.tiRed));
//                ipError.setText("동일한 IP 주소가 존재합니다.");
//            } else {
//                ipError.setTextColor(getResources().getColor(R.color.homeMain));
//                ipError.setText("사용 가능합니다.");
//                btnNext.setEnabled(true);
//
//                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(editIp.getWindowToken(), 0);
//            }
//            progressDialog.dismiss();
//            super.onPostExecute(aVoid);
//        }
//    }

    private void clearData() {
        mBinding.setupIp.setText("");
        mBinding.setupSubnet.setText("");
        mBinding.setupGate.setText("");
    }

    public static int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (!pattern.matcher(maskStr).matches()) {
            Log.e(TAG, "subMask is error");
            return 0;
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    public static Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    public static boolean isIpAddress(String ipAddress) {
//        Log.d(TAG, "isIpAddress: " + ipAddress);
        boolean returnValue = false;

        String regex = "^([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3})$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(ipAddress);

        if (m.matches()) {
            returnValue = true;
        }
        return returnValue;
    }

    private void netcfgEthDown() throws IOException {
        Runtime.getRuntime().exec("su -c netcfg eth0 down");
    }

    private void netcfgEthUp() throws IOException {
        Runtime.getRuntime().exec("su -c netcfg eth0 up");

    }

    private void netcfgDhcp() throws IOException {
        Runtime.getRuntime().exec("su -c netcfg eth0 dhcp");
    }

    private void dhcpcdDhcp() throws IOException {
        Runtime.getRuntime().exec("dhcpcd -p eth0");
    }
}
