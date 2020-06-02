package com.sscctv.nursecallapp.ui.activity;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;
import com.sscctv.nursecallapp.ui.fragment.settings.AccountSettingsFragment;
import com.sscctv.nursecallapp.ui.fragment.settings.AudioSettingsFragment;
import com.sscctv.nursecallapp.ui.fragment.settings.CallSettingsFragment;
import com.sscctv.nursecallapp.ui.fragment.settings.NetworkSettingsFragment;
import com.sscctv.nursecallapp.ui.utils.BasicSetting;
import com.sscctv.nursecallapp.ui.utils.LedSetting;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;
import com.sscctv.nursecallapp.ui.utils.SettingListenerBase;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;

import java.util.ArrayList;

public class _SettingsActivity extends AppCompatActivity {
    private static final String TAG = "_SettingsActivity";
    private BasicSetting mTunnel, mAudio, mVideo, mCall, mChat, mNetwork, mAdvanced, mContact;
    private LinearLayout mAccounts;
    private TextView mAccountsHeader;
    private Core core;
//    private AccountSettingsFragment accountSettingsFragment;
    private AudioSettingsFragment audioSettingsFragment;
    private CallSettingsFragment callSettingsFragment;
    private NetworkSettingsFragment networkSettingsFragment;
//    private HomeFragment homeFragment;
    private static final int MAIN_PERMISSIONS = 1;
    protected static final int FRAGMENT_SPECIFIC_PERMISSION = 2;
    private CoreListenerStub mCoreListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        core = MainCallService.getCore();

        mAccounts = findViewById(R.id.accounts_settings_list);
        mAccountsHeader = findViewById(R.id.accounts_settings_list_header);

//        mTunnel = findViewById(R.id.pref_tunnel);
        mAudio = findViewById(R.id.pref_audio);
//        mVideo = findViewById(R.id.pref_video);
        mCall = findViewById(R.id.pref_call);
//        mChat = findViewById(R.id.pref_chat);
        mNetwork = findViewById(R.id.pref_network);
//        mAdvanced = findViewById(R.id.pref_advanced);
//        mContact = findViewById(R.id.pref_contact);

//        accountSettingsFragment = new AccountSettingsFragment();
        audioSettingsFragment = new AudioSettingsFragment();
        callSettingsFragment = new CallSettingsFragment();
        networkSettingsFragment = new NetworkSettingsFragment();

