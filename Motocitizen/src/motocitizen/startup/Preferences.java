package motocitizen.startup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

import motocitizen.content.Type;
import motocitizen.main.R;
import motocitizen.utils.Const;

@SuppressLint("CommitPrefEdits")
public class Preferences {
    /* constants */
    private final static float DEFAULT_LATITUDE          = 55.752295f;
    private final static float DEFAULT_LONGITUDE         = 37.622735f;
    private final static int   DEFAULT_SHOW_DISTANCE     = 200;
    private final static int   DEFAULT_ALARM_DISTANCE    = 20;
    private final static int   DEFAULT_MAX_NOTIFICATIONS = 3;
    private final static int   DEFAULT_MAX_AGE           = 24;
    /* end constants */

    public final static  String showAcc;
    public final static  String showBreak;
    public final static  String showSteal;
    public final static  String showOther;
    public final static  String distanceShow;
    public final static  String distanceAlarm;
    public final static  String mapProvider;
    public final static  String currentVersion;
    public final static  String doNotDisturb;
    public final static  String hoursAgo;
    public final static  String maxNotifications;
    public final static  String useVibration;
    public final static  String userId;
    public final static  String userName;
    public final static  String userRole;
    public final static  String DEFAULT_REGION;

    private final static String onWay;
    private final static String soundTitle;
    private final static String soundURI;
    private final static String login;
    private final static String password;
    private final static String anonim;
    private final static String GCMRegistrationCode;
    private final static String appVersion;
    private final static String savedLng;
    private final static String savedLat;
    private final static String notificationList;

    private final static String[] mapProviders;

    private static SharedPreferences preferences;
    private static Context           context;

    static {
        showAcc = "mc.show.acc";
        showBreak = "mc.show.break";
        showSteal = "mc.show.steal";
        showOther = "mc.show.other";
        distanceShow = "mc.distance.show";
        distanceAlarm = "mc.distance.alarm";
        mapProvider = "mc.map.provider";
        currentVersion = "version";
        doNotDisturb = "do.not.disturb";
        hoursAgo = "hours.ago";
        maxNotifications = "notifications.max";
        useVibration = "use.vibration";
        userId = "userId";
        userName = "userName";
        userRole = "userRole";
        onWay = "mc.onway";
        soundTitle = "mc.notification.sound.title";
        soundURI = "mc.notification.sound";
        login = "mc.login";
        password = "mc.password";
        anonim = "mc.anonim";
        GCMRegistrationCode = "mc.gcm.id";
        appVersion = "mc.app.version";
        savedLng = "savedlng";
        savedLat = "savedlat";
        notificationList = "notificationList";
        DEFAULT_REGION = "default.region";

        mapProviders = new String[]{"google", "osm", "yandex"};
    }

