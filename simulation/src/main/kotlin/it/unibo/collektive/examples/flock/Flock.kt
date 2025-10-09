package it.unibo.collektive.examples.flock

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.utils.Vector2D
import it.unibo.collektive.examples.utils.coordinates
import it.unibo.collektive.examples.utils.move
import it.unibo.collektive.examples.utils.normalize
import it.unibo.collektive.examples.utils.vdot
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.util.Point2D
import kotlin.math.PI

private const val VELOCITY = 0.1
private const val CONNECTIVITY_RADIUS = 30.0
private const val LEADER_DIRECTION_WEIGHT = 0.5
private const val FAR_NEIGHBOR_ATTRACTION_WEIGHT = 0.2
private const val CLOSE_NEIGHBOR_THRESHOLD = 5.0
private const val FAR_NEIGHBOR_THRESHOLD = 10.0

/**
 *
 */
fun Aggregate<Int>.flockEntryPoint(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Unit =
    with(collektiveDevice) {
        val leader: Boolean = env["leader"]
        val p = coordinates()
        // If the node is a leader move towards the origin, otherwise stay still
        val dir = p.normalize() * -LEADER_DIRECTION_WEIGHT * (if (leader) 1.0 else 0.0)
        val flockDir = flock(
            dir = dir,
            nbrRange = { distances() },
            nbrVec = { neighboring(p).alignedMapValues(mapNeighborhood { p }) { p, newO -> p - newO } },
        )
        // Move the node in the computed directions
        move(flockDir, VELOCITY)
    }

/**
 * Implements flocking behavior.
 * Each device computes a new direction based on its initial direction [dir]
 * (which points towards the origin if the device is a leader, or is zero otherwise),
 * the distance to its neighbors [nbrRange], and the directions pointing to its neighbors [nbrVec].
 * The behavior is defined as follows:
 * - If a neighbor is closer than 5 units, steer away from it.
 * - If a neighbor is farther than 10 units, steer slightly towards it.
 * - Otherwise, align with the neighbor.
 * The resulting direction is normalized and combined with the current direction.
 */
fun Aggregate<Int>.flock(
    dir: Vector2D,
    nbrRange: () -> Field<Int, Double>,
    nbrVec: () -> Field<Int, Vector2D>,
): Vector2D = share(Vector2D(0.0 to 0.0)) { nbrV ->
    val d = nbrV.alignedMapValues(nbrRange(), nbrVec()) { vel, dist, dir ->
        when {
            // steer away if too close
            dist > 0.0 && dist <= CLOSE_NEIGHBOR_THRESHOLD -> dir.normalize() * -1.0
            // steer slightly towards if too far
            dist > FAR_NEIGHBOR_THRESHOLD -> dir.normalize() * FAR_NEIGHBOR_ATTRACTION_WEIGHT
            // align if at a good distance
            else -> vel.normalize()
        }
    }.intHood(neighboring(spatialWeight(CONNECTIVITY_RADIUS))).normalize()
    val v = nbrV.local.value
    (dir + if (d vdot d > 0) d else v).normalize()
}

/**
 * Computes integral (weighted sum) of neighbor vectors in the field.
 * This function combines the directions from all neighbors by applying their respective
 * [weights] and summing the resulting vectors. .
 */
fun Field<Int, Vector2D>.intHood(weights: Field<Int, Double>): Vector2D =
    with(alignedMapValues(weights) { point, weight -> point * weight }.all) {
        fold(Vector2D(0.0 to 0.0)) { acc, entry -> acc + entry.value }
    }

/**
 * Computes the spatial weight of a device given a [radius].
 * The spatial weight is defined as the area of the circle with the given [radius]
 * divided by the number of devices in the neighborhood.
 * If there is only one device in the neighborhood, the spatial weight is equal to the area of the circle.
 */
fun Aggregate<Int>.spatialWeight(radius: Double): Double = with(neighborhood().all) {
    val countDevices = size
    val totalArea = PI * radius * radius
    totalArea / countDevices
}
