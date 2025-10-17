package it.unibo.collektive.examples.wire

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.utils.Vector2D
import it.unibo.collektive.examples.utils.coordinates
import it.unibo.collektive.examples.utils.pointTo
import it.unibo.collektive.examples.utils.vectorZero
import it.unibo.collektive.stdlib.booleans.FieldedBooleans.and
import it.unibo.collektive.stdlib.collapse.any
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.collapse.minBy
import it.unibo.collektive.stdlib.collapse.valueOfMinBy
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * Wire the source and the destination with connections to the next hop on the shortest path, avoiding obstacles.
 */
fun Aggregate<Int>.wire(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Unit =
    with(collektiveDevice) {
        val source: Boolean = env["src"]
        val destination: Boolean = env["dest"]
        val obstacle: Boolean = env["obstacle"]
        val hasObstacleInNeighborhood = neighboring(obstacle).all.any { it.value }
        val position = coordinates()
        val connectionDir = when {
            hasObstacleInNeighborhood && (!source && !destination) -> vectorZero
            else -> connect(
                source = source,
                destination = destination,
                metric = { distances() },
                neighborDirectionVectors = {
                    neighboring(position).alignedMapValues(mapNeighborhood { position }) { p, newO -> p - newO }
                },
            )
        }
        pointTo(connectionDir)
    }

/**
 * Connect [source] to [destination] using the given [metric] to measure distances
 * and [neighborDirectionVectors] to get the direction to each neighbor.
 * This function computes the direction of the next hop on the path from [source] to [destination].
 * If the current node is not on the path from [source] to [destination], return the zero vector.
 */
fun Aggregate<Int>.connect(
    source: Boolean,
    destination: Boolean,
    metric: () -> Field<Int, Double>,
    neighborDirectionVectors: () -> Field<Int, Vector2D>,
): Vector2D {
    val toDestination = distanceTo(destination, metric())
    val isOnShortestPath = shortestPath(source, toDestination)
    return when {
        isOnShortestPath -> {
            val neighborDistances = neighboring(toDestination)
            val minNeighborhoodDistance = neighborDistances.all.valueOfMinBy { (_, dist) -> dist }
            neighborDirectionVectors()
                .alignedMapValues(neighborDistances) { dir, dist ->
                    if (dist == minNeighborhoodDistance) dir else vectorZero
                }
                .all
                .fold(vectorZero) { acc, (_, v) -> acc + v }
        }
        else -> vectorZero
    }
}

/**
 * Check whenever the current node is on the path from [source] to destination.
 * [toDestination] is the distance to the destination.
 */
fun Aggregate<Int>.shortestPath(source: Boolean, toDestination: Double): Boolean = share(false) { nbrIsPath ->
    val minId = neighboring(toDestination).all.minBy { (_, value) -> value }.id
    val isOnShortestPath = neighboring(minId)
        .mapValues { it == localId }.and(nbrIsPath)
        .all
        .any { (_, value) -> value }
    when {
        source -> true
        else -> isOnShortestPath
    }
}
