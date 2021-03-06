package motocitizen.ui.rows.accident

import android.content.Context
import motocitizen.content.accident.Accident
import motocitizen.main.R

class OwnedEndedRow(context: Context, accident: Accident) : OwnedRow(context, accident) {
    override val textColor: Int
        get() = ENDED_COLOR
    override val background: Int
        get() = R.drawable.owner_accident_ended
}
