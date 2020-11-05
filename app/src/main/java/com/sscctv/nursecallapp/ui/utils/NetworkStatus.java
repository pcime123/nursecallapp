package com.sscctv.nursecallapp.ui.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Objects;

public class NetworkStatus {
    public static final String TAG = NetworkStatus.class.getSimpleName();
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(manager).getActiveNetworkInfo();
        if(networkInfo != null) {
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_ETHERNET) {
                return ConnectivityManager.TYPE_ETHERNET;
            }
        }
        return TYPE_NOT_CONNECTED;
    }
}
