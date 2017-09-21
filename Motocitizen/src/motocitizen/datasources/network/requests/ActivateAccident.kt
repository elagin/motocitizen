package motocitizen.datasources.network.requests

import motocitizen.datasources.network.ApiRequestWithAuth
import motocitizen.datasources.network.ApiResponse
import motocitizen.datasources.network.Methods

class ActivateAccident(accidentId: Int, callback: (ApiResponse) -> Unit) : ApiRequestWithAuth(callback) {
    init {
        params.put("m", Methods.ACTIVATE_ACCIDENT.code)
        params.put("id", accidentId.toString())
        call()
    }
}