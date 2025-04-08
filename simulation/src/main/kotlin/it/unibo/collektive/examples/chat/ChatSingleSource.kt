package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import kotlin.Double.Companion.POSITIVE_INFINITY


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
fun Aggregate<Int>.chatSingleSource(distanceSensor: DistanceSensor, source: Boolean, message: String = "Hello"): Message {
    val state = share(POSITIVE_INFINITY){
        val dist = with(distanceSensor) {distances()}
        when{
            source -> 0.0
            else -> (it + dist).min(POSITIVE_INFINITY)
        }
    }
    val content =  when{
        state <= REACHABLE -> message
        state < THRESHOLD -> "$message ${"%.0f".format(calculateFaint(state))}%"
        else -> "Unreachable"
    }
    return Message(content, state)
}

/**
 * Entry point for the proximity chat simulation.
 *
 * Extracts whether the current node is a source using the [environment] variable,
 * and delegates to [chatSingleSource] using the provided [distanceSensor].
 */
fun Aggregate<Int>.chatSingleEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): String = chatSingleSource(distanceSensor, environment["source"]).toString()

