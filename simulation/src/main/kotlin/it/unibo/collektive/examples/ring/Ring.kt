package it.unibo.collektive.examples.ring

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Time
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gradientCast
import kotlin.math.abs

private const val WAVE_SPEED = 5.0
private const val WAVE_THICKNESS = 4.0
private const val WAVE_PERIOD = 40.0

/**
 * Entry point for the ring wave pattern.
 */
fun Aggregate<Int>.ringEntryPoint(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Boolean =
    with(collektiveDevice) {
        ring(
            center = env["center"],
            currentTime = { environment.simulation.time },
            metric = { distances() },
        )
    }

/**
 * Create a ring wave pattern originating from the [center] node.
 * The wave propagates outward from the center, with a speed defined by [WAVE_SPEED],
 * a thickness defined by [WAVE_THICKNESS], and a period defined by [WAVE_PERIOD].
 * It returns a boolean field indicating whether the ring is active at each node.
 */
private fun Aggregate<Int>.ring(center: Boolean, currentTime: () -> Time, metric: () -> Field<Int, Double>): Boolean =
    run {
        val waveTime = broadcastTime(center, currentTime, metric)
        val distance = distanceTo(center, metric = metric())
        isRingActive(waveTime, distance)
    }

/**
 * Broadcast the current time from the [center] to all other nodes in the network,
 * based on the given [metric].
 */
private fun Aggregate<Int>.broadcastTime(
    center: Boolean,
    currentTime: () -> Time,
    metric: () -> Field<Int, Double>,
): Time = gradientCast(
    source = center,
    local = currentTime(),
    metric = metric(),
)

/**
 * Check if the ring is active at the given [waveTime] and [distance] from the center.
 * A ring is active if the distance from the center is within the wave thickness.
 */
private fun isRingActive(waveTime: Time, distance: Double): Boolean =
    abs(WAVE_SPEED * (waveTime.toDouble() % WAVE_PERIOD) - distance) < WAVE_THICKNESS
