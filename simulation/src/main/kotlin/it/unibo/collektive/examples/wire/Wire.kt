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
            hasObstacleInNeighborhood && (!source && !destination) -> Vector2D(0.0 to 0.0)
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
    val toDest = distanceTo(destination, metric())
    val isOnShortestPath = shortestPath(source, toDest)
    return when {
        isOnShortestPath -> {
            val neighborDistances = neighboring(toDest)
            val minNeighborhoodDistance = neighborDistances.all.valueOfMinBy { (_, dist) -> dist }
            neighborDirectionVectors()
                .alignedMapValues(neighborDistances) { dir, dist ->
                    if (dist == minNeighborhoodDistance) dir else Vector2D(0.0 to 0.0)
                }
                .all
                .fold(Vector2D(0.0 to 0.0)) { acc, (_, v) -> acc + v }
        }
        else -> Vector2D(0.0 to 0.0)
    }
}

/**
 * Check whenever the current node is on the path from [src] to destination.
 * [d] is the distance to the destination.
 */
fun Aggregate<Int>.shortestPath(src: Boolean, d: Double): Boolean = share(false) { nbrIsPath ->
    val minId = with(neighboring(d).all) { minBy { (_, value) -> value }.id }
    val isOnShortestPath = neighboring(minId)
        .mapValues { it == localId }.and(nbrIsPath)
        .all
        .any { (_, value) -> value }
    when {
        src -> true
        else -> isOnShortestPath
    }
}
