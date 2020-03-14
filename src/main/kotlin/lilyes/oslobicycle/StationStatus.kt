package lilyes.oslobicycle

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class StationStatus(val stationId: String,
                         val numBikesAvailable: Int,
                         val numDocksAvailable: Int,
                         val isInstalled: Boolean
) {
    @JsonCreator
    constructor(@JsonProperty("station_id") stationId: String,
                @JsonProperty("num_bikes_available") numBikesAvailable: Int,
                @JsonProperty("num_docks_available") numDocksAvailable: Int,
                @JsonProperty("is_installed") isInstalled: Boolean,
                @JsonProperty("is_renting") isRenting: Boolean,
                @JsonProperty("is_returning") isReturning: Boolean) :
            this(stationId = stationId,
                    numBikesAvailable = if (isRenting) numBikesAvailable else 0,
                    numDocksAvailable = if (isReturning) numDocksAvailable else 0,
                    isInstalled = isInstalled) {

    }
}