package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables 

/**
 * Return the node identifier
*/
fun Aggregate<Int>.getLocalId(): Int {
    return localId
}