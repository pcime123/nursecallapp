package com.sscctv.nursecallapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartService extends BroadcastReceiver {

    private static final String TAG = "RestartService";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "RestartService Called: " + intent.getAction());


        if(intent.getAction().equals("ACTION.RESTART.PersistentService")) {
            Log.i(TAG, "ACTION.RESTART.PersistentService");

            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){

            Log.i(TAG, "ACTION_BOOT_COMPLETED" );
            Intent i = new Intent(context,PersistentService.class);
            context.startService(i);

        }

    }
}
