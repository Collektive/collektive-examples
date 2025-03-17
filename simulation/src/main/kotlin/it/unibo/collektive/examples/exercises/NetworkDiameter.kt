package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.examples.exercises.distanceToSource
import it.unibo.collektive.examples.channel.broadcast
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.field.operations.maxBy
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * 3) Calculate in the source an estimate of the true diameter of the network (the maximum distance of a device in the network).
 * 4) Broadcast the diameter to every node in the network.
*/ 

fun Aggregate<Int>.networkDiameter(environment: EnvironmentVariables, distanceSensor: DistanceSensor): Int {
    // Individuate source and calculate distance to source from the previous exercises
    distanceToSource(environment)

    // Calculate the distance of the local minimum for each neighborhood field
    val distance: Int = environment["distanceToSource"]

    // Identifies the node with the maximum number of hops to the source
    val maxHopToSource = neighboring(distance).max(distance)

    // Identifies the node furthest from the source, i.e. the most peripheral node
    environment["furthest"] = isMaxValue(maxHopToSource, environment["distanceToSource"])

    // Calculate distance to furthest node from the source in the network
    val distanceToFurthest = distanceToFurthest(environment["furthest"])

    // Identifies the node with the maximum number of hops corresponding to the diameter of the entire network
    val flagNodeWithMaxHopToFurthest = isMaxValue(environment["distanceToFurthest"])

    environment["distanceToFurthest"] = distanceToFurthest

    // Only the identified node will broadcast its maximum number of hops to the entire network.
    var broadcastMessage = broadcast(distanceSensor, from = flagNodeWithMaxHopToFurthest, payload = environment["distanceToFurthest"]).toInt()

    if(distanceToFurthest <= broadcastMessage){
        environment["networkDiameter"] = broadcastMessage
    }else{
        environment["networkDiameter"] = distanceToFurthest
    }

    environment["broadcastMessagePayload"] = broadcastMessage

    return localId
}

fun Aggregate<Int>.isMaxValue(value: Int, localValue: Int? = Int.MIN_VALUE): Boolean {
    // Find the maximum value in the neighborhood field
    val maxValue = neighboring(value).max(value)

    // localValue is an optional parameter to evaluate (during maximum identification) an additional local value of the node
    if(localValue != Int.MIN_VALUE){
        return maxValue == value && localValue == value
    }else{
        return maxValue == value
    }
}

fun Aggregate<Int>.distanceToFurthest(furthest: Boolean): Int {
    return distanceTo(
        furthest,                        
        0,                             
        Int.MAX_VALUE,             
        { a: Int, b: Int ->            
            if (a == Int.MAX_VALUE || b == Int.MAX_VALUE) Int.MAX_VALUE
            else (a + b).coerceAtMost(Int.MAX_VALUE) 
        }
    ) {
        neighboring(1)                 
    }
}

