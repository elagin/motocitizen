package motocitizen.ui.fragments

import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceFragment
import motocitizen.datasources.preferences.Preferences
import motocitizen.main.R
import motocitizen.router.Router
import motocitizen.user.User
import motocitizen.utils.EQUATOR
import motocitizen.utils.preferenceByName
import motocitizen.utils.show

class SettingsFragment : PreferenceFragment() {
    private val PREFERENCES = R.xml.preferences

    private lateinit var notificationDistPreference: Preference
    private lateinit var notificationAlarmPreference: Preference
    private val preferences = Preferences

    private lateinit var buttonAuth: Preference
    private lateinit var buttonSound: Preference
    private lateinit var showAcc: Preference
    private lateinit var showBreak: Preference
    private lateinit var showSteal: Preference
    private lateinit var showOther: Preference
    private lateinit var hoursAgo: Preference
    private lateinit var maxNotifications: Preference
    private lateinit var useVibration: Preference
    private lateinit var notificationSoundPreference: Preference
    private lateinit var authPreference: Preference

    private var login = preferences.login

    override fun onResume() {
        super.onResume()
        preferenceScreen = null
        addPreferencesFromResource(PREFERENCES)
        bindPreferences()
        setUpListeners()
        update()
    }

    private fun update() {
        authPreference.summary = if (login.isNotEmpty()) User.roleName + ": " + login else User.roleName
        maxNotifications.summary = preferences.maxNotifications.toString()
        hoursAgo.summary = preferences.hoursAgo.toString()
        notificationSoundPreference.summary = preferences.soundTitle
        notificationDistPreference.summary = preferences.visibleDistance.toString()
        notificationAlarmPreference.summary = preferences.alarmDistance.toString()
    }

    private fun setUpListeners() {
        notificationDistPreference.onPreferenceChangeListener = OnPreferenceChangeListener(this::distanceListener)
        notificationAlarmPreference.onPreferenceChangeListener = OnPreferenceChangeListener(this::distanceListener)
        maxNotifications.onPreferenceChangeListener = OnPreferenceChangeListener(this::maxNotificationsListener)
        hoursAgo.onPreferenceChangeListener = OnPreferenceChangeListener(this::hoursAgoListener)
        useVibration.onPreferenceChangeListener = OnPreferenceChangeListener(this::vibrationListener)
        buttonSound.onPreferenceClickListener = Preference.OnPreferenceClickListener { this.soundButtonPressed() }
        buttonAuth.onPreferenceClickListener = Preference.OnPreferenceClickListener { this.authButtonPressed() }
        showAcc.onPreferenceChangeListener = OnPreferenceChangeListener(this::visibleListener)
        showBreak.onPreferenceChangeListener = OnPreferenceChangeListener(this::visibleListener)
        showSteal.onPreferenceChangeListener = OnPreferenceChangeListener(this::visibleListener)
        showOther.onPreferenceChangeListener = OnPreferenceChangeListener(this::visibleListener)
    }

    private fun maxNotificationsListener(preference: Preference, newValue: Any): Boolean {
        preference.summary = newValue as String
        return true
    }

    private fun hoursAgoListener(preference: Preference, newValue: Any): Boolean {
        var value = newValue
        if (value == "0") value = "1"
        preference.summary = value.toString()
        return true
    }

    private fun vibrationListener(preference: Preference, newValue: Any): Boolean {
        preferences.vibration = newValue as Boolean
        return true
    }

    private fun authButtonPressed(): Boolean {
        Router.goTo(activity, Router.Target.AUTH)
        return true
    }

    private fun soundButtonPressed(): Boolean {
        fragmentManager.beginTransaction().replace(android.R.id.content, SelectSoundFragment()).commit()
        return true
    }

    private fun bindPreferences() {
        notificationDistPreference = preferenceByName("distanceShow")
        notificationAlarmPreference = preferenceByName("distanceAlarm")
        showAcc = preferenceByName("showAcc")
        showBreak = preferenceByName("showBreak")
        showSteal = preferenceByName("showSteal")
        showOther = preferenceByName("showOther")
        hoursAgo = preferenceByName("hoursAgo")
        maxNotifications = preferenceByName("maxNotifications")
        useVibration = preferenceByName("useVibration")
        buttonAuth = findPreference(resources.getString(R.string.settings_auth_button))
        buttonSound = findPreference(resources.getString(R.string.notification_sound))
        notificationSoundPreference = findPreference(resources.getString(R.string.notification_sound))
        authPreference = findPreference(resources.getString(R.string.settings_auth_button))
    }

    private fun distanceListener(preference: Preference, newValue: Any): Boolean {
        val value = Math.min(EQUATOR, Integer.parseInt(newValue as String))

        when (preference) {
            notificationDistPreference  -> preferences.visibleDistance = value
            notificationAlarmPreference -> preferences.alarmDistance = value
        }
        update()
        return false
    }

    private fun visibleListener(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            "mc.show.acc"   -> preferences.showAcc = newValue as Boolean
            "mc.show.break" -> preferences.showBreak = newValue as Boolean
            "mc.show.steal" -> preferences.showSteal = newValue as Boolean
            "mc.show.other" -> preferences.showOther = newValue as Boolean
        }
        if (isAllHidden) {
            show(activity, getString(R.string.no_one_accident_visible))
        }
        update()
        return false
    }

    private val isAllHidden: Boolean
        inline get() = !(preferences.showAcc || preferences.showBreak || preferences.showOther || preferences.showSteal)
}
