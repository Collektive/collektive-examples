package it.unibo.collektive.examples.branching

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.hood

/**
 * A simple example of branching.
 */
fun Aggregate<Int>.branching(environment: EnvironmentVariables) = when (environment.get<Boolean>("source")) {
    true -> 0
    false -> neighboring(1).hood(0) { acc, _ -> acc + 1 }
}
