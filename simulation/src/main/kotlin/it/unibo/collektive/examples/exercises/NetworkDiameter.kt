package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.examples.exercises.distanceToSource
import it.unibo.collektive.examples.channel.broadcast
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.stdlib.spreading.hopDistanceTo

/**
 * Calculate in the [source] an estimate of the true [diameter] of the network (the maximum distance of a device in the network).
 * 
 * Broadcast the [diameter] to every node in the network.
*/ 
fun Aggregate<Int>.networkDiameter(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): Int {
    val isFurthest = isMaxValue(distanceToSource(environment))

    val distanceToFurthest = hopDistanceTo(isFurthest)

    val flagNodeWithMaxHopToFurthest = isMaxValue(distanceToFurthest)

    val broadcastMessage = broadcast(distanceSensor, from = flagNodeWithMaxHopToFurthest, payload = distanceToFurthest.toDouble()).toInt()

    var networkDiameter = distanceToFurthest
    if(distanceToFurthest <= broadcastMessage){
        networkDiameter = broadcastMessage
    }
    
    return networkDiameter
}

/**
 * Function that identifies the [maximum value] and returns true if the passed value is the maximum.
 */
fun Aggregate<Int>.isMaxValue(value: Int): Boolean {
    val maxValue = share(value){ field ->
        field.max(value)
    }
    return maxValue == value
}