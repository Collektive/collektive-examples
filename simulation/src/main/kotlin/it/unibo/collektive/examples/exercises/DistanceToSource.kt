package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.examples.exercises.searchSource

/**
 * Compute the [distances] between any node and the [source] using the adaptive bellman-ford algorithm.
*/
fun Aggregate<Int>.distanceToSource() = hopDistanceTo(searchSource())
