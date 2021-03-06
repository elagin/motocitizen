package motocitizen.content

import motocitizen.content.volunteer.Volunteer
import org.json.JSONObject
import java.util.*

object VolunteersController {
    val volunteers: TreeMap<Int, Volunteer> = TreeMap()
    fun addVolunteers(volunteersList: JSONObject) {
        volunteersList.keys()
                .forEach { volunteers[it.toInt()] = Volunteer(it.toInt(), volunteersList.getString(it)) }
    }
}