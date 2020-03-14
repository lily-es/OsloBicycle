package lilyes.oslobicycle

import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.system.measureTimeMillis

class MainView() : View() {

    private val controller = StationInfoController()

    private val stations = mutableListOf<Station>().asObservable()

    private val button = button("Refresh") {
        action {
            tryUpdate()
        }
    }

    private val infoText = label("")
    private val table = tableview(stations) {
        val name = readonlyColumn("Name", Station::name).remainingWidth()
        val bikes = readonlyColumn("Available Bikes", Station::availableBikes)
        val locks = readonlyColumn("Available Locks", Station::availableLocks)

        name.isResizable = false
        name.isReorderable = false

        bikes.isResizable = false
        bikes.isReorderable = false

        locks.isResizable = false
        locks.isReorderable = false

        columnResizePolicy = SmartResize.POLICY
        vgrow = Priority.ALWAYS
    }

    override val root = vbox()

    init {
        title = "Bycycle Viewer"
        with(root) {
            this += button
            this += table
            this += infoText
        }
        tryUpdate()
    }


    private fun tryUpdate() {
        try {
            infoText.text = ""
            val milis = measureTimeMillis {
                stations.setAll(controller.getStationInfo())
            }
            infoText.text = "Fetched in ${milis / 1000.0}s"

        } catch (e: RequestFailedException) {
            infoText.text = FAIL_MESSAGE
        }
    }

    companion object {
        const val FAIL_MESSAGE = "Failed to get station information"
    }
}