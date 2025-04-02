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
 * The message is emitted by a source node and propagates to neighbors,
 * with the perceived message degrading linearly as distance increases.
 * - Nodes within [REACHABLE] hear the full message.
 * - Nodes within [THRESHOLD] hear a faint version (with percentage).
 * - Nodes beyond [THRESHOLD] report the message as "Unreachable".
 *
 * @param distanceSensor used to get distances to neighbors
 * @param source whether this node is the source of the message
 * @param message the message to be propagated from the source
 * @return the received/degraded version of the message
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
 * Extracts the `source` flag from the environment and delegates
 * to [chatAlgorithm] to compute the node's message output.
 *
 * @param environment environment variables (used to check if node is a source)
 * @param distanceSensor used to get neighbor distances
 * @return the message seen by this node
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
 *
 * @param distance the computed gradient distance from the source
 * @return a percentage value representing the message strength (0–100)
 */
fun calculateFaint(distance: Double):Double{
    return (1.0 - (distance - REACHABLE)/ REACHABLE)*100
}