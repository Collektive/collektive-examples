package it.unibo.collektive.examples.exercises

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.examples.channel.broadcast
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.hopDistanceTo

/** Calculate in the source an estimate of the true diameter of the network
 * (the maximum distance of a device in the network).
 * Broadcast the diameter to every node in the network. */
fun Aggregate<Int>.networkDiameter(distanceSensor: CollektiveDevice<*>): Int {
    val distanceToSource = distanceToSource(distanceSensor)
    val isFurthest = gossipMax(distanceToSource) == distanceToSource
    val distanceToFurthest = hopDistanceTo(isFurthest)
    val flagNodeWithMaxHopToFurthest = gossipMax(distanceToFurthest) == distanceToFurthest
    val broadcastMessage = broadcast(
        distances = with(distanceSensor) { distances() },
        from = flagNodeWithMaxHopToFurthest,
        payload = distanceToFurthest.toDouble(),
    ).toInt()
    return when {
        distanceToFurthest <= broadcastMessage -> broadcastMessage
        else -> distanceToFurthest
    }
}
