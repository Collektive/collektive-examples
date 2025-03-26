package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Identify the maximum ID values among the neighboring nodes.
 * 
 * Assign a distinct color to the nodes with the identified maximum local ID values.
 * 
 * Identify the maximum ID values in the network.
 * 
 * Assign a distinct color to the nodes with the identified maximum ID values in the network.
*/

fun Aggregate<Int>.maxNeighborID(): Int = neighboring(localId).max(localId)

fun Aggregate<Int>.maxNetworkID(environment: EnvironmentVariables): Int {
    val maxLocalValue = maxNeighborID()

    environment["isMaxLocalID"] = localId == maxLocalValue

    val maxValue = share(maxLocalValue){ previous ->
        previous.max(maxLocalValue)
    }

    environment["isMaxID"] = localId == maxValue
    
    return maxValue
}