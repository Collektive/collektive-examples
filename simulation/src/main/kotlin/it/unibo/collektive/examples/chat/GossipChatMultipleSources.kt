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
    content: String = "echo from node $localId",
    lifeTime: Double = 100.0,
    maxDistance: Double = 3000.0,
): Map<Int, Message> {
    /*
    Gossip‐share self‐stabilizing of the sources.
     */
    val localSources: Set<Int> = if (isSource) setOf(localId) else emptySet()
    val sources: Set<Int> = share(localSources) { neighborSets: Field<Int, Set<Int>> ->
        neighborSets.neighborsValues.fold(localSources) { accumulator, neighborSet ->
            accumulator + neighborSet
        }
    }
    /*
    Compute [gossipGradient] for each source.
     */
    val messages = mutableMapOf<Int, Message>()
    for (sourceId in sources) {
        alignedOn(sourceId) {
            val result = gossipGradient(
                distances = distances,
                target = sourceId,
                isSource = localId == sourceId && isSource,
                currentTime = currentTime,
                content = content,
                lifeTime = lifeTime,
                maxDistance = maxDistance,
            )

            result?.let { messages[sourceId] = result }
        }
    }

    return messages
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

    return chatMultipleSources(
        distances,
        isSource,
        currentTime,
    ).map { "${it.key}: ${it.value.content}" }
        .joinToString("\n")
}
