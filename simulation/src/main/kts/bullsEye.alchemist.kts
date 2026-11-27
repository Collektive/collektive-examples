import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(1.5))
        deployments {
            deploy(grid(0.0, 0.0, 20.0, 20.0, 1.0, 1.0, 0.3, 0.3)) {
                program("it.unibo.collektive.examples.bullsEye.BullsEyeKt.bullsEyeEntrypoint", 1)
            }
        }
        applyLaunchOptions()
    }
}
