import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.inCircle.InCircleKt.inCircleEntrypoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(10.0))
        deployments {
            deploy(grid(0.0, 0.0, 200.0, 200.0, 5.0, 5.0, 1.0, 1.0)) {
                program(program, 1)
                contents { "center"(false) }
            }
            deploy(point(100.0, 100.0)) {
                program(program, 1)
                contents { "center"(true) }
            }
        }
        applyLaunchOptions()
    }
}
