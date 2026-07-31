package it.unibo.collektive.examples.channel

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gradientCast

/**
 * The entrypoint of the program running a channel with obstacles.
 */
context(collektiveDevice: CollektiveDevice<*>)
fun Aggregate<Int>.channelWithObstaclesEntrypoint(): Boolean = with(collektiveDevice) {
    channelWithObstacles(collektiveDevice["source"], collektiveDevice["target"], collektiveDevice["obstacle"])
}

/**
 * Compute the channel between the [source] and the [target] avoiding [obstacle].
 */
context(collektiveDevice: CollektiveDevice<*>)
fun Aggregate<Int>.channelWithObstacles(source: Boolean, target: Boolean, obstacle: Boolean): Boolean =
    !obstacle && channel(source, target, channelWidth = 0.5)

/**
 * Compute the channel between the [source] and the [destination] with a specific [channelWidth].
 */
context(collektiveDevice: CollektiveDevice<*>)
fun Aggregate<Int>.channel(source: Boolean, destination: Boolean, channelWidth: Double): Boolean =
    with(collektiveDevice) {
        require(channelWidth.isFinite() && channelWidth > 0)
        val distances = distances()
        val toSource = distanceTo(source, metric = distances)
        val toDestination = distanceTo(destination, metric = distances)
        val sourceToDestination = gradientCast(source, toDestination, distances) // broadcast
        val channel = toSource + toDestination - sourceToDestination
        return channel.isFinite() && channel <= channelWidth
    }
