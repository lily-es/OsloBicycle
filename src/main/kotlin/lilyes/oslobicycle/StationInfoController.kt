package lilyes.oslobicycle

import tornadofx.*
import kong.unirest.Unirest
import kong.unirest.json.JSONException
import kong.unirest.json.JSONObject
import java.lang.Exception

class StationInfoController {
    val stationStatus = mutableListOf<Station>().asObservable()

    fun updateStationInfo() {
        val stationStatusResponse = Unirest.get(STATUS_URL)
                .header("Client-Identifier", IDENTIFIER)
                .accept("application/json")
                .asJson()

        val stationInfoResponse = Unirest.get(INFO_URL)
                .header("Client-Identifier", IDENTIFIER)
                .accept("application/json")
                .asJson()

        if (stationStatusResponse.isSuccess && stationInfoResponse.isSuccess) {
            val stationList = processStations(stationInfoResponse.body.`object`, stationStatusResponse.body.`object`)
            stationStatus.setAll(stationList)
        } else {
            throw RequestFailedException("Failed to get info")
        }
    }

    //might not be necessary to fetch the station names every time, but they might change at any time
    private fun processStations(stationInfoJson: JSONObject, stationStatusJson: JSONObject): List<Station> {
        val stationNames = processStationNames(stationInfoJson)
        val stations = mutableListOf<Station>()

        val stationsJson = stationStatusJson.getJSONObject("data").getJSONArray("stations")

        for (x in 0 until stationsJson.length()) {
            val stationStatus = stationsJson.getJSONObject(x)

            val stationName = stationNames[stationStatus.getString("station_id")] ?: continue //would also log

            if (stationStatus.getInt("is_installed") == 1) {
                val bikesAvailable = if (stationStatus.getInt("is_renting") == 1) {
                    stationStatus.getInt("num_bikes_available")
                } else {
                    0
                }

                val locksAvailable = if (stationStatus.getInt("is_returning") == 1) {
                    try {
                        stationStatus.getInt("num_docks_available")
                        //If key doesnt exist it means its a virtual station, which has unlimited locks(v2.0 RC)
                    } catch (e: JSONException) {
                        99
                    }
                } else {
                    0
                }
                //possible design choice, hide stations that have no available bikes and locks?
                stations.add(Station(stationName, bikesAvailable, locksAvailable))
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
    }
}

class RequestFailedException(message: String) : Exception(message)