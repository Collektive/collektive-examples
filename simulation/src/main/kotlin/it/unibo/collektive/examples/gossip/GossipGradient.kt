package it.unibo.collektive.examples.gossip

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Data structure representing a state in a gossip-based gradient algorithm.
 *
 * This structure is used to propagate and compute the [path] from the source
 * to all the nodes in a distributed system.
 * [localDistance] is the initial local distance of the nodes, and [distance]
 * represents the current estimated distance for the [path].
 */
data class GradientGossip<ID : Comparable<ID>>(
    val distance: Double,
    val localDistance: Double,
    val path: List<ID> = emptyList(),
) {
    /**
     * Reset gossip to start from the local value of the specified node [id].
     */
    fun base(id: ID) = GradientGossip(localDistance, localDistance, listOf(id))

    /**
     * Add a new hop [id] to the path, update the distance with [newBest] and the
     * localDistance with [localDistance]
     */
    fun addHop(newBest: Double, localDistance: Double, id: ID) = GradientGossip(
        distance = newBest,
        localDistance = localDistance,
        path = path + id,
    )
}

/**
 * Computes the minimum gradient distance from a [source] node to all other nodes in a distributed system
 * using gossip-based communication. The function iteratively propagates [distances] information across
 * neighbors while avoiding loops in the paths. It stabilizes to the minimal distance once information has been fully shared.
 */
fun Aggregate<Int>.gossipGradient(distances: Field<Int, Double>, source: Boolean): Double {
    val localDistance = if (source) 0.0 else Double.POSITIVE_INFINITY
    val localGossip = GradientGossip<Int>(
        distance = localDistance,
        localDistance = localDistance,
        path = listOf(localId),
    )

    val result = share(localGossip) { neighborsGossip: Field<Int, GradientGossip<Int>> ->
        var bestGossip = localGossip
        val neighbors = neighborsGossip.neighbors.toSet()

        for ((id, neighborGossip) in neighborsGossip.toMap()) {
            val recentPath = neighborGossip.path.asReversed().drop(1)
            val pathIsValid = recentPath.none { it == localId || it in neighbors }
            val nextGossip = if (pathIsValid) neighborGossip else neighborGossip.base(id)

            val neighborDistance = distances[id]
            val totalDistance = nextGossip.distance + neighborDistance

            if (totalDistance < bestGossip.distance) {
                bestGossip = nextGossip.addHop(totalDistance, localGossip.localDistance, localId)
            }
        }
        bestGossip
    }
    return result.distance
}

/**
 * Uses the [environment] to determine source status,
 * and the [distanceSensor] to compute [distances] between nodes.
 * Returns the distance of the node from the [source].
 *
 */
fun Aggregate<Int>.gossipGradientEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): Double {
    val distances: Field<Int, Double> = with(distanceSensor) { distances() }
    return when (val isSource: Any = environment["source"]) {
        is String -> gossipGradient(distances, isSource.toBoolean())
        is Boolean -> gossipGradient(distances, isSource)
        else -> 0.0
    }
}
