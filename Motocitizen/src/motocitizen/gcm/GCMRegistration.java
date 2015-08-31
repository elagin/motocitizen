package motocitizen.gcm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import motocitizen.MyApp;
import motocitizen.content.Content;
import motocitizen.network.requests.GCMRegistrationRequest;
import motocitizen.startup.Preferences;

public class GCMRegistration {
    private static final String TAG                              = "GCM";
    private final static int    PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final        String SENDER_ID                        = "258135342835";
    private        GoogleCloudMessaging gcm;
    private        String               regid;
    private static Context              context;
    private static Preferences          prefs;

    public GCMRegistration(Context context) {
        GCMRegistration.context = context;
        prefs = ((MyApp) context.getApplicationContext()).getPreferences();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId();
            Log.d(TAG, regid);
            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                storeRegistrationId(regid);
            }
        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
    }

    private static int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private static Map<String, String> createPOST(String regId) {
        String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        Map<String, String> POST = new HashMap<>();
        POST.put("owner_id", String.valueOf(Content.auth.getid()));
        POST.put("gcm_key", regId);
        POST.put("login", Content.auth.getLogin());
        POST.put("imei", imei);
        POST.put("passhash", Content.auth.makePassHash());
        POST.put("calledMethod", "registerGCM");
        return POST;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) context, PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
        int currentVersion = getAppVersion();
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
                @SuppressWarnings("UnusedAssignment") String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
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
        new GCMRegistrationRequest(context, regId);
    }
}