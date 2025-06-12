package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Runs a multi-source proximity chat using [gossipGradient].
 *
 * Each node computes its distance to all sources, identified with [isSource].
 * Nodes within [maxDistance] hear the [content],
 * and nodes beyond [maxDistance] are excluded.
 * The messages have a lifetime equal to [lifeTime].
 * Returns a map from source name to the received [Message] with content and distance.
 */
fun Aggregate<Int>.chatMultipleSources(
    distances: Field<Int, Double>,
    isSource: Boolean,
    currentTime: Double,
    content: String,
    lifeTime: Double,
    maxDistance: Double,
): Map<Int, Message> {
    /*
    Gossip‐share self‐stabilizing of the sources
     */
    val localSources: Set<Int> = if (isSource) setOf(localId) else emptySet()
    val sources: Set<Int> = share(localSources) { neighborSets: Field<Int, Set<Int>> ->
        neighborSets.neighborsValues.fold(localSources) { accumulator, neighborSet ->
            accumulator + neighborSet
        }
    }
    /*
    Compute [gossipGradient] for each source
     */
    val distancesToEachSource = mutableMapOf<Int, Double>()
    for (sourceId in sources) {
        alignedOn(sourceId) {
            val distance = gossipGradient(distances, sourceId)
            distancesToEachSource[sourceId] = if (currentTime <= lifeTime) distance else Double.POSITIVE_INFINITY
        }
    }

    val message: MutableMap<Int, Message> = mutableMapOf()
    distancesToEachSource.filter { it.value < maxDistance }.forEach { (source, distanceFromSource) ->
        message[source] = Message(content, distanceFromSource)
    }
    return message
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
    val currentTime = distanceSensor.currentTime.toDouble()
    val lifeTime = 100.0
    val content = "Hello"
    val maxDistance = 10.0

    val message = chatMultipleSources(
        distances,
        isSource,
        currentTime,
        content,
        lifeTime,
        maxDistance,
    )
    return message.toString()
}
