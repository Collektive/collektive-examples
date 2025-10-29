package it.unibo.collektive.examples.navGrad

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.utils.coordinates
import it.unibo.collektive.examples.utils.magnitude
import it.unibo.collektive.examples.utils.move
import it.unibo.collektive.examples.utils.normalize
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.collapse.valueOfMinBy
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.minus
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.util.Point2D
import kotlin.math.abs

typealias Vector2D = Point2D

private const val VELOCITY = 0.2

/**
 * Entry point for the navigation gradient example.
 */
fun Aggregate<Int>.navGradEntrypoint(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Double =
    with(collektiveDevice) {
        val p = coordinates()
        val isMover: Boolean = env["is-mover"]
        val source: Boolean = env["source"]
        // Compute the direction to move in.
        val dir = navGrad(
            mover = isMover,
            source = source,
            neighborDistances = { distances() },
            neighborDirectionVectors = {
                neighboring(p).alignedMapValues(mapNeighborhood { p }, { p, newO -> p - newO })
            },
        )
        move(dir, VELOCITY)
        // Return the distance to the source for visualization purposes.
        distanceTo(source = source, metric = distances())
    }

/**
 * Computes the navigation gradient vector for a node, determining its direction of movement.
 * The function's behavior depends on the [mover] and [source] boolean field:
 * - If a node is a [mover], the function computes a normalized vector pointing towards the [source]
 *   by following the gradient of a potential field. This vector represents the optimal direction of movement.
 * - If a node is **not** a [mover] or if the gradient is zero, the function returns a zero vector,
 *   indicating that no movement should occur.
 * The calculation relies on the [neighborDistances] and [neighborDirectionVectors] functions to obtain the distances
 * and vectors pointing to neighboring nodes, respectively.
 */
fun Aggregate<Int>.navGrad(
    mover: Boolean,
    source: Boolean,
    neighborDistances: () -> Field<Int, Double>,
    neighborDirectionVectors: () -> Field<Int, Vector2D>,
): Vector2D = shareDistanceTo(!mover, source, neighborDistances).let { distance ->
    val g = grad(distance, neighborDistances, neighborDirectionVectors)
    when {
        mover && g.magnitude() > 0.0 -> g.normalize()
        else -> Vector2D(0.0 to 0.0)
    }
}

/**
 * Share the distance from the [isCalculating] or [source] to all non [isCalculating] nodes.
 * The [neighborDistances] function provides the distances to neighboring nodes.
 * The function returns the computed distance as a Double.
 */
fun Aggregate<Int>.shareDistanceTo(
    isCalculating: Boolean,
    source: Boolean,
    neighborDistances: () -> Field<Int, Double>,
): Double {
    // Compute the distance to the source
    val toSource = distanceTo(source, neighborDistances())
    // If this node is calculating, it uses its own distance to source.
    // Otherwise, its distance is considered infinite.
    val myDist = if (isCalculating) toSource else Double.POSITIVE_INFINITY
    // Compute the potential dist for each neighbor
    // by adding the distance from the neighbor to its distance to the source.
    val potentialDist = neighborDistances() + neighboring(myDist)
    // Find the minimum distance among all neighbors.
    val minDistance = potentialDist.all.valueOfMinBy { (_, value) -> value }
    // If the node is calculating, return its own distance; otherwise, return the minimum distance found.
    return if (isCalculating) myDist else minDistance
}

/**
 * Compute the gradient of a scalar field [v].
 * The gradient is calculated using the differences in the values of [v] between the node and its neighbors,
 * as well as the distances and directions to those neighbors.
 * The [neighborDistances] function provides the distances to neighboring nodes,
 * and the [neighborDirectionVectors] function provides the vectors pointing to neighboring nodes.
 * The function returns the gradient as a [Vector2D].
 */
fun Aggregate<Int>.grad(
    v: Double,
    neighborDistances: () -> Field<Int, Double>,
    neighborDirectionVectors: () -> Field<Int, Vector2D>,
): Vector2D {
    // Compute the difference in the value of v between this node and its neighbors.
    val differences = mapNeighborhood { v } - neighboring(v)
    // Get vectors pointing to neighbors.
    val directions = neighborDirectionVectors()
    // Get the distances to neighbors.
    val distances = neighborDistances()
    // Combine the differences, coordinates, and distances to compute the gradient vector.
    return distances.alignedMapValues(differences, directions, { dist, diff, dir ->
        when {
            dist == 0.0 || !(abs(diff) < Double.POSITIVE_INFINITY) -> Vector2D(0.0 to 0.0)
            else -> dir.normalize() * (diff / dist)
        }
    }).all.run {
        fold(Vector2D(0.0 to 0.0)) { acc, (_, value) -> acc + value } / size.toDouble()
    }
}
