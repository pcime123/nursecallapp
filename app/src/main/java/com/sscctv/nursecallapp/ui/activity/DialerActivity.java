package com.sscctv.nursecallapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

import org.linphone.core.Address;
import org.linphone.core.Core;

import java.util.Objects;

public class DialerActivity extends MainActivity {

    EditText textView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        textView = findViewById(R.id.inputNum);
        findViewById(R.id.num0).setOnClickListener(view -> {
            textView.append("0");
        });
        findViewById(R.id.num1).setOnClickListener(view -> {
            textView.append("1");
        });
        findViewById(R.id.num2).setOnClickListener(view -> {
            textView.append("2");
        });
        findViewById(R.id.num3).setOnClickListener(view -> {
            textView.append("3");
        });
        findViewById(R.id.num4).setOnClickListener(view -> {
            textView.append("4");
        });
        findViewById(R.id.num5).setOnClickListener(view -> {
            textView.append("5");
        });
        findViewById(R.id.num6).setOnClickListener(view -> {
            textView.append("6");
        });
        findViewById(R.id.num7).setOnClickListener(view -> {
            textView.append("7");
        });
        findViewById(R.id.num8).setOnClickListener(view -> {
            textView.append("8");
        });
        findViewById(R.id.num9).setOnClickListener(view -> {
            textView.append("9");
        });
        findViewById(R.id.del).setOnClickListener(view -> {
            int len = textView.getText().length();
            if (len > 0) {
                textView.getText().delete(len - 1, len);
            }
        });
        findViewById(R.id.star).setOnClickListener(view -> {
            textView.append("*");
        });


        findViewById(R.id.callStart).setOnClickListener(view -> {
            if (textView.getText().length() > 0) {
                Core core = MainCallService.getCore();
//                Address address = core.interpretUrl(textView.getText().toString());
                newOutgoingCall(textView.getText().toString());
                if(core.getCurrentCall() == null) {
                    NurseCallUtils.printShort(getApplicationContext(), "failed");
                }
            }
        });
    }
}
