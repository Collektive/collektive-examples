package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Maximum distance at which the message is clearly received.
 */
public const val REACHABLE: Double = 5.0
/**
 * Maximum distance at which the message can still be faintly perceived.
 */
public const val THRESHOLD: Double = 10.0

/**
 * Computes a proximity-based message propagation using aggregate computing.
 *
 * The message is emitted by a node marked as a [source] and propagates through neighbors,
 * with the perceived message degrading linearly based on distance.
 * Nodes within [REACHABLE] hear the full [message], nodes within [THRESHOLD] receive a faint version,
 * and nodes beyond [THRESHOLD] report the message as "Unreachable".
 *
 * The [distanceSensor] is used to measure neighbor distances.
 */
fun Aggregate<Int>.chatAlgorithm(distanceSensor: DistanceSensor, source: Boolean, message: String = "Hello"): String {
    val state = share(POSITIVE_INFINITY){
        val dist = with(distanceSensor) {distances()}
        when{
            source -> 0.0
            else -> (it + dist).min(POSITIVE_INFINITY)
        }
    }
    return when{
        state <= REACHABLE -> message
        state < THRESHOLD -> "$message ${"%.0f".format(calculateFaint(state))}%"
        else -> "Unreachable"
    }
}


/**
 * Entry point for the proximity chat simulation.
 *
 * Extracts whether the current node is a source using the [environment] variable,
 * and delegates to [chatAlgorithm] using the provided [distanceSensor].
 */
fun Aggregate<Int>.chatAlgorithmEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): String = chatAlgorithm(distanceSensor, environment["source"])

/**
 * Computes the perceived intensity (faintness) of a message based on distance.
 *
 * The result is a percentage from 100 (fully clear) to 0 (barely understandable).
 * Intended to be used when distance is between [REACHABLE] and [THRESHOLD].
 */
fun calculateFaint(distance: Double):Double{
    return (1.0 - (distance - REACHABLE)/ REACHABLE)*100
}