    public Preferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Preferences.context = context;
    }

    public static void putBoolean(String name, boolean value) {
        preferences.edit().putBoolean(name, value).commit();
    }

    public static int getOnWay() {
        return preferences.getInt(onWay, 0);
    }

    public static void setOnWay(int id) {
        preferences.edit().putInt(onWay, id).commit();
    }

    public static LatLng getSavedLatLng() {
        double lat = (double) preferences.getFloat(savedLat, DEFAULT_LATITUDE);
        double lng = (double) preferences.getFloat(savedLng, DEFAULT_LONGITUDE);
        return new LatLng(lat, lng);
    }

    public static boolean getDoNotDisturb() {
        return preferences.getBoolean(doNotDisturb, false);
    }

    public static void setDoNotDisturb(boolean value) {
        preferences.edit().putBoolean(doNotDisturb, value).commit();
    }

    public static String getCurrentVersion() {
        return preferences.getString(currentVersion, context.getString(R.string.unknown_code_version));
    }

    public static void setCurrentVersion(String version) {
        preferences.edit().putString(currentVersion, version).commit();
    }

    public static void saveLatLng(LatLng latlng) {
        preferences.edit().putFloat(savedLat, (float) latlng.latitude).putFloat(savedLng, (float) latlng.longitude).commit();
    }

    public static int getVisibleDistance() {
        int distance;
        try {
            distance = preferences.getInt(distanceShow, DEFAULT_SHOW_DISTANCE);
        } catch (Exception e) {
            String distanceString = preferences.getString(distanceShow, String.valueOf(DEFAULT_SHOW_DISTANCE));
            distance = Integer.parseInt(distanceString);
        }
        return distance;
    }

    public static void setVisibleDistance(int distance) {
        if (distance > Const.EQUATOR) {
            distance = Const.EQUATOR;
        }
        preferences.edit().putString(distanceShow, String.valueOf(distance)).commit();
    }

    public static int getAlarmDistance() {
        int distance;
        try {
            distance = preferences.getInt(distanceAlarm, DEFAULT_ALARM_DISTANCE);
        } catch (Exception e) {
            String distanceString = preferences.getString(distanceAlarm, String.valueOf(DEFAULT_ALARM_DISTANCE));
            distance = Integer.parseInt(distanceString);
        }
        return distance;
    }

    public static void setAlarmDistance(int distance) {
        if (distance > Const.EQUATOR) {
            distance = Const.EQUATOR;
        }
        preferences.edit().putString(distanceAlarm, String.valueOf(distance)).commit();
    }

    public static String getAlarmSoundTitle() {
        return preferences.getString(soundTitle, "default system");
    }

    public static Uri getAlarmSoundUri() {
        Uri    uri;
        String uriString = preferences.getString(soundURI, "default");
        if (uriString.equals("default")) {
            uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
        } else uri = Uri.parse(uriString);
        return uri;
    }

    public static String getMapProvider() {
        return preferences.getString(mapProvider, "google");
    }

    public static void setMapProvider(String provider) {
        if (Arrays.asList(mapProviders).contains(provider)) {
            preferences.edit().putString(mapProvider, provider);
        }
    }

    public static String getLogin() {
        return preferences.getString(login, "");
    }

    public static void setLogin(String value) {
        preferences.edit().putString(login, value).commit();
    }

    public static String getPassword() {
        return preferences.getString(password, "");
    }

    public static void setPassword(String value) {
        preferences.edit().putString(password, value).commit();
    }

    public static boolean isAnonim() {
        return preferences.getBoolean(anonim, false);
    }

    public static void setAnonim(boolean value) {
        preferences.edit().putBoolean(anonim, value).commit();
    }

    public static String getGCMRegistrationCode() {
        return preferences.getString(GCMRegistrationCode, "");
    }

    public static void setGCMRegistrationCode(String code) {
        preferences.edit().putString(GCMRegistrationCode, code).commit();
    }

    public static int getAppVersion() {
        return preferences.getInt(appVersion, 0);
    }

    public static void setAppVersion(int code) {
        preferences.edit().putInt(appVersion, code).commit();
    }

    public static void setSoundAlarm(String title, Uri uri) {
        preferences.edit().putString(soundTitle, title).putString(soundURI, uri.toString()).commit();
    }

    public static void setDefaultSoundAlarm() {
        preferences.edit().putString(soundTitle, "default system").putString(soundURI, "default").commit();
    }

    public static void resetAuth() {
        preferences.edit().remove(login).remove(password).commit();
    }

    public static boolean isHidden(Type type) {
        switch (type) {
            case BREAK:
                return !toShowBreak();
            case MOTO_AUTO:
            case MOTO_MOTO:
            case MOTO_MAN:
            case SOLO:
                return !toShowAcc();
            case STEAL:
                return !toShowSteal();
            case OTHER:
            default:
                return !toShowOther();
        }
    }

    public static boolean toShowBreak() {
        return preferences.getBoolean(showBreak, true);
    }

    public static boolean toShowAcc() {
        return preferences.getBoolean(showAcc, true);
    }

    public static boolean toShowSteal() {
        return preferences.getBoolean(showSteal, true);
    }

    public static boolean toShowOther() {
        return preferences.getBoolean(showOther, true);
    }

    public static int getMaxNotifications() {
        return Integer.parseInt(preferences.getString(maxNotifications, String.valueOf(DEFAULT_MAX_NOTIFICATIONS)));
    }

    public static JSONArray getNotificationList() {
        JSONArray json;
        try {
            json = new JSONArray(preferences.getString(notificationList, "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
            json = new JSONArray();
        }
        return json;
    }

    public static void setNotificationList(JSONArray json) {
        preferences.edit().putString(notificationList, json.toString()).commit();
    }

    public static int getHoursAgo() {
        return Integer.parseInt(preferences.getString(hoursAgo, String.valueOf(DEFAULT_MAX_AGE)));
    }

    public static boolean getVibration() {
        return preferences.getBoolean(useVibration, true);
    }

    public static String getUserName() {
        return preferences.getString(userName, "");
    }

    public static void setUserName(String name) {
        preferences.edit().putString(userName, name).commit();
    }

    public static int getUserId() {
        return preferences.getInt(userId, 0);
    }

    public static void setUserId(int id) {
        preferences.edit().putInt(userId, id).commit();
    }

    public static void setUserRole(String role) {
        preferences.edit().putString(userRole, role).commit();
    }

    public static void setDefaultRegion(String value) {
        preferences.edit().putString(DEFAULT_REGION, value).commit();
    }

    public static String getDefaultRegion() {
        return preferences.getString(DEFAULT_REGION, "77");
    }
}
