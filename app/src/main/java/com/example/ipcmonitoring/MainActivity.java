package com.example.ipcmonitoring;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver dynamicReceiver;

    private ArrayList<String> intents = new ArrayList<String>() {{
        add(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        add(Intent.ACTION_NEW_OUTGOING_CALL);
        add(Intent.ACTION_MEDIA_MOUNTED);
        add(Intent.ACTION_BOOT_COMPLETED);
        add(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        add(Intent.ACTION_USER_PRESENT);
        add(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        dynamicReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                } else if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
//                    Log.d("IPCMonitor", "onReceive: " + intent.getAction());
//                }
//            }
//        };

        // Register the dynamic BroadcastReceiver with the intent filter object
        IntentFilter intentFilter = new IntentFilter();
        for(String intentString: intents) {
            intentFilter.addAction(intentString);
        }

        Map<String, ArrayList<String>> map = getBroadcastReceivers(getApplicationContext(), intents);
        for(String app: map.keySet()) {
            Log.d("IPCMonitor", "onCreate: " + app + " " + map.get(app));
        }

    }

    @Override
    protected void onDestroy() {
        // Unregister the dynamic BroadcastReceiver when the activity is destroyed
        if (dynamicReceiver != null) {
            unregisterReceiver(dynamicReceiver);
        }
        super.onDestroy();
    }

    public static boolean hasBroadcastReceiver(Context context, String packageName, String intentString) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(intentString);
        intent.setPackage(packageName);

        // Query the package manager to see if the receiver exists
        return pm.queryBroadcastReceivers(intent, 0).size() > 0;
    }

    public static Map<String, ArrayList<String>> getBroadcastReceivers(Context context, List<String> intents) {
        List<String> thirdPartyApps = getThirdPartyApps(context);
        Map<String, ArrayList<String>> map = new HashMap<>();

        for(String packageName: thirdPartyApps) {
            ArrayList<String> receivers = new ArrayList<>();
            for(String intentString: intents) {
                if(hasBroadcastReceiver(context, packageName, intentString)) {
//                    Log.d("IPCMonitor", "getBroadcastReceivers: " + app + " " + intentString);
                    receivers.add(intentString);
                }
            }
            if(receivers.size() > 0) {
                map.put(packageName, receivers);
            }
        }
        return map;
    }


    public static List<String> getThirdPartyApps(Context context) {
        List<String> thirdPartyApps = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            // skip system apps
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                thirdPartyApps.add(packageInfo.packageName);
            }
        }

        return thirdPartyApps;
    }
}
