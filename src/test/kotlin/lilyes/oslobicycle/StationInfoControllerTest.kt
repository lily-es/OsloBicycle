package lilyes.oslobicycle

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import kong.unirest.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class StationInfoControllerTest {

    @MockK
    private lateinit var getRequestStatus: GetRequest

    @MockK
    private lateinit var getRequestInfo: GetRequest

    @MockK
    private lateinit var getRequest: GetRequest

    @MockK
    private lateinit var httpResponse: HttpResponse<JsonNode>

    @MockK
    private lateinit var httpResponseStatus: HttpResponse<JsonNode>

    @MockK
    private lateinit var httpResponseInfo: HttpResponse<JsonNode>

    @Test
    fun `test updating station info works`() {
        mockkStatic(Unirest::class)

        every { Unirest.get(StationInfoController.STATUS_URL) } returns getRequestStatus
        every { getRequestStatus.header(any(), any()) } returns getRequestStatus
        every { getRequestStatus.accept(any()) } returns getRequestStatus
        every { getRequestStatus.asJson() } returns httpResponseStatus
        every { httpResponseStatus.isSuccess } returns true

        every { Unirest.get(StationInfoController.INFO_URL) } returns getRequestInfo
        every { getRequestInfo.header(any(), any()) } returns getRequestInfo
        every { getRequestInfo.accept(any()) } returns getRequestInfo
        every { getRequestInfo.asJson() } returns httpResponseInfo
        every { httpResponseInfo.isSuccess } returns true

        every { httpResponseInfo.body } returns stationInfoJson
        every { httpResponseStatus.body } returns stationStatusJson

        val controller = StationInfoController()

        val stations = listOf<Station>(
                Station("Skøyen Stasjon", 7, 5),
                Station("7 Juni Plassen", 4, 8),
                Station("Sotahjørnet", 4, 9)
        )

        assertEquals(stations, controller.getStationInfo())
    }

    @Test
    fun `test updating station info throws correct exceptions`() {
        mockkStatic(Unirest::class)
        every { Unirest.get(any()) } returns getRequest
        every { getRequest.header(any(), any()) } returns getRequest
        every { getRequest.accept(any()) } returns getRequest
        every { getRequest.asJson() } throws UnirestException("")

        val controller = StationInfoController()

        assertThrows<RequestFailedException> { controller.getStationInfo() }

        every { getRequest.asJson() } returns httpResponse
        every { httpResponse.isSuccess } returns false

        assertThrows<RequestFailedException> { controller.getStationInfo() }
    }

    companion object {
        private val stationInfoJson = JsonNode(File("src/test/resources/stationInfo.json").readText())
        private val stationStatusJson = JsonNode(File("src/test/resources/stationStatus.json").readText())
    }

}