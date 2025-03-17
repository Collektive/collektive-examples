package it.unibo.collektive.examples.branching

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.hood

/**
 * A simple example of branching.
 */

fun Aggregate<Int>.branching(environment: EnvironmentVariables) =
    when (environment.get<Boolean>("source")) {
        true -> 0
         /**
         * Counts neighbors, starting from the initial value 0.
         * `hood(initialValue) { lambda }` reduces values from neighbors:
         * 1. initialValue: starting value for the reduction
         * 2. lambda: aggregation function combining neighbor values
         */
        false -> neighboring(1).hood(0) { acc, _ -> acc + 1 }
    }
