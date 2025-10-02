package it.unibo.collektive.examples.inCircle

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Position
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.gradientCast
import it.unibo.collektive.stdlib.util.Point2D
import kotlin.math.pow

private const val RADIUS = 30.0

/**
 * Entry point for the program which computes whether the device is within a circle of a given radius.
 */
fun Aggregate<Int>.inCircleEntrypoint(env: EnvironmentVariables, collektiveDevice: CollektiveDevice<*>): Boolean =
    with(collektiveDevice) {
        inCircle(
            center = env["center"],
            p = environment.getPosition(collektiveDevice.node).toPoint2D(),
            metric = { distances() },
        )
    }

/**
 * Determines if the current device (located in [p]) is within a circle of a specified radius from a [center] point.
 */
fun Aggregate<Int>.inCircle(center: Boolean, p: Point2D, metric: () -> Field<Int, Double>): Boolean = with(p) {
    // Broadcast the center position to the whole network
    val centerPos = gradientCast(
        source = center,
        local = this,
        metric = metric(),
    )
    distanceToSquared(centerPos) <= RADIUS.pow(2)
}

/**
 * Computes the squared distance between this point and [other] point.
 */
private fun Point2D.distanceToSquared(other: Point2D): Double {
    val dx = x - other.x
    val dy = y - other.y
    return dx * dx + dy * dy
}

/**
 * Converts a [Position] to a [Point2D] collektive type.
 */
private fun Position<*>.toPoint2D(): Point2D = Point2D(coordinates[0] to coordinates[1])
