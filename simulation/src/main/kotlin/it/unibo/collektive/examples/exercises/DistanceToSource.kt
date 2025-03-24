package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.examples.exercises.searchSource

/**
 * Compute the [distances] between any node and the [source] using the adaptive bellman-ford algorithm.
*/

fun Aggregate<Int>.distanceToSource(environment: EnvironmentVariables): Int {
    // Individuate source from the previous exercise 
    searchSource(environment)

    // Calculate the hop distance to the source
    environment["distanceToSource"] = hopDistanceTo(environment["source"])
    return environment["distanceToSource"]
}