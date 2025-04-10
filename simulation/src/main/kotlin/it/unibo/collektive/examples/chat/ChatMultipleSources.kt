package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.fold
import it.unibo.collektive.stdlib.spreading.multiGradientCast
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Runs a multi-source proximity chat using [multiGradientCast].
 *
 * Each node computes its distance to all sources, identified via the [environment].
 * Nodes within [REACHABLE] hear the full [message], nodes within [THRESHOLD] receive a faint version,
 * and nodes beyond [THRESHOLD] report the message as "Unreachable".
 *
 * Returns a map from source name to the received [Message] with content and distance.
 */
fun Aggregate<Int>.chatMultipleSources(
    distanceSensor: DistanceSensor,
    environment: EnvironmentVariables,
    message: String = "Hello"
): Map<String, Message>{
    val isSource = environment.get<Boolean>("source")
    val sourceName = environment.getOrDefault("sourceName", "node")

    val idToName : Map<Int, String> = share(emptyMap<Int, String>()) { neighborSources ->
        neighborSources.fold(emptyMap<Int, String>()) { accumulator, neighborMap ->
            accumulator + neighborMap}.let { collected ->
            if (isSource) collected + (localId to sourceName) else collected
            }
        }

    val sources = idToName.keys
    val multiState : Map<Int, Double> = multiGradientCast(
        sources = sources,
        local = if (localId in sources) 0.0 else POSITIVE_INFINITY,
        metric = with(distanceSensor) { distances() },
        accumulateData = {fromSource, toNeighbor, _ -> fromSource + toNeighbor },
    )
    val transformedState : Map<String, Double> =  multiState
        .map {(key, value) -> idToName[key]!! to value}
        .toMap()

    val content : MutableMap<String, Message> = mutableMapOf()
    transformedState.forEach{ (name, dist) ->
        content[name] = FadedMessage(message, dist)
    }

    return content.toMap()
}

/**
 * Entrypoint for the multi-source chat simulation.
 *
 * Uses the [environment] to determine source status and name,
 * and the [distanceSensor] to compute distances between nodes.
 * Returns a printable string representation of the received messages.
 */
fun Aggregate<Int>.chatMultipleEntryPoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): String = chatMultipleSources(distanceSensor, environment).toString()