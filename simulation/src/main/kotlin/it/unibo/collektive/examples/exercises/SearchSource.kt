package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * 1) Select a node called "source", chosen by finding the node with minimum uid 
 * in the network, assuming that the diameter of the network is no more than 10 hops.
*/

fun Aggregate<Int>.minNeighborID(): Int {
    // Exchange the localId with neighbors and obtain a field of values
    val neighborValues = neighboring(local = localId)

    // Find the minimum value among neighbors (including self)
    val maxValue = neighborValues.min(base = localId)

    return maxValue
}

fun Aggregate<Int>.searchSource(environment: EnvironmentVariables): Int {
    val minLocalValue = minNeighborID()

    // Exchange the minLocalValue with neighbors and obtain a field of values
    val neighborValues = neighboring(local = minLocalValue)

    // Find the maximum value among neighbors (including self)
    val minValue = neighborValues.min(base = minLocalValue)

    // Assign the result to a molecule (only if using Alchemist)
    environment["source"] = localId == minValue
    environment["localMinID"] = minValue

    // The program return localId assigned at nodes label in simulation
    return localId
}