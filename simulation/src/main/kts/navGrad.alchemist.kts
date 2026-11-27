import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.graphStreamDeployment
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.navGrad.NavGradKt.navGradEntrypoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(50.0))
        deployments {
            deploy(grid(-100.0, -100.0, 100.0, 100.0, 30.0, 30.0, 30.0, 30.0)) {
                program(program, 1)
                contents {
                    "is-mover"(false)
                    "source"(false)
                }
            }
            deploy(point(100.0, 100.0)) {
                program(program, 1)
                contents {
                    "is-mover"(false)
                    "source"(true)
                }
            }
            val movers = graphStreamDeployment(10, -60.0, -60.0, 20.0, "RandomGenerator")
            // The YAML loader adds the links of GraphStream deployments, the DSL does not.
            movers.getAssociatedLinkingRule<Any?>()?.let { networkModel(it) }
            deploy(movers) {
                program(program, 1)
                contents {
                    "is-mover"(true)
                    "source"(false)
                }
            }
        }
        applyLaunchOptions()
    }
}
