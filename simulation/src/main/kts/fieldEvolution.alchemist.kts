import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.rectangle
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(5.0))
        deployments {
            deploy(rectangle(200, 0.0, 0.0, 20.0, 20.0)) {
                program("it.unibo.collektive.examples.fieldEvolution.FieldEvolutionKt.fieldEvolution", 1)
            }
        }
        applyLaunchOptions()
    }
}
