import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.physics.environments.continuousPhysics2DEnvironment
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.wire.WireKt.wire"
val obstacles: List<Rectangle<Euclidean2DPosition>> = listOf(
    Rectangle(-20.0, -80.0, 10.0, 100.0),
    Rectangle(-50.0, 20.0, 80.0, 10.0),
)

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment(continuousPhysics2DEnvironment()) {
        networkModel(connectWithinDistance(15.0))
        deployments {
            deploy(grid(-100.0, -100.0, 100.0, 100.0, 7.0, 7.0)) {
                program(program, 1)
                contents {
                    "src"(false)
                    "dest"(false)
                    "obstacle"(false)
                    if (obstacles.any { position in it }) "obstacle"(true)
                }
            }
            deploy(point(-50.0, -50.0)) {
                program(program, 1)
                contents {
                    "src"(true)
                    "dest"(false)
                    "obstacle"(false)
                }
            }
            deploy(point(50.0, 50.0)) {
                program(program, 1)
                contents {
                    "src"(false)
                    "dest"(true)
                    "obstacle"(false)
                }
            }
        }
        applyLaunchOptions()
    }
}
