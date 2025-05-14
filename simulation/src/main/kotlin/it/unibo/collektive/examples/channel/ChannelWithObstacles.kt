package it.unibo.collektive.examples.channel

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gradientCast

/**
 * The entrypoint of the program running a channel with obstacles.
 */
fun Aggregate<Int>.channelWithObstaclesEntrypoint(
    env: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): Boolean = channelWithObstacles(distanceSensor, env["source"], env["target"], env["obstacle"])

/**
 * Compute the channel between the [source] and the [target] avoiding [obstacle].
 */
fun Aggregate<Int>.channelWithObstacles(
    collektiveDevice: CollektiveDevice<*>,
    source: Boolean,
    target: Boolean,
    obstacle: Boolean,
): Boolean = !obstacle && channel(collektiveDevice, source, target, channelWidth = 0.5)

/**
 * Compute the channel between the [source] and the [destination] with a specific [channelWidth].
 */
fun Aggregate<Int>.channel(
    collektiveDevice: CollektiveDevice<*>,
    source: Boolean,
    destination: Boolean,
    channelWidth: Double,
): Boolean = with(collektiveDevice) {
    require(channelWidth.isFinite() && channelWidth > 0)
    val distances = distances()
    val toSource = distanceTo(source, metric = distances)
    val toDestination = distanceTo(destination, metric = distances)
    val sourceToDestination = broadcast(distances = distances, from = source, payload = toDestination)
    val channel = toSource + toDestination - sourceToDestination
    return if (channel.isFinite()) channel <= channelWidth else false
}

/**
 * Computes the [gradientCast] from the [source] with the [value] that is the distance from the [source] to the target.
 */
fun Aggregate<Int>.broadcast(distances: Field<Int, Double>, from: Boolean, payload: Double): Double = gradientCast(
    source = from,
    local = payload,
    metric = distances,
)
