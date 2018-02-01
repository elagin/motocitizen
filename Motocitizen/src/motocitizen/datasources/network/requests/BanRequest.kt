package motocitizen.datasources.network.requests

import motocitizen.datasources.network.ApiRequestWithAuth
import motocitizen.datasources.network.ApiResponse
import motocitizen.datasources.network.Methods

class BanRequest(id: Int, callback: (ApiResponse) -> Unit) : ApiRequestWithAuth(callback = callback) {
    override val method: String = Methods.BAN

    init {
        params["id"] = id.toString()
        call()
    }
}