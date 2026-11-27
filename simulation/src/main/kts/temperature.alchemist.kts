import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.temperature.TemperatureKt.temperatureEntrypoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(20.0))
        deployments {
            deploy(grid(0.0, 0.0, 200.0, 200.0, 10.0, 10.0, 10.0, 10.0)) {
                program(program, 1)
                contents {
                    "heat_source"(false)
                    "cold_source"(false)
                }
            }
            listOf(point(100.0, 100.0), point(50.0, 100.0)).forEach { heatSource ->
                deploy(heatSource) {
                    program(program, 1)
                    contents {
                        "heat_source"(true)
                        "cold_source"(false)
                    }
                }
            }
            listOf(point(0.0, 0.0), point(200.0, 0.0), point(0.0, 200.0), point(200.0, 200.0)).forEach { coldSource ->
                deploy(coldSource) {
                    program(program, 1)
                    contents {
                        "cold_source"(true)
                        "heat_source"(false)
                    }
                }
            }
        }
        applyLaunchOptions()
    }
}
