package it.unibo.collektive.examples.inCircle

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Position
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.chat.gossipGradient
import it.unibo.collektive.stdlib.spreading.gradientCast
import kotlin.math.pow

private const val RADIUS = 30.0

fun Aggregate<Int>.inCircleEntrypoint(
    env: EnvironmentVariables,
    collektiveDevice: CollektiveDevice<*>
): Boolean =
    with(collektiveDevice) {
        inCircle(
            center = env["center"],
            p = environment.getPosition(collektiveDevice.node),
            metric = { distances() }
        )
    }

fun Aggregate<Int>.inCircle(
    center: Boolean,
    p: Position<*>,
    metric: () -> Field<Int, Double>
): Boolean =
    with(p) {
        // Broadcast the center position to the whole network
        val centerPos = gradientCast(
            source = center,
            local = this,
            metric = metric()
        )
        distanceToSquared(centerPos) <= RADIUS.pow(2)
    }


/**
 * @param other the other vector,
 * @return the dot product between this and [other].
 * @throws IllegalArgumentException if the vectors have different sizes
 */
private fun DoubleArray.dot(other: DoubleArray): Double {
    require(size == other.size) { "Vector must have same dimension." }
    return zip(other) { a, b -> a * b }.sum()
}

/**
 * @param other the other position
 * @return the squared distance between this position and [other]
 */
private fun Position<*>.distanceToSquared(other: Position<*>): Double {
    val diff = coordinates.zip(other.coordinates) { c1, c2 -> c1 - c2 }.toDoubleArray()
    return diff.dot(diff)
}


