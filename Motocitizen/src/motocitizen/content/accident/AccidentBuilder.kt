package motocitizen.content.accident

import motocitizen.content.history.History
import motocitizen.content.volunteer.VolunteerAction
import motocitizen.dictionary.AccidentStatus
import motocitizen.dictionary.Medicine
import motocitizen.dictionary.Type
import motocitizen.geo.geocoder.AccidentLocation
import motocitizen.geo.geolocation.MyLocationManager
import motocitizen.user.User
import java.util.*
import kotlin.collections.ArrayList

class AccidentBuilder {
    var id = 0
        private set
    var status = AccidentStatus.ACTIVE
        private set
    var type = Type.OTHER
        private set
    var medicine = Medicine.NO
        private set
    var time = Date()
        private set
    var location = AccidentLocation(coordinates = MyLocationManager.getLocation())
        private set
    var owner = User.id
        private set
    var description = ""
        private set
    var volunteers = ArrayList<VolunteerAction>()
        private set
    var history = ArrayList<History>()
        private set
    var messagesCount = 0
        private set

    fun id(id: Int) = apply { this.id = id }

    fun status(status: AccidentStatus) = apply { this.status = status }

    fun type(type: Type) = apply { this.type = type }

    fun medicine(medicine: Medicine) = apply { this.medicine = medicine }

    fun time(time: Date) = apply { this.time = time }

    fun location(location: AccidentLocation) = apply { this.location = location }

    fun owner(owner: Int) = apply { this.owner = owner }

    fun description(description: String) = apply { this.description = description }

    fun messagesCount(count: Int) = apply { this.messagesCount = count }

    fun from(accident: Accident) = apply {
        id = accident.id
        type = accident.type
        medicine = accident.medicine
        time = accident.time
        location = accident.location
        owner = accident.owner
        description = accident.description
        volunteers = accident.volunteers
        history = accident.history
        messagesCount = accident.messagesCount
    }

    fun build(): Accident {
        val accident = if (owner == User.id) ownedAccident() else commonAccident()

        accident.description = description
        accident.volunteers.addAll(volunteers)
        accident.history.addAll(history)
        accident.messagesCount = messagesCount
        return accident
    }

    private fun commonAccident(): Accident = when (status) {
        AccidentStatus.ACTIVE -> ActiveAccident(id, type, medicine, time, location, owner)
        AccidentStatus.ENDED  -> EndedAccident(id, type, medicine, time, location, owner)
        AccidentStatus.HIDDEN -> HiddenAccident(id, type, medicine, time, location, owner)
    }

    private fun ownedAccident(): Accident = when (status) {
        AccidentStatus.ACTIVE -> OwnedActiveAccident(id, type, medicine, time, location)
        AccidentStatus.ENDED  -> OwnedEndedAccident(id, type, medicine, time, location)
        AccidentStatus.HIDDEN -> OwnedHiddenAccident(id, type, medicine, time, location)
    }
}