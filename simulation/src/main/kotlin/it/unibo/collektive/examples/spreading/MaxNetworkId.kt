package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Identify the maximum ID values in the network.
 * 
 * Assign a distinct color to the nodes with the identified maximum ID values in the network.
*/

fun Aggregate<Int>.maxNetworkID(environment: EnvironmentVariables): Int =
    share(localId){ field ->
        field.max(localId)
    }.also { maxValue ->
        environment["isMaxID"] = localId == maxValue
    }