package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.fields.fold
import it.unibo.collektive.stdlib.spreading.multiGradientCast
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Runs a multi-source proximity chat using [multiGradientCast].
 *
 * Each node computes its distance to all sources, identified with [isSource].
 * Nodes within [REACHABLE] hear the full [message], nodes within [THRESHOLD] receive a faint version,
 * and nodes beyond [THRESHOLD] report the message as "Unreachable".
 *
 * Returns a map from source name to the received [Message] with content and distance.
 */
fun Aggregate<Int>.chatMultipleSources(
    distances: Field<Int, Double>,
    isSource: Boolean,
    message: String = "Hello",
): Map<Int, Message> {
    val sources: Set<Int> = share(emptySet<Int>()) { neighborSources ->
        neighborSources.fold(emptySet<Int>()) { accumulator, neighborEntry ->
            accumulator + neighborEntry.value
        }.let { collected ->
            if (isSource) collected + localId else collected
        }
    }

    val multiState: Map<Int, Double> = multiGradientCast(
        sources = sources,
        local = if (localId in sources) 0.0 else POSITIVE_INFINITY,
        metric = distances,
        accumulateData = { fromSource, toNeighbor, _ -> fromSource + toNeighbor },
    )

    val content: MutableMap<Int, Message> = mutableMapOf()
    multiState.forEach { (name, dist) ->
        content[name] = FadedMessage(message, dist)
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
fun Aggregate<Int>.chatMultipleEntryPoint(
    environment: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): String {
    val distances = with(distanceSensor) { distances() }
    val isSource = environment.get<Boolean>("source")
    return chatMultipleSources(distances, isSource).toString()
}
