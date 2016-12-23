package motocitizen.gcm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import motocitizen.MyApp;
import motocitizen.network.requests.GCMRegistrationRequest;
import motocitizen.utils.Preferences;

public class GCMRegistration {
    /* constants */
    private static final String TAG                = "GCM";
    private static final String SENDER_ID          = "258135342835";
    private static final int    RESOLUTION_REQUEST = 9000;
    /* end constants */

    private GoogleCloudMessaging gcm;
    private String               regid;

    public GCMRegistration(Context context) {
        if (!checkPlayServices(context)) return;
        gcm = GoogleCloudMessaging.getInstance(MyApp.getAppContext());
        regid = getRegistrationId();
        if (regid.isEmpty()) {
            registerInBackground();
        } else {
            storeRegistrationId(regid);
        }
    }

    private static int getAppVersion() {
        try {
            PackageInfo packageInfo = MyApp.getAppContext().getPackageManager().getPackageInfo(MyApp.getAppContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean checkPlayServices(Context context) {
        GoogleApiAvailability app        = GoogleApiAvailability.getInstance();
        int                   resultCode = app.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId() {
        String registrationId = Preferences.getGCMRegistrationCode();
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = Preferences.getAppVersion();
        int currentVersion    = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MyApp.getAppContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    storeRegistrationId(regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, msg);
            }

        }.execute(null, null, null);
    }

    @SuppressLint("CommitPrefEdits")
    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        Preferences.setAppVersion(appVersion);
        Preferences.setGCMRegistrationCode(regId);
        new GCMRegistrationRequest(regId);
    }
}
