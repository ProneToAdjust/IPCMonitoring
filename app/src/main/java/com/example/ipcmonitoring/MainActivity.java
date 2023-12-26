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

    private final ArrayList<String> intents = new ArrayList<String>() {{
        add(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        add(Intent.ACTION_NEW_OUTGOING_CALL);
        add(Intent.ACTION_MEDIA_MOUNTED);
        add(Intent.ACTION_BOOT_COMPLETED);
        add(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        add(Intent.ACTION_USER_PRESENT);
        add(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
    }};

    private final HashMap<String, String> intentDescriptions = new HashMap<String, String>() {{
        put(TelephonyManager.ACTION_PHONE_STATE_CHANGED, "This broadcast is sent when the phone state changes, such as when a call is incoming, outgoing, or ended.");
        put(Intent.ACTION_NEW_OUTGOING_CALL, "This broadcast is sent when the user initiates a new outgoing call.");
        put(Intent.ACTION_MEDIA_MOUNTED, "This broadcast is sent when a new external storage volume, such as an SD card, is mounted.");
        put(Intent.ACTION_BOOT_COMPLETED, "This broadcast is sent when the device finishes booting.");
        put(Intent.ACTION_LOCKED_BOOT_COMPLETED, "This broadcast is sent after the user has finished booting, but while still in the locked state.");
        put(Intent.ACTION_USER_PRESENT, "This broadcast is sent when the user unlocks the device or enters the home screen.");
        put(Telephony.Sms.Intents.SMS_RECEIVED_ACTION, "This broadcast is sent when a new text-based SMS message has been received by the device.");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, ArrayList<String>> map = getBroadcastReceivers(getApplicationContext(), intents);
        for(String app: map.keySet()) {
            String logMsg = app + " has the following receivers:";
            for(String receiver: map.get(app)) {
                logMsg += "\n\t" + receiver;
                logMsg += "\n\t\t" + intentDescriptions.get(receiver);
            }
            Log.d("MainActivity", logMsg);
        }

    }

    public static Map<String, ArrayList<String>> getBroadcastReceivers(Context context, List<String> intents) {
        List<PackageInfo> thirdPartyApps = getThirdPartyApps(context);
        Map<String, ArrayList<String>> map = new HashMap<>();

        for(PackageInfo packageInfo: thirdPartyApps) {
            ArrayList<String> receivers = new ArrayList<>();
            for(String intentString: intents) {
                if(hasBroadcastReceiver(context, packageInfo.packageName, intentString)) {
                    receivers.add(intentString);
                }
            }
            if(receivers.size() > 0) {
                String appLabel = context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString();
                map.put(appLabel, receivers);
            }
        }
        return map;
    }


    public static List<PackageInfo> getThirdPartyApps(Context context) {
        List<PackageInfo> thirdPartyApps = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            // skip system apps
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                thirdPartyApps.add(packageInfo);
            }
        }

        return thirdPartyApps;
    }

    public static boolean hasBroadcastReceiver(Context context, String packageName, String intentString) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(intentString);
        intent.setPackage(packageName);

        // Query the package manager to see if the receiver exists
        return pm.queryBroadcastReceivers(intent, 0).size() > 0;
    }
}
