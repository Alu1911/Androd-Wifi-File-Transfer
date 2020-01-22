package com.Devlex.iWifiFileTransfer;


import com.Devlex.iWifiFileTransfer.BuildConfig;
import com.crashlytics.android.Crashlytics;

/**
 * Created by HaKr on 23/05/16.
 */

public class CrashReportingManager {

    public static void logException(Exception e) {
        logException(e, false);
    }

    public static void logException(Exception e, boolean log) {
        if(BuildConfig.DEBUG){
            e.printStackTrace();
        } else if(log) {
            Crashlytics.logException(e);

        }
    }
}
