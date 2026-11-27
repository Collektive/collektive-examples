import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.graphStreamDeployment
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.collektive.examples.utils.applyLaunchOptions

simulation2D(CollektiveIncarnation<Euclidean2DPosition>()) {
    environment {
        networkModel(connectWithinDistance(2.0))
        deployments {
            val graph = graphStreamDeployment(15, 2.0, 0.0, "PreferentialAttachment")
            // The YAML loader adds the links of GraphStream deployments, the DSL does not.
            graph.getAssociatedLinkingRule<Any?>()?.let { networkModel(it) }
            deploy(graph) {
                program("it.unibo.collektive.examples.diameter.NetworkDiameterKt.networkDiameter", 1)
            }
        }
        applyLaunchOptions()
    }
}
