package com.sscctv.nursecallapp.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.opencsv.CSVReader;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.ActivityCsvExploerBinding;
import com.sscctv.nursecallapp.pbx.OpenApi;
import com.sscctv.nursecallapp.ui.adapter.AdapterScanList;
import com.sscctv.nursecallapp.ui.adapter.ExtItem;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVScanActivity extends AppCompatActivity {
    private static final String TAG = "CSVScanActivity";

    private ActivityCsvExploerBinding mBinding;
    private static int nScanTasksFinished = 0;
    private static ArrayList<CSVScanTask> csvScanTasks = new ArrayList<>();
    public static List<File> scanResults = new ArrayList<File>();
    private int select;
    private String sd, usb, internal;
    private boolean isScanning;
    private File usbPath, sdPath, intPath;
    private BroadcastReceiver mountReceiver;
    private static AdapterScanList scanListAdapter = null;
    private ArrayList<ExtItem> csvList;
    private TinyDB tinyDB;
    private ArrayList<ExtItem> compareArrayList;
    private String searchMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_csv_exploer);
        tinyDB = new TinyDB(this);
        scanListAdapter = new AdapterScanList(this, R.layout.item_scan_list, scanResults);
        mBinding.scanListView.setAdapter(scanListAdapter);
        scanListAdapter.allClear();
        scanListAdapter.notifyDataSetChanged();

        mBinding.scanListView.setOnItemClickListener(((adapterView, view, i, l) ->
        {
            openCSV(scanListAdapter.getItem(i).toString());
            Log.d(TAG, "Click: " + scanListAdapter.getItem(i));
        }
        ));

        mBinding.btnInternal.setOnClickListener(view -> {
            select = 0;
            csvScan(select);
        });

        mBinding.btnSd.setOnClickListener(view -> {
            select = 1;
            csvScan(select);
        });

        mBinding.btnUSB.setOnClickListener(view -> {
            select = 2;
            csvScan(select);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startWatchingMount();
        select = 0;
        csvScan(select);
        compareArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);
        searchMode = tinyDB.getString(KeyList.DEVICE_SEARCH_WARD);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWatchingMount();
    }

    private void openCSV(String path) {

        try {
            CSVReader reader = new CSVReader(new FileReader(path));

            csvList = new ArrayList<>();
            ArrayList<ExtItem> tempList = new ArrayList<>();

            String[] data;
            while ((data = reader.readNext()) != null) {
                if (compareType(data[0]) && compareName(data[2]) && compareDevice(data[2])) {
                    tempList.add(new ExtItem(data[1], data[2], false));
                }
            }

            for (int j = 0; j < tempList.size(); j++) {
                for (int a = 0; a < compareArrayList.size(); a++) {
                    if (tempList.get(j).getNum().equals(compareArrayList.get(a).getNum())) {
                        tempList.remove(j);
                    }
                }
            }

            if (searchMode.equals("ward")) {
                String device_ward = tinyDB.getString(KeyList.DEVICE_WARD);

                for (int i = 0; i < tempList.size();i++) {
                    String[] callerId = tempList.get(i).getName().split("-");
                    String ward = callerId[1];

                    Log.d(TAG, "DEVICE: " + device_ward + " Ward: " + ward);
                    if (device_ward.equals(ward)) {
                        csvList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                    }
                }
            } else {
                csvList = tempList;
            }

            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_GET_EXTENSION, csvList);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
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
            return false;
        }
        return true;
    }


    @SuppressLint("StaticFieldLeak")
    private class CSVScanTask extends AsyncTask<File, File, Boolean> {
        private String csvExtension;

        private CSVScanTask() {
            this.csvExtension = ".csv";
        }

        private boolean recursiveScan(File file) {
            Log.d(TAG, "recursiveScan: " + file);
            if (!isCancelled()) {
                ArrayList<File> arrayList = new ArrayList<>();
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    int length = listFiles.length;
                    int i = 0;
                    while (i < length) {
                        File file2 = listFiles[i];
                        if (!isCancelled()) {
                            if (file2.isDirectory()) {
                                arrayList.add(file2);
                            } else if (file2.getName().endsWith(this.csvExtension)) {
                                publishProgress(file2);
                            }
                            i++;
                        }
                    }
                }
                for (File file1 : arrayList) {
                    try {
                        if (!recursiveScan(file1)) {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            return false;
        }

        protected Boolean doInBackground(File... fileArr) {
            try {
                recursiveScan(fileArr[0]);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return Boolean.TRUE;
        }

        protected void onPostExecute(Boolean bool) {
            nScanTasksFinished = nScanTasksFinished + 1;
            if (nScanTasksFinished >= csvScanTasks.size()) {
                switch (select) {
                    case 0:
                        mBinding.scanning.setText("Internal CSV file found");
                        break;
                    case 1:
                        mBinding.scanning.setText("External SD Card CSV file found");
                        break;
                    case 2:
                        mBinding.scanning.setText("External USB CSV file found");
                        break;

                }
                isScanning = false;
            }
        }

        protected void onPreExecute() {
            mBinding.scanning.setText("Scanning for CSV files...");
            isScanning = true;
        }

        protected void onProgressUpdate(File... fileArr) {
            if (!isCancelled() && !scanResults.contains(fileArr[0])) {
                scanResults.add(fileArr[0]);
                scanListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void csvScan(int select) {
        listClear();
        storageCheck();
        File file = Environment.getExternalStorageDirectory();

        switch (select) {
            case 0:
                file = Environment.getExternalStorageDirectory();
                break;
            case 1:
                file = sdPath;
                break;
            case 2:
                file = usbPath;
                break;
        }

        CSVScanTask csvScanTask = new CSVScanTask();
        csvScanTasks.add(csvScanTask);
        csvScanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
    }

    private void storageCheck() {
        sd = null;
        usb = null;
        try {
            Process p = Runtime.getRuntime().exec("su -c vdc volume list");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String sLine;
            while ((sLine = input.readLine()) != null) {
                if (sLine.equals("110 0 extsd /storage/extsd 4")) {
                    sdPath = new File("/storage/extsd");
                    sd = "/storage/extsd";
                } else if (sLine.equals("110 0 usbhost /storage/usbhost 4")) {
                    usbPath = new File("/storage/usbhost");
                    usb = "/storage/usbhost";
                } else {
                    intPath = Environment.getExternalStorageDirectory();
                    internal = "internal";
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sd == null) {
            mBinding.btnSd.setEnabled(false);
        } else {
            mBinding.btnSd.setEnabled(true);
        }

        if (usb == null) {
            mBinding.btnUSB.setEnabled(false);
        } else {
            mBinding.btnUSB.setEnabled(true);
        }
    }

    private void listClear() {
        scanListAdapter.allClear();
        scanListAdapter.notifyDataSetChanged();
    }

    public void startWatchingMount() {
        if (mountReceiver == null) {
            mountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    storageCheck();
                    if (sd == null && usb != null) {
                        select = 2;
                    } else if (sd != null && usb == null) {
                        select = 1;
                    } else if (sd != null) {
                        select = 1;
                    } else {
                        select = 0;
                    }
                    csvScan(select);
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addDataScheme("file");
            this.registerReceiver(mountReceiver, filter);
        }
    }

    public void stopWatchingMount() {
        if (mountReceiver != null) {
            this.unregisterReceiver(mountReceiver);
        }
    }

}
