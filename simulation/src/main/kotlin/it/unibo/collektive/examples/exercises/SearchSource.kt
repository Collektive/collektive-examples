package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

/**
 * Select a node called [source], chosen by finding the node with [minimum uid] 
 * in the network, assuming that the diameter of the network is no more than 10 hops.
*/
fun Aggregate<Int>.searchSource(environment: EnvironmentVariables): Int =
    share(localId){ field ->
        field.min(localId)
    }.also { minValue ->
        environment["isSource"] = localId == minValue
    }
