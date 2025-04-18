package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.max

/**
 * Identify the maximum ID values among the neighboring nodes.
*/
fun Aggregate<Int>.maxNeighborID(environment: EnvironmentVariables): Int = 
    mapNeighborhood { it }.max(localId).also { 
        environment["isMaxID"] = it == localId 
    }