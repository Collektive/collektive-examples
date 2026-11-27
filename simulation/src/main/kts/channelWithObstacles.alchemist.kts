import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.contains
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val channelWidth = 0.5
val program = "it.unibo.collektive.examples.channel.ChannelWithObstaclesKt.channelWithObstaclesEntrypoint"
val obstacles: List<Rectangle<Euclidean2DPosition>> = listOf(
    Rectangle(83.0, 36.0, 10.0, 150.0),
    Rectangle(0.0, 30.0, 170.0, 10.0),
    Rectangle(5.0, 160.0, 80.0, 10.0),
    Rectangle(7.0, 74.0, 80.0, 8.0),
)

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(10.0))
        deployments {
            deploy(grid(0.0, 0.0, 200.0, 200.0, 7.0, 7.0, 0.5, 0.5)) {
                contents {
                    "channelWidth"(channelWidth)
                    "obstacle"(false)
                    "source"(false)
                    "target"(false)
                    if (obstacles.any { position in it }) "obstacle"(true)
                }
                program(program, 1)
            }
            deploy(point(3.0, 3.0)) {
                contents {
                    "channelWidth"(channelWidth)
                    "source"(true)
                    "target"(false)
                    "obstacle"(false)
                }
                program(program, 1)
            }
            deploy(point(75.0, 60.0)) {
                contents {
                    "channelWidth"(channelWidth)
                    "target"(true)
                    "source"(false)
                    "obstacle"(false)
                }
                program(program, 1)
            }
        }
        applyLaunchOptions()
    }
}
