import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.deployments.rectangle
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.multiGradient.MultiGradientKt.multiGradientEntryPoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(5.0))
        deployments {
            deploy(rectangle(198, 0.0, 0.0, 20.0, 20.0)) {
                program(program, 1)
                contents { "source"(false) }
            }
            listOf(point(10.0, 10.0), point(15.0, 15.0)).forEach { source ->
                deploy(source) {
                    program(program, 1)
                    contents { "source"(true) }
                }
            }
        }
        applyLaunchOptions()
    }
}