        initAccounts();
        setListeners();


        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {

                if (state == Call.State.IncomingReceived) {
                     finish();
                } else if (state == Call.State.Connected) {
                    finish();
                }

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainCallService.getCore().addListener(mCoreListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainCallService.getCore().removeListener(mCoreListener);
    }

    private void setListeners() {

        mAudio.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.settings_detail_container, audioSettingsFragment).commit();
                    }
                });

        mCall.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.settings_detail_container, callSettingsFragment).commit();
                    }
                });

        mNetwork.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.settings_detail_container, networkSettingsFragment).commit();
                    }
                });
    }
    private void initAccounts() {
        mAccounts.removeAllViews();
        ProxyConfig[] proxyConfigs = core.getProxyConfigList();

        if ((proxyConfigs == null) || proxyConfigs.length == 0) {
            mAccountsHeader.setVisibility(View.GONE);
            NurseCallUtils.printShort(this, "No Account");
        } else {
            mAccountsHeader.setVisibility(View.VISIBLE);
            int i = 0;
            for(ProxyConfig proxyConfig : proxyConfigs) {
                final LedSetting setting = new LedSetting(getApplicationContext());
                setting.setTitle(NurseCallUtils.getDisplayableAddress(proxyConfig.getIdentityAddress()));
                if(proxyConfig.equals(core.getDefaultProxyConfig())) {
                    setting.setSubtitle("Default Account");
                }

                switch (proxyConfig.getState()) {
                    case Ok:
                        setting.setColor(LedSetting.Color.GREEN);
                        break;
                    case Failed:
                        setting.setColor(LedSetting.Color.RED);
                        break;
                    case Progress:
                        setting.setColor(LedSetting.Color.ORANGE);
                        break;
                    case None:
                    case Cleared:
                        setting.setColor(LedSetting.Color.GRAY);
                        break;
                }

                final int acctounIndex = i;
                setting.setListener(new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                       showAccountSettings(acctounIndex);
                    }
                });
                mAccounts.addView(setting);
            }
        }
    }

    public void showAccountSettings(int accountIndex) {
        Bundle extras = new Bundle();
        extras.putInt("Account", accountIndex);
        AccountSettingsFragment accountSettingsFragment = new AccountSettingsFragment();
        accountSettingsFragment.setArguments(extras);
//        changeFragment(accountSettingsFragment, getString(R.string.pref_sipaccount), isChild);
                getSupportFragmentManager().beginTransaction().replace(R.id.settings_detail_container, accountSettingsFragment).commit();

//        showTopBarWithTitle(getString(R.string.pref_sipaccount));
    }


    public boolean checkPermission(String permission) {
        int granted = getPackageManager().checkPermission(permission, getPackageName());
        Log.i(TAG,
                "[Permission] "
                        + permission
                        + " permission is "
                        + (granted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissions(String[] permissions) {
        boolean allGranted = true;
        for (String permission : permissions) {
            allGranted &= checkPermission(permission);
        }
        return allGranted;
    }

    public void requestPermissionIfNotGranted(String permission) {
        if (!checkPermission(permission)) {
            Log.i(TAG, "[Permission] Requesting " + permission + " permission");

            String[] permissions = new String[] {permission};
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean locked = km.inKeyguardRestrictedInputMode();
            if (!locked) {
                // This is to workaround an infinite loop of pause/start in Activity issue
                // if incoming activity_call ends while screen if off and locked
                ActivityCompat.requestPermissions(this, permissions, FRAGMENT_SPECIFIC_PERMISSION);
            }
        }
    }

    public void requestPermissionsIfNotGranted(String[] perms) {
        requestPermissionsIfNotGranted(perms, FRAGMENT_SPECIFIC_PERMISSION);
    }

    private void requestPermissionsIfNotGranted(String[] perms, int resultCode) {
        ArrayList<String> permissionsToAskFor = new ArrayList<>();
        if (perms != null) { // This is created (or not) by the child activity
            for (String permissionToHave : perms) {
                if (!checkPermission(permissionToHave)) {
                    permissionsToAskFor.add(permissionToHave);
                }
            }
        }

        if (permissionsToAskFor.size() > 0) {
            for (String permission : permissionsToAskFor) {
                Log.i(TAG, "[Permission] Requesting " + permission + " permission");
            }
            String[] permissions = new String[permissionsToAskFor.size()];
            permissions = permissionsToAskFor.toArray(permissions);

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean locked = km.inKeyguardRestrictedInputMode();
            if (!locked) {
                // This is to workaround an infinite loop of pause/start in Activity issue
                // if incoming activity_call ends while screen if off and locked
                ActivityCompat.requestPermissions(this, permissions, resultCode);
            }
        }
    }

//    private void requestRequiredPermissions() {
//        requestPermissionsIfNotGranted(mPermissionsToHave, MAIN_PERMISSIONS);
//    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length <= 0) return;

        for (int i = 0; i < permissions.length; i++) {
            Log.i(TAG,
                    "[Permission] "
                            + permissions[i]
                            + " is "
                            + (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            ? "granted"
                            : "denied"));
            if (permissions[i].equals(Manifest.permission.READ_CONTACTS)
                    || permissions[i].equals(Manifest.permission.WRITE_CONTACTS)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (MainCallService.isReady()) {
//                        ContactsManager.getInstance().enableContactsAccess();
//                        ContactsManager.getInstance().initializeContactManager();
                    }
                }
            } else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                boolean enableRingtone = grantResults[i] == PackageManager.PERMISSION_GRANTED;
//                LinphonePreferences.instance().enableDeviceRingtone(enableRingtone);
//                LinphoneManager.getInstance().enableDeviceRingtone(enableRingtone);
            } else if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                LinphoneUtils.reloadVideoDevices();
            }
        }
    }


}
