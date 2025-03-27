package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Identify the maximum ID values among the neighboring nodes.
 * 
 * Assign a distinct color to the nodes with the identified maximum local ID values.
*/

fun Aggregate<Int>.maxNeighborID(environment: EnvironmentVariables): Int = 
    neighboring(localId).max(localId).also { 
        environment["isMaxID"] = it == localId 
    }