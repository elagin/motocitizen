package motocitizen.ui.popups

import android.content.Context
import motocitizen.content.accident.Accident
import motocitizen.user.User
import motocitizen.datasources.preferences.Preferences
import motocitizen.utils.getAccidentTextToCopy
import motocitizen.utils.getPhonesFromText

//todo renew after hide/end/activate

class AccidentListPopup(val context: Context, private val accident: Accident) : PopupWindowGeneral(context) {
    init {
        val accText = accident.getAccidentTextToCopy()

        rootView.addView(copyButtonView(accText))
        for (phone in getPhonesFromText(accident.description)) {
            rootView.addView(phoneButtonView(phone), layoutParams)
            rootView.addView(smsButtonView(phone), layoutParams)
        }
        if (User.isModerator || Preferences.login == accident.ownerName())
            rootView.addView(finishButtonView(accident))

        if (User.isModerator) {
            rootView.addView(hideButtonView(accident))
            rootView.addView(banButtonView(accident.owner), layoutParams)
        }

        rootView.addView(shareMessageView(accText))
        rootView.addView(coordinatesButtonView(accident), layoutParams)
        contentView = rootView
    }
}
