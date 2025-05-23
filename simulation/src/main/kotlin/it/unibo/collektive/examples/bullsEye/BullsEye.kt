package it.unibo.collektive.examples.bullsEye

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.gossipMin
import kotlin.math.abs
import kotlin.math.hypot

// Constants for colors
private const val COLOR_CENTER = 25
private const val COLOR_INNER_RING = 75
private const val COLOR_MID_RING = 50
private const val COLOR_OUTER_RING = 0
private const val COLOR_BEYOND = 85

// Distance thresholds
private const val DISTANCE_CENTER = 1.0
private const val DISTANCE_INNER_RING = 4.0
private const val DISTANCE_MID_RING = 7.0
private const val DISTANCE_OUTER_RING = 10.0

/** Draws a bullseye pattern based on network distances and node positions.
 * This function identifies two distant nodes (extremes) in the network to define a main axis,
 * then computes an approximate center point (intersection of two diagonals),
 * and finally assigns a value based on the distance from this central node, creating
 * concentric zones.
 * The returned value is intended for visualization (e.g., as a color gradient from 0 to 100),
 * allowing the rendering of a bullseye pattern across the network.*/
fun Aggregate<Int>.bullsEye(metric: Field<Int, Double>): Int {
    // Creates a gradient from a randomly chosen node (using gossipMin), measuring
    // distances based on the provided metric.
    val distToRandom = distanceTo(gossipMin(localId) == localId, metric = metric)
    // Finds the node that is farthest from the random starting node. This will serve
    // as the first “extreme” of the network.
    val firstExtreme = gossipMax(distToRandom to localId, compareBy { it.first }).second
    // Builds a distance gradient starting from the first extreme node.
    val distanceToExtreme = distanceTo(firstExtreme == localId, metric = metric)
    // Finds the node that is farthest from the first extreme.
    // This defines the other end of the main axis (the second “extreme”).
    val (distanceBetweenExtremes, otherExtreme) =
        gossipMax(distanceToExtreme to localId, compareBy { it.first })
    // Builds a distance gradient from the second extreme.
    val distanceToOtherExtreme = distanceTo(otherExtreme == localId, metric = metric)
    // Approximates the center of the network by computing the intersection of
    // diagonals between the two extremes, and finds the closest node to that point.
    val distanceFromMainDiameter =
        abs(distanceBetweenExtremes - distanceToExtreme - distanceToOtherExtreme)
    val distanceFromOpposedDiagonal = abs(distanceToExtreme - distanceToOtherExtreme)
    val approximateDistance = hypot(distanceFromOpposedDiagonal, distanceFromMainDiameter)
    val centralNode = gossipMin(approximateDistance to localId, compareBy { it.first }).second
    // Measures how far each node is from the computed center.
    val distanceFromCenter = distanceTo(centralNode == localId)
    return when (distanceFromCenter) {
        in 0.0..DISTANCE_CENTER -> COLOR_CENTER
        in DISTANCE_CENTER..DISTANCE_INNER_RING -> COLOR_INNER_RING
        in DISTANCE_INNER_RING..DISTANCE_MID_RING -> COLOR_MID_RING
        in DISTANCE_MID_RING..DISTANCE_OUTER_RING -> COLOR_OUTER_RING
        else -> COLOR_BEYOND
    }
}

/** Executes the bullsEye and prepares its result for visualization. */
fun Aggregate<Int>.bullsEyeEntrypoint(simulatedDevice: CollektiveDevice<*>) =
    bullsEye(with(simulatedDevice) { distances() })
