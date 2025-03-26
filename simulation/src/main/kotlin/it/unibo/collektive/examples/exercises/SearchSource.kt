package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Select a node called [source], chosen by finding the node with [minimum uid] 
 * in the network, assuming that the diameter of the network is no more than 10 hops.
*/

fun Aggregate<Int>.minNeighborId(): Int = neighboring(localId).min(localId)

fun Aggregate<Int>.searchSource(environment: EnvironmentVariables): Int {
    val minLocalValue = minNeighborId()

    val minValue = share(minLocalValue){ previous ->
        previous.min(minLocalValue)
    }

    environment["isSource"] = localId == minValue

    return minValue
}