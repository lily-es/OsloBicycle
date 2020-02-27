package lilyes.oslobicycle

import javafx.stage.Stage
import tornadofx.*

class BikeApp: App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minWidth= 400.0
        super.start(stage)
    }
}