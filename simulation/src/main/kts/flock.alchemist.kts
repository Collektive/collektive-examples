import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.graphStreamDeployment
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

val program = "it.unibo.collektive.examples.flock.FlockKt.flockEntryPoint"

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(30.0))
        deployments {
            // Each flock: a random graph of followers around a corner, and two leaders.
            listOf(
                (-150.0 to -150.0) to (-140.0 to -140.0),
                (150.0 to -150.0) to (140.0 to -140.0),
                (-150.0 to 150.0) to (-140.0 to 140.0),
                (150.0 to 150.0) to (140.0 to 140.0),
            ).forEach { (corner, inner) ->
                val followers = graphStreamDeployment(20, corner.first, corner.second, 5.0, "RandomGenerator")
                // The YAML loader adds the links of GraphStream deployments, the DSL does not.
                followers.getAssociatedLinkingRule<Any?>()?.let { networkModel(it) }
                deploy(followers) {
                    program(program, 1)
                    contents { "leader"(false) }
                }
                listOf(corner, inner).forEach { (x, y) ->
                    deploy(point(x, y)) {
                        program(program, 1)
                        contents { "leader"(true) }
                    }
                }
            }
        }
        applyLaunchOptions()
    }
}
