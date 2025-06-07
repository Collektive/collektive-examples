package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * Computes a proximity-based message propagation using aggregate computing.
 *
 * The message is emitted by a node marked as a [source] and propagates through neighbors,
 * with the perceived message degrading linearly based on [distances].
 * Nodes within [PERFECTLY_REACHABLE] hear the full [message],
 * nodes within [ALMOST_UNREACHABLE] receive a faint version,
 * and nodes beyond [ALMOST_UNREACHABLE] report the message as "Unreachable".
 *
 */
fun Aggregate<Int>.chatSingleSource(
    distances: Field<Int, Double>,
    source: Boolean,
    message: String = "Hello",
): Message {
    val state = distanceTo(source, distances)
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
    val distances: Field<Int, Double> = with(distanceSensor) { distances() }
    val isSource = environment.get<Boolean>("source")
    return chatSingleSource(distances, isSource).toString()
}
