import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.voronoi.VoronoiKt.voronoiEntryPoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(20.0))
        deployments {
            deploy(grid(0.0, 0.0, 200.0, 200.0, 10.0, 10.0, 10.0, 10.0)) {
                program(program, 1)
                contents { "source"(false) }
            }
            listOf(point(20.0, 20.0), point(180.0, 180.0), point(20.0, 180.0), point(180.0, 20.0)).forEach { source ->
                deploy(source) {
                    program(program, 1)
                    contents { "source"(true) }
                }
            }
        }
        applyLaunchOptions()
    }
}
