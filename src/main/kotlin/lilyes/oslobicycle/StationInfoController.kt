package lilyes.oslobicycle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kong.unirest.Unirest
import kong.unirest.UnirestException
import kong.unirest.json.JSONObject

import java.lang.Exception

class StationInfoController {
    fun getStationInfo(): List<Station> {
        try{
            val stationStatusResponse = Unirest.get(STATUS_URL)
                    .header("Client-Identifier", IDENTIFIER)
                    .accept("application/json")
                    .asJson()

            val stationInfoResponse = Unirest.get(INFO_URL)
                    .header("Client-Identifier", IDENTIFIER)
                    .accept("application/json")
                    .asJson()

            if (stationStatusResponse.isSuccess && stationInfoResponse.isSuccess) {
                return processStations(stationInfoResponse.body.`object`, stationStatusResponse.body.`object`)
            } else {
                throw RequestFailedException("Failed to get info")
            }
        } catch (e: UnirestException){
            throw RequestFailedException("Failed to get info", e)
        }

    }

    private fun processStations(stationInfoJson: JSONObject, stationStatusJson: JSONObject): List<Station> {
        val stations = mutableListOf<Station>()

        val stationNames = processStationNames(stationInfoJson)
        val stationsStatus: List<StationStatus> = MAPPER.readValue(stationStatusJson.getJSONObject("data")
                .getJSONArray("stations").toString())

        for (status: StationStatus in stationsStatus){
            val name = stationNames[status.stationId] ?: continue//log this
            if(status.isInstalled){
                stations.add(Station(name, status.numBikesAvailable, status.numDocksAvailable))
            }

        }
        return stations
    }

    private fun processStationNames(stationInfoJson: JSONObject): Map<String, String> {
        val stationNames = mutableMapOf<String, String>()
        val stationsInfo = stationInfoJson.getJSONObject("data").getJSONArray("stations")
        for (x in 0 until stationsInfo.length()) {
            val stationInfo = stationsInfo.getJSONObject(x)
            stationNames[stationInfo.getString("station_id")] = stationInfo.getString("name")
        }
        return stationNames
    }


    companion object {
        const val IDENTIFIER = "lilyes - oslobicycle"
        const val STATUS_URL = "https://gbfs.urbansharing.com/oslobysykkel.no/station_status.json"
        const val INFO_URL = "https://gbfs.urbansharing.com/oslobysykkel.no/station_information.json"
        private val MAPPER = jacksonObjectMapper()
    }
}

class RequestFailedException(message: String, override val cause: Throwable?) : Exception(message, cause){
    constructor(message: String) : this(message, null)
}