package motocitizen.fragments;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import motocitizen.main.R;
import motocitizen.router.Router;
import motocitizen.user.User;
import motocitizen.utils.Const;
import motocitizen.utils.Preferences;
import motocitizen.utils.ToastUtils;

public class SettingsFragment extends PreferenceFragment {
    private Preference     notificationDistPreference;
    private Preference     notificationAlarmPreference;
    private ListPreference mapProviderPreference;

    private void update() {
        Preferences preferences = Preferences.getInstance(getActivity());
        String login = preferences.getLogin();
        Preference.OnPreferenceChangeListener visibleListener = (preference, newValue) -> {
            preferences.putBoolean(preference.getKey(), (boolean) newValue);
            if (preferences.hideAccidents()
                && preferences.hideBreaks()
                && preferences.hideOthers()
                && preferences.hideSteals()) {
                ToastUtils.show(getActivity(), getString(R.string.no_one_accident_visible));
            }
            update();
            return false;
        };
        Preference.OnPreferenceChangeListener distanceListener = (preference, newValue) -> {
            Integer value = Math.min(Const.EQUATOR, Integer.parseInt((String) newValue));

            if (preference.equals(notificationDistPreference)) {
                preferences.setVisibleDistance(value);
            } else if (preference.equals(notificationAlarmPreference)) {
                preferences.setAlarmDistance(value);
            }
            update();
            return false;
        };

        mapProviderPreference = (ListPreference) getPreferenceScreen().findPreference(preferences.getPreferenceName("mapProvider"));
        notificationDistPreference = findPreference(preferences.getPreferenceName("distanceShow"));
        notificationAlarmPreference = findPreference(preferences.getPreferenceName("distanceAlarm"));
        Preference buttonAuth                  = findPreference(getResources().getString(R.string.settings_auth_button));
        Preference buttonSound                 = findPreference(getResources().getString(R.string.notification_sound));
        Preference showAcc                     = findPreference(preferences.getPreferenceName("showAcc"));
        Preference showBreak                   = findPreference(preferences.getPreferenceName("showBreak"));
        Preference showSteal                   = findPreference(preferences.getPreferenceName("showSteal"));
        Preference showOther                   = findPreference(preferences.getPreferenceName("showOther"));
        Preference hoursAgo                    = findPreference(preferences.getPreferenceName("hoursAgo"));
        Preference maxNotifications            = findPreference(preferences.getPreferenceName("maxNotifications"));
        Preference useVibration                = findPreference(preferences.getPreferenceName("useVibration"));
        Preference notificationSoundPreference = findPreference(getResources().getString(R.string.notification_sound));
        Preference authPreference              = findPreference(getResources().getString(R.string.settings_auth_button));

        authPreference.setSummary(login.length() > 0 ? User.getInstance(getActivity()).getRoleName() + ": " + login : User.getInstance(getActivity()).getRoleName());
        maxNotifications.setSummary(String.valueOf(preferences.getMaxNotifications()));
        hoursAgo.setSummary(String.valueOf(preferences.getHoursAgo()));
        notificationSoundPreference.setSummary(preferences.getAlarmSoundTitle());
        notificationDistPreference.setSummary(String.valueOf(preferences.getVisibleDistance()));
        notificationAlarmPreference.setSummary(String.valueOf(preferences.getAlarmDistance()));

        notificationDistPreference.setOnPreferenceChangeListener(distanceListener);
        notificationAlarmPreference.setOnPreferenceChangeListener(distanceListener);
        mapProviderPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            mapProviderPreference.setValue(newValue.toString());
            preference.setSummary(mapProviderPreference.getEntry());
            return true;
        });
        maxNotifications.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        });
        hoursAgo.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.equals("0")) newValue = "1";
            preference.setSummary(newValue.toString());
            return true;
        });
        useVibration.setOnPreferenceChangeListener((preference, newValue) -> {
            preferences.putBoolean(preference.getKey(), (boolean) newValue);
            return true;
        });
        buttonSound.setOnPreferenceClickListener(stub -> {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SelectSoundFragment()).commit();
            return true;
        });
        buttonAuth.setOnPreferenceClickListener(stub -> {
            Router.goTo(getActivity(), Router.Target.AUTH);
            return true;
        });
        showAcc.setOnPreferenceChangeListener(visibleListener);
        showBreak.setOnPreferenceChangeListener(visibleListener);
        showSteal.setOnPreferenceChangeListener(visibleListener);
        showOther.setOnPreferenceChangeListener(visibleListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        update();
    }
}
