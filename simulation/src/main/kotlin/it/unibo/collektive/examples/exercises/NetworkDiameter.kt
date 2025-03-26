package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.examples.exercises.distanceToSource
import it.unibo.collektive.examples.channel.broadcast
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.stdlib.spreading.hopDistanceTo

/**
 * Calculate in the [source] an estimate of the true [diameter] of the network (the maximum distance of a device in the network).
 * Broadcast the diameter to every node in the network.
*/ 

fun Aggregate<Int>.networkDiameter(environment: EnvironmentVariables, distanceSensor: DistanceSensor): Int {
    val distanceToSource = distanceToSource(environment)

    val distance: Int = distanceToSource

    val maxHopToSource = neighboring(distance).max(distance)

    val isFurthest = isMaxValue(maxHopToSource, distanceToSource)

    val distanceToFurthest = hopDistanceTo(isFurthest)

    val flagNodeWithMaxHopToFurthest = isMaxValue(distanceToFurthest)

    val broadcastMessage = broadcast(distanceSensor, from = flagNodeWithMaxHopToFurthest, payload = distanceToFurthest.toDouble()).toInt()

    var networkDiameter = distanceToFurthest
    if(distanceToFurthest <= broadcastMessage){
        networkDiameter = broadcastMessage
    }

    return networkDiameter
}

fun Aggregate<Int>.isMaxValue(value: Int, localValue: Int? = Int.MIN_VALUE): Boolean {
    val maxValue = neighboring(value).max(value)

    if(localValue != Int.MIN_VALUE){
        return maxValue == value && localValue == value
    }else{
        return maxValue == value
    }
}