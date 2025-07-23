package it.unibo.collektive.examples.chat

import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share

/**
 * Data structure representing a state in a gossip-based gradient algorithm.
 *
 * This structure is used to propagate and compute the [path] from the source
 * to all the nodes in a distributed system and a [content] message.
 * [localDistance] is the initial local distance of the nodes, and [distance]
 * represents the current estimated distance for the [path].
 */
data class GossipGradient<ID : Comparable<ID>>(
    val distance: Double,
    val localDistance: Double,
    val content: String,
    val path: List<ID> = emptyList(),
) {
    /**
     * Reset gossip to start from the local value of the specified node [id].
     */
    fun base(id: ID) = GossipGradient(localDistance, localDistance, content, listOf(id))

    /**
     * Add a new hop [id] to the path, update the distance with [newBest] and the
     * localDistance with [localDistance].
     */
    fun addHop(newBest: Double, localDistance: Double, id: ID) = GossipGradient(
        distance = newBest,
        localDistance = localDistance,
        content = content,
        path = path + id,
    )
}

/**
 * Computes the minimum gradient distance from a source node to all other nodes in a distributed system
 * using gossip-based communication with [GossipGradient].
 *
 * The function iteratively propagates [distances] information across
 * neighbors while avoiding loops in the paths.
 * It stabilizes to the minimal distance once information has been fully shared.
 * Broadcasts a message containing the provided [content] to all nodes within the given [maxDistance]
 * for the defined [lifeTime].
 */
fun Aggregate<Int>.gossipGradient(
    distances: Field<Int, Double>,
    target: Int,
    isSource: Boolean,
    currentTime: Double,
    content: String,
    lifeTime: Double,
    maxDistance: Double,
): Message? {
    /*
    Indicate if the current node is the target of the gradient calculation.
     */
    val isTargetNode = localId == target
    /*
    Only broadcast content if this node is both the target of the gradient calculation
    and is a source.
     */
    val localContent = if (isTargetNode && isSource) content else ""
    /*
    If the node is the target of the gradient calculation initialize its distance as 0.0 (from itself).
     */
    val localDistance = if (isTargetNode) 0.0 else Double.POSITIVE_INFINITY
    val localGossip = GossipGradient(
        distance = localDistance,
        localDistance = localDistance,
        content = localContent,
        path = listOf(localId),
    )

    val distanceMap = distances.toMap()
    val result = share(localGossip) { neighborsGossip: Field<Int, GossipGradient<Int>> ->
        var bestGossip = localGossip
        val neighbors = neighborsGossip.toMap().keys

        for ((neighborId, neighborGossip) in neighborsGossip.toMap()) {
            val recentPath = neighborGossip.path.asReversed().drop(1)
            val pathIsValid = recentPath.none { it == localId || it in neighbors }
            val nextGossip = if (pathIsValid) neighborGossip else neighborGossip.base(neighborId)
            val totalDistance = nextGossip.distance + distanceMap.getOrDefault(neighborId, nextGossip.distance)
            if (totalDistance < bestGossip.distance && neighborGossip.content.isNotEmpty()) {
                bestGossip = nextGossip.addHop(totalDistance, localGossip.localDistance, localId)
            }
        }
        bestGossip
    }

    /*
    Filter the message before sharing it.
     */
    val message = Message(result.content, result.distance)

    return message.takeIf {
        currentTime <= lifeTime &&
            result.distance < maxDistance &&
            result.distance.isFinite() &&
            result.content.isNotEmpty() &&
            localId != target
    }
}
