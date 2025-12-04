package it.unibo.collektive.examples.track

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.channel.channel
import it.unibo.collektive.examples.utils.Vector2D
import it.unibo.collektive.examples.utils.coordinates
import it.unibo.collektive.examples.utils.pointTo
import it.unibo.collektive.examples.utils.vectorZero
import it.unibo.collektive.stdlib.spreading.gradientCast
import it.unibo.collektive.stdlib.util.Point2D

private const val CHANNEL_WIDTH = 10.0

/**
 * Entrypoint for the tracking program.
 */
fun Aggregate<Int>.trackEntrypoint(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Boolean =
    with(collektiveDevice) {
        val target: Boolean = env["target"]
        val destination: Boolean = env["dst"]
        // Check if the device is part of the channel between target and dst
        val inChannel = channel(collektiveDevice, target, destination, CHANNEL_WIDTH)
        // Compute the direction to the target, relative to dst
        val toTarget = track(target, destination, inChannel, coordinates()) { distances() }
        pointTo(toTarget)
        // Return whether the device is in the channel between target and dst
        inChannel
    }

/**
 * Computes the direction to the [target], relative to [destination].
 * If the device is not in the [channel], it returns a zero vector.
 * If the device is in the [channel] but not [destination], it returns a zero vector.
 * If the device is [destination], it returns the vector pointing to the [target].
 */
fun Aggregate<Int>.track(
    target: Boolean,
    destination: Boolean,
    channel: Boolean,
    coordinates: Point2D,
    metric: () -> Field<Int, Double>,
): Vector2D = when {
    channel -> {
        // Broadcast the target's coordinates to the channel
        val targetCoordinates = gradientCast(
            source = target,
            local = coordinates,
            metric = metric(),
        )
        if (destination) targetCoordinates - coordinates else vectorZero
    }

    else -> vectorZero
}
