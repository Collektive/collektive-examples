package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.examples.exercises.distanceToSource
import it.unibo.collektive.examples.channel.broadcast
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.stdlib.spreading.gossipMax

/**
 * Calculate in the [source] an estimate of the true [diameter] of the network 
 * (the maximum distance of a device in the network).
 * Broadcast the [diameter] to every node in the network.
*/ 
fun Aggregate<Int>.networkDiameter(distanceSensor: CollektiveDevice<*>): Int {
    val isFurthest = isMaxValue(distanceToSource(distanceSensor))
    val distanceToFurthest = hopDistanceTo(isFurthest)
    val flagNodeWithMaxHopToFurthest = isMaxValue(distanceToFurthest)
    val broadcastMessage = broadcast(
        distanceSensor, 
        from = flagNodeWithMaxHopToFurthest, 
        payload = distanceToFurthest.toDouble()
    ).toInt()
    return when {
        distanceToFurthest <= broadcastMessage -> broadcastMessage
        else -> distanceToFurthest
    }
}

/**
 * Function that identifies the [maximum value] and returns true if the passed value 
 * is the maximum.
 */
fun Aggregate<Int>.isMaxValue(localValue: Int): Boolean = gossipMax(localValue).let { maxValue ->
    localValue == maxValue
}
