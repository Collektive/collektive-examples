import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.actions.brownianMove
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.physics.environments.continuousPhysics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.genericReaction
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.track.TrackKt.trackEntrypoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment(continuousPhysics2DEnvironment()) {
        networkModel(connectWithinDistance(50.0))
        deployments {
            deploy(grid(-100.0, -100.0, 100.0, 100.0, 20.0, 20.0, 20.0, 20.0)) {
                program(program, 1)
                contents {
                    "target"(false)
                    "dst"(false)
                }
            }
            // Two random walkers: the target and the destination.
            listOf(point(20.0, 20.0) to true, point(-20.0, -20.0) to false).forEach { (where, isTarget) ->
                deploy(where) {
                    program(program, 1)
                    withTimeDistribution(1) {
                        program(genericReaction()) { action(brownianMove(5.0)) }
                    }
                    contents {
                        "target"(isTarget)
                        "dst"(!isTarget)
                    }
                }
            }
        }
        applyLaunchOptions()
    }
}
