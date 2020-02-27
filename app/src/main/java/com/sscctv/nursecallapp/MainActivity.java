package com.sscctv.nursecallapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.main.AccountFragment;
import com.sscctv.nursecallapp.ui.main.CallListFragment;
import com.sscctv.nursecallapp.ui.main.HomeFragment;
import com.sscctv.nursecallapp.ui.main.InComingFragment;
import com.sscctv.nursecallapp.ui.main.OutGoingFragment;
import com.sscctv.nursecallapp.ui.utils.IOnBackPressed;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AccountFragment accountFragment;
    private OutGoingFragment outGoingFragment;
    private InComingFragment inComingFragment;
    private CallListFragment callListFragment;
    private HomeFragment homeFragment;
    private EditText inputCall;

    private TextView txtResult;
    private Core core;

    private long pressedTime = 0;
    private int curView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountFragment = new AccountFragment();
        outGoingFragment = new OutGoingFragment();
        inComingFragment = new InComingFragment();
        callListFragment = new CallListFragment();
        homeFragment = new HomeFragment();
        core = MainCallService.getCore();
        Log.d(TAG, "Missed Call: " + core.getMissedCallsCount());
        txtResult = findViewById(R.id.txtResult);
        inputCall = findViewById(R.id.input_call);

        Button goMain = findViewById(R.id.btnAccount);
        goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFragmentChange(1);
            }
        });

        Button goMenu = findViewById(R.id.btnCall);
        goMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Address address = core.interpretUrl(inputCall.getText().toString());
                inviteAddress(address);


            }
        });

        Button goList = findViewById(R.id.btnCallList);
        goList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFragmentChange(4);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("call"));
        onFragmentChange(0);
        curView = 0;

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msg = intent.getIntExtra("msg", 0);
            Log.d(TAG, "Fragment: " + msg);
            onFragmentChange(msg);
        }
    };

    private void inviteAddress(Address address) {
        CallParams params = core.createCallParams(null);
        if(address != null) {
            core.inviteAddressWithParams(address, params);
        } else {
            Toast.makeText(getApplicationContext(), "Address null", Toast.LENGTH_SHORT).show();
        }
    }

    public void newOutgoingCall(String to) {
        if( to == null) return;
        Log.d(TAG, "newOutgoingCall: " + !to.startsWith("sip:") + ", " + !to.contains("@"));
        Address address = core.interpretUrl(to);
        inviteAddress(address);
    }

    public void onFragmentChange(int index) {
        Log.d(TAG, "onBackPressed: " + index);
        curView = index;
        switch (index) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, accountFragment).commit();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, outGoingFragment).commit();
                break;
            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, inComingFragment).commit();
                break;
            case 4:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, callListFragment).commit();
                break;
        }
    }

    public void setTxtResult(String val, String str) {
        txtResult.setText(String.format("%s: %s", val, str));
        Log.w(TAG, val + ": " +str);
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
        if (curView == 0) {
            if (pressedTime == 0) {
                Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
                    pressedTime = 0;
                } else {
                    super.onBackPressed();

                }
            }

        }
    }
}