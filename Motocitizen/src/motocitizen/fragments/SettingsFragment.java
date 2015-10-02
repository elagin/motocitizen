package motocitizen.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import motocitizen.Activity.AuthActivity;
import motocitizen.MyApp;
import motocitizen.main.R;
import motocitizen.utils.Preferences;
import motocitizen.Activity.MainScreenActivity;
import motocitizen.utils.Const;

public class SettingsFragment extends PreferenceFragment {
    Preference nottifDistPreference;
    Preference nottifAlarmPreference;
    Preference authPreference;
    Preference nottifSoundPreference;
    Preference showAcc;
    Preference showBreak;
    Preference showSteal;
    Preference showOther;
    Preference hoursAgo;
    Preference maxNotifications;
    Preference useVibration;

    private ListPreference mapProviderPreference;
    private final Preference.OnPreferenceChangeListener mapProviderListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            mapProviderPreference.setValue(newValue.toString());
            //MyApp.getMap().createMap(newValue.toString());
            preference.setSummary(mapProviderPreference.getEntry());
            return true;
        }
    };
    private final Preference.OnPreferenceChangeListener distanceListener    = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String valueText = (String) newValue;
            int    value;
            if (valueText.length() > 6) {
                valueText = valueText.substring(0, 6);
            }
            value = Integer.parseInt(valueText);
            value = Math.max(Const.EQUATOR, value);

            if (preference.equals(nottifDistPreference)) {
                Preferences.setVisibleDistance(value);
            } else if (preference.equals(nottifAlarmPreference)) {
                Preferences.setAlarmDistance(value);
            }
            update();
            return false;
        }
    };
    private final Preference.OnPreferenceChangeListener visibleListener     = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Preferences.putBoolean(preference.getKey(), (boolean) newValue);
            if (!(Preferences.toShowAcc() || Preferences.toShowBreak() || Preferences.toShowOther() || Preferences.toShowSteal())) {
                message(getString(R.string.no_one_accident_visible));
            }
            update();
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void update() {
        Preference buttonAuth = findPreference(getResources().getString(R.string.mc_settings_auth_button));
        buttonAuth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent i = new Intent(getActivity(), AuthActivity.class);
                getActivity().startActivity(i);
                return true;
            }
        });
        Preference buttonSound = findPreference(getResources().getString(R.string.mc_notif_sound));
        buttonSound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                getFragmentManager().beginTransaction().replace(android.R.id.content, new SelectSoundFragment()).commit();
                return true;
            }
        });
        nottifDistPreference = findPreference(Preferences.distanceShow);
        nottifAlarmPreference = findPreference(Preferences.distanceAlarm);

        nottifSoundPreference = findPreference(getResources().getString(R.string.mc_notif_sound));
        authPreference = findPreference(getResources().getString(R.string.mc_settings_auth_button));

        mapProviderPreference = (ListPreference) getPreferenceScreen().findPreference(Preferences.mapProvider);
        mapProviderPreference.setOnPreferenceChangeListener(mapProviderListener);
        nottifDistPreference.setOnPreferenceChangeListener(distanceListener);
        nottifAlarmPreference.setOnPreferenceChangeListener(distanceListener);
        String login = Preferences.getLogin();
        if (login.length() > 0)
            authPreference.setSummary(MyApp.getRole().getName() + ": " + login);
        else authPreference.setSummary(MyApp.getRole().getName());
        nottifSoundPreference.setSummary(Preferences.getAlarmSoundTitle());
        nottifDistPreference.setSummary(String.valueOf(Preferences.getVisibleDistance()));
        nottifAlarmPreference.setSummary(String.valueOf(Preferences.getAlarmDistance()));

        showAcc = findPreference(Preferences.showAcc);
        showBreak = findPreference(Preferences.showBreak);
        showSteal = findPreference(Preferences.showSteal);
        showOther = findPreference(Preferences.showOther);

        showAcc.setOnPreferenceChangeListener(visibleListener);
        showBreak.setOnPreferenceChangeListener(visibleListener);
        showSteal.setOnPreferenceChangeListener(visibleListener);
        showOther.setOnPreferenceChangeListener(visibleListener);

        hoursAgo = findPreference(Preferences.hoursAgo);
        hoursAgo.setSummary(String.valueOf(Preferences.getHoursAgo()));
        hoursAgo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer value;
                try {
                    value = Integer.parseInt((String) newValue);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
                if (value > 24) newValue = "24";
                if (value < 1) newValue = "1";
                preference.setSummary(newValue.toString());
                return true;
            }
        });
        maxNotifications = findPreference(Preferences.maxNotifications);
        maxNotifications.setSummary(String.valueOf(Preferences.getMaxNotifications()));
        maxNotifications.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    if (Integer.parseInt((String) newValue) < 0) newValue = "0";
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
                preference.setSummary(newValue.toString());
                return true;
            }
        });
        useVibration = findPreference(Preferences.useVibration);
        useVibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Preferences.putBoolean(preference.getKey(), (boolean) newValue);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        update();
    }

    private void message(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }
}
