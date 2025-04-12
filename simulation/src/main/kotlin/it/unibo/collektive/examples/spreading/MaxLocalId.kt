package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Identify the maximum ID values among the neighboring nodes.
*/
fun Aggregate<Int>.maxNeighborID(environment: EnvironmentVariables): Int = 
    neighboring(localId).max(localId).also { 
        environment["isMaxID"] = it == localId 
    }