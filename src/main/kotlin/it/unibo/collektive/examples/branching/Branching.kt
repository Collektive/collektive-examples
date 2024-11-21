package it.unibo.collektive.examples.branching

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.neighboringViaExchange
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.hood

/**
 * A simple example of branching.
 */
fun Aggregate<Int>.branching(environment: EnvironmentVariables) =
    if (environment["source"]) {
        0
    } else {
        neighboringViaExchange(1).hood(0) { acc, _ -> acc + 1 }
    }
