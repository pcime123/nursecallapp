package com.sscctv.nursecallapp.ui.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.nursecallapp.R;

import org.linphone.core.Address;
import org.linphone.core.CoreListenerStub;

public class NurseCallUtils {

    private Context mContext;

    public NurseCallUtils() {}

    public static Dialog getDialog(Context context, String text) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable d = new ColorDrawable(ContextCompat.getColor(context, R.color.gray));
        d.setAlpha(200);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow()
                .setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(d);

        TextView customText = dialog.findViewById(R.id.dialog_message);
        customText.setText(text);
        return dialog;
    }


    public static String getAddressDisplayName(Address address) {
        if (address == null) return null;

        String displayName = address.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.getUsername();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.asStringUriOnly();
        }
        return displayName;
    }

    public static void sendStatus(Context context, int msg) {
        Intent intent = new Intent("call");
        intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
