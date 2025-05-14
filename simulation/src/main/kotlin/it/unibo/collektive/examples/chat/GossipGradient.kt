package it.unibo.collektive.examples.chat

import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share

/**
 * Data structure representing a state in a gossip-based gradient algorithm.
 *
 * This structure is used to propagate and compute the [path] from the source
 * to all the nodes in a distributed system.
 * [localDistance] is the initial local distance of the nodes, and [distance]
 * represents the current estimated distance for the [path].
 */
data class GossipGradient<ID : Comparable<ID>>(
    val distance: Double,
    val localDistance: Double,
    val path: List<ID> = emptyList(),
) {
    /**
     * Reset gossip to start from the local value of the specified node [id].
     */
    fun base(id: ID) = GossipGradient(localDistance, localDistance, listOf(id))

    /**
     * Add a new hop [id] to the path, update the distance with [newBest] and the
     * localDistance with [localDistance].
     */
    fun addHop(newBest: Double, localDistance: Double, id: ID) = GossipGradient(
        distance = newBest,
        localDistance = localDistance,
        path = path + id,
    )
}

/**
 * Computes the minimum gradient distance from a [source] node to all other nodes in a distributed system
 * using gossip-based communication. The function iteratively propagates [distances] information across
 * neighbors while avoiding loops in the paths.
 * It stabilizes to the minimal distance once information has been fully shared.
 */
fun Aggregate<Int>.gossipGradient(distances: Field<Int, Double>, target: Int): Double {
    val isSource = localId == target
    val localDistance = if (isSource) 0.0 else Double.POSITIVE_INFINITY
    val localGossip = GossipGradient(
        distance = localDistance,
        localDistance = localDistance,
        path = listOf(localId),
    )

    val result = share(localGossip) { neighborsGossip: Field<Int, GossipGradient<Int>> ->
        var bestGossip = localGossip
        val neighbors = neighborsGossip.toMap().keys

        for ((neighborId, neighborGossip) in neighborsGossip.toMap()) {
            val recentPath = neighborGossip.path.asReversed().drop(1)
            val pathIsValid = recentPath.none { it == localId || it in neighbors }
            val nextGossip = if (pathIsValid) neighborGossip else neighborGossip.base(neighborId)

            val neighborDistance = distances[neighborId]
            val totalDistance = nextGossip.distance + neighborDistance

            if (totalDistance < bestGossip.distance) {
                bestGossip = nextGossip.addHop(totalDistance, localGossip.localDistance, localId)
            }
        }
        bestGossip
    }
    return result.distance
}
