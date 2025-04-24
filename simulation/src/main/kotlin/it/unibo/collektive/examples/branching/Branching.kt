package it.unibo.collektive.examples.branching

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.neighbors.neighborCounter

/**
 * A simple example of branching.
 */
fun Aggregate<Int>.branching(environment: EnvironmentVariables): Int {
    val count = neighborCounter()
    return when (environment.get<Boolean>("source")) {
        true -> 0
        false -> count
    }
}
