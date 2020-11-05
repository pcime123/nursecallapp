package com.sscctv.nursecallapp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.opencsv.CSVReader;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.databinding.FragCsvExploerBinding;
import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.fragment.adapter.BedCSVAdapter;
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
import java.util.Objects;

public class BedCSVFragment extends Fragment {

    private static final String TAG = BedCSVFragment.class.getSimpleName();
    private static int nScanTasksFinished = 0;
    private static ArrayList<CSVScanTask> csvScanTasks = new ArrayList<>();
    public static List<File> scanResults = new ArrayList<File>();
    private int select;
    private String sd, usb, internal;
    private boolean isScanning;
    private File usbPath, sdPath, intPath;
    private BroadcastReceiver mountReceiver;
    private static BedCSVAdapter scanListAdapter = null;
    private ArrayList<ExtItem> csvList;
    private TinyDB tinyDB;
    private ArrayList<ExtItem> compareArrayList;
    private String searchMode;
    private Dialog dialog;
    private boolean isWard;
    private TextView txtScan;
    private Button btnSd, btnUsb, btnInter;
    private boolean isSd, isUsb;

    public static BedCSVFragment newInstance() {
        BedCSVFragment fragment = new BedCSVFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach()");
        stopWatchingMount();
        super.onDetach();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        startWatchingMount();
        select = 0;
        csvScan(select);
        compareArrayList = NurseCallUtils.getExtList(tinyDB, KeyList.KEY_CUR_EXTENSION);
        searchMode = tinyDB.getString(KeyList.DEVICE_SEARCH_WARD);
        isWard = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        tinyDB.remove(KeyList.KEY_GET_EXTENSION);

        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragCsvExploerBinding layout = DataBindingUtil.inflate(inflater, R.layout.frag_csv_exploer, container, false);

        tinyDB = new TinyDB(getContext());
        scanListAdapter = new BedCSVAdapter(getContext(), R.layout.item_scan_list, scanResults);
        layout.scanListView.setAdapter(scanListAdapter);
        scanListAdapter.allClear();
        scanListAdapter.notifyDataSetChanged();

        layout.btnPrev.setOnClickListener(view -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedAddFragment.newInstance());
        });

        layout.scanListView.setOnItemClickListener(((adapterView, view, i, l) ->
                goSearch(i)
        ));

        layout.btnInternal.setOnClickListener(view -> {
            select = 0;
            csvScan(select);
        });

        txtScan = layout.txtScan;

        btnSd = layout.btnSd;
        btnUsb = layout.btnUSB;

        btnSd.setOnClickListener(view -> {
            if(!isSd) {
                toastView(view, "SD 카드가 삽입되지 않았습니다.");
                return;
            }
            select = 1;
            csvScan(select);
        });

        btnUsb.setOnClickListener(view -> {
            if(!isUsb) {
                toastView(view, "USB 메모리가 삽입되지 않았습니다.");
                return;
            }
            select = 2;
            csvScan(select);
        });


        return layout.getRoot();
    }


    private void goSearch(int i) {
        dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pbx_login);
        final CheckBox ckBox = dialog.findViewById(R.id.dialog_chk_ward);
        final Button btnLogin = dialog.findViewById(R.id.dialog_pbx_login);
        final TextView txtWard = dialog.findViewById(R.id.dialog_ward);

        final LinearLayout layoutWard = dialog.findViewById(R.id.dialog_layout_ward);
        final LinearLayout layoutError = dialog.findViewById(R.id.dialog_layout_ward_error);
        final LinearLayout layoutPw = dialog.findViewById(R.id.dialog_layout_pw);

        final TextView title = dialog.findViewById(R.id.dialog_edit_title);
        title.setText(String.format("%s 선택됨", scanListAdapter.getItem(i).toString()));
        btnLogin.setText("CSV 가져오기");

        layoutPw.setVisibility(View.GONE);
        txtWard.setText(tinyDB.getString(KeyList.DEVICE_WARD));
        if (txtWard.getText().toString().equals("")) {
            ckBox.setChecked(false);
            ckBox.setEnabled(false);
            layoutWard.setVisibility(View.GONE);
            layoutError.setVisibility(View.VISIBLE);
        } else {
            ckBox.setChecked(isWard);
            ckBox.setEnabled(isWard);
            layoutWard.setVisibility(View.VISIBLE);
            layoutError.setVisibility(View.GONE);
        }

        btnLogin.setOnClickListener(view1 -> {
            isWard = ckBox.isChecked();
            dialog.dismiss();

            openCSV(scanListAdapter.getItem(i).toString());

        });
        dialog.show();

    }

    private void openCSV(String path) {

        try {
            CSVReader reader = new CSVReader(new FileReader(path));

            csvList = new ArrayList<>();
            ArrayList<ExtItem> tempList = new ArrayList<>();

            String[] data;
            while ((data = reader.readNext()) != null) {
                if (compareType(data[0]) && compareName(data[2])
                        && compareDevice(data[2])) {
                    tempList.add(new ExtItem(data[1], data[2], false));
                }
            }


            for (int j = 0; j < tempList.size(); j++) {
                for (int a = 0; a < compareArrayList.size(); a++) {
                    Log.d(TAG, "[T] " + tempList.get(j).getNum() + " [C] " + compareArrayList.get(a).getNum());
                    if (tempList.get(j).getNum().equals(compareArrayList.get(a).getNum())) {
                        tempList.remove(j);
                    }
                }
            }
            Log.d(TAG, "tempList LIST: " + tempList.size());

            if (isWard) {
                if (searchMode.equals("ward")) {
                    String device_ward = tinyDB.getString(KeyList.DEVICE_WARD);

                    for (int i = 0; i < tempList.size(); i++) {
                        String[] callerId = tempList.get(i).getName().split("-");
                        String ward = callerId[1];

                        Log.d(TAG, "DEVICE: " + device_ward + " Ward: " + ward);
                        if (device_ward.equals(ward)) {
                            csvList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                        }
                    }
                    Log.d(TAG, "CSV LIST: " + csvList.size());

                } else {
                    csvList = tempList;
                }
            } else {
                Log.d(TAG, "---tempList LIST: " + tempList.size());

                for (int i = 0; i < tempList.size(); i++) {
                    csvList.add(new ExtItem(tempList.get(i).getNum(), tempList.get(i).getName(), false));
                }
            }


            NurseCallUtils.putExtList(tinyDB, KeyList.KEY_GET_EXTENSION, csvList);

            if (csvList.size() == 0) {
                errorSearch();
            } else {
                ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedAddFragment.newInstance());
            }

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
        return name.contains(KeyList.MODEL_PAGER_BASIC) || name.contains(KeyList.MODEL_PAGER_EXTENTION)
                || name.contains(KeyList.MODEL_PAGER_BASIC_WALL) || name.contains(KeyList.MODEL_PAGER_BASIC_STAND);
    }


    @SuppressLint("StaticFieldLeak")
    private class CSVScanTask extends AsyncTask<File, File, Boolean> {
        private String csvExtension;

        private CSVScanTask() {
            this.csvExtension = ".csv";
        }

        private boolean recursiveScan(File file) {
//            Log.d(TAG, "recursiveScan: " + file);
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
//                        txtScan.setText("Internal CSV file found");
                        break;
                    case 1:
//                        txtScan.setText("External SD Card CSV file found");
                        break;
                    case 2:
//                        txtScan.setText("External USB CSV file found");
                        break;

                }
                isScanning = false;
            }
        }

        protected void onPreExecute() {
//            txtScan.setText("Scanning for CSV files...");
            isScanning = true;
        }

        protected void onProgressUpdate(File... fileArr) {
            if (!isCancelled() && !scanResults.contains(fileArr[0])) {
                scanResults.add(fileArr[0]);
                scanListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void csvScan(int select) {
        listClear();
        storageCheck();
        File file = Environment.getExternalStorageDirectory();

        switch (select) {
            case 0:
                file = Environment.getExternalStorageDirectory();
                txtScan.setText("내부 저장소 CSV 목록");
                break;
            case 1:
                file = sdPath;
                txtScan.setText("SD 카드 CSV 목록");
                break;
            case 2:
                file = usbPath;
                txtScan.setText("USB 메모리 CSV 목록");
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
//            btnSd.setEnabled(false);
            isSd = false;
            btnSd.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.button_disable));
        } else {
//            btnSd.setEnabled(true);
            isSd = true;
            btnSd.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.button_enable));
        }

        if (usb == null) {
            isUsb = false;
//            btnUsb.setEnabled(false);
            btnUsb.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.button_disable));

        } else {
            isUsb = true;
//            btnUsb.setBackground(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.button_enable));
            btnUsb.setBackgroundResource(R.drawable.button_home);
//            btnUsb.setEnabled(true);
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
            Objects.requireNonNull(this.getContext()).registerReceiver(mountReceiver, filter);
        }
    }

    public void stopWatchingMount() {
        if (mountReceiver != null) {
            Objects.requireNonNull(this.getContext()).unregisterReceiver(mountReceiver);
        }
    }

    private void errorSearch() {
        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error_pbx);
        final TextView error = dialog.findViewById(R.id.dialog_txt_pbx_error);
        final Button close = dialog.findViewById(R.id.dialog_pbx_close);
        close.setOnClickListener(view1 -> {
            ((MainActivity) Objects.requireNonNull(getActivity())).replaceFragment(BedAddFragment.newInstance());
            dialog.dismiss();
        });
        dialog.show();

    }

    private void toastView(View view,String str) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View toastDesign = layoutInflater.inflate(R.layout.custom_toast, view.findViewById(R.id.toast_design_root));

        TextView text = toastDesign.findViewById(R.id.TextView_toast_design);
        text.setText(str);

        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastDesign);
        toast.show();
    }

}
