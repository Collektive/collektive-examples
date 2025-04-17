package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Computes a proximity-based message propagation using aggregate computing.
 *
 * The message is emitted by a node marked as a [source] and propagates through neighbors,
 * with the perceived message degrading linearly based on [distances].
 * Nodes within [REACHABLE] hear the full [message], nodes within [THRESHOLD] receive a faint version,
 * and nodes beyond [THRESHOLD] report the message as "Unreachable".
 *
 */
fun Aggregate<Int>.chatSingleSource(
    distances: Field<Int, Double>,
    source: Boolean,
    message: String = "Hello",
): Message {
    val state = share(POSITIVE_INFINITY) {
        when {
            source -> 0.0
            else -> (it + distances).min(POSITIVE_INFINITY)
        }
    }
    return FadedMessage(message, state)
}

/**
 * Entry point for the proximity chat simulation.
 *
 * Extracts whether the current node is a source using the [environment] variable,
 * and delegates to [chatSingleSource] using the provided [distanceSensor].
 */
fun Aggregate<Int>.chatSingleEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): String {
    val distances = with(distanceSensor) { distances() }
    return chatSingleSource(distances, environment["source"]).toString()
}
