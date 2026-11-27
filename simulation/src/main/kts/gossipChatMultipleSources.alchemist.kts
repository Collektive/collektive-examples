import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.reactions.genericReaction
import it.unibo.collektive.examples.utils.applyLaunchOptions

val incarnation = CollektiveIncarnation<GeoPosition>()

simulationOnMap(incarnation) {
    environment(OSMEnvironment(incarnation, "vcm.pbf", false)) {
        networkModel(connectWithinDistance(3000.0))
        deployments {
            deploy(FromGPSTrace(50, "vcmuser.gpx", true, "AlignToSimulationTime")) {
                contents { "source"(true) }
                program("it.unibo.collektive.examples.chat.GossipChatMultipleSourcesKt.gossipChatMultipleSourcesEntrypoint", 1)
                // The YAML scenario attached the GPS-trace movement twice (once nested in the program pool,
                // once explicitly): kept as is to preserve its behaviour.
                repeat(2) {
                    withTimeDistribution(1) {
                        program(genericReaction()) {
                            action(reproduceGPSTrace("vcmuser.gpx", true, "AlignToSimulationTime"))
                        }
                    }
                }
            }
        }
        applyLaunchOptions()
    }
}
