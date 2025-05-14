package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.fields.fold

/**
 * Runs a multi-source proximity chat using [gossipGradient].
 *
 * Each node computes its distance to all sources, identified with [isSource].
 * Nodes within [REACHABLE] hear the full [message], nodes within [THRESHOLD] receive a faint version,
 * and nodes beyond [THRESHOLD] are excluded.
 *
 * Returns a map from source name to the received [Message] with content and distance.
 */
fun Aggregate<Int>.chatMultipleSources(
    distances: Field<Int, Double>,
    isSource: Boolean,
    message: String = "Hello",
): Map<Int, Message> {
    /*
    Get the sources of the network
     */
    val sources: Set<Int> = share(emptySet()) { neighborSources ->
        neighborSources.fold(emptySet<Int>()) { accumulator, neighborEntry ->
            accumulator + neighborEntry.value
        }.let { collected ->
            if (isSource) collected + localId else collected
        }
    }
    /*
    Compute [gossipGradient] for each source
     */
    val distancesToEachSource = mutableMapOf<Int, Double>()
    for (sourceId in sources) {
        alignedOn(sourceId) {
            val distance = gossipGradient(distances, sourceId)
            distancesToEachSource[sourceId] = distance
        }
    }
    val content: MutableMap<Int, Message> = mutableMapOf()
    distancesToEachSource.filter { it.value < THRESHOLD }.forEach { (source, distance) ->
        content[source] = FadedMessage(message, distance)
    }
    return content
}

/**
 * Entrypoint for the multi-source chat simulation.
 *
 * Uses the [environment] to determine source status,
 * and the [distanceSensor] to compute [distances] between nodes.
 * Returns a printable string representation of the received messages.
 */
fun Aggregate<Int>.gossipChatMultipleSourcesEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): String {
    val distances: Field<Int, Double> = with(distanceSensor) { distances() }
    val isSource = environment.get<Boolean>("source")

    val message = chatMultipleSources(distances, isSource)
    return message.toString()
}
