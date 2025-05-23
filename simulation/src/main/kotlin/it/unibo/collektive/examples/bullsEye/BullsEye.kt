package it.unibo.collektive.examples.bullsEye

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.gossipMin
import kotlin.math.abs
import kotlin.math.hypot

/** Draws a bullseye pattern based on network distances and node positions.
 * This function identifies two distant nodes (extremes) in the network to define a main axis,
 * then computes an approximate center point (intersection of two diagonals), 
 * and finally assigns a value based on the distance from this central node, creating 
 * concentric zones.
 * The returned value is intended for visualization (e.g., as a color gradient from 0 to 100),
 * allowing the rendering of a bullseye pattern across the network.*/
fun Aggregate<Int>.bullsEye(metric: Field<Int, Double>): Int {
    val colors = listOf(25, 75, 50, 0, 85)
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
        in 0.0..1.0 -> colors[0]
        in 1.0..4.0 -> colors[1]
        in 4.0..7.0 -> colors[2]
        in 7.0..10.0 -> colors[3]
        else -> colors[4]
    }
}

/** Executes the bullsEye and prepares its result for visualization. */
fun Aggregate<Int>.bullsEyeEntrypoint(simulatedDevice: CollektiveDevice<*>): Int {
    return bullsEye(with(simulatedDevice) { distances() })
}
