package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.collapse.maxBy

/** Identify the maximum ID values among the neighboring nodes. */
fun Aggregate<Int>.maxNeighborID(environment: EnvironmentVariables): Int = neighborhood().all.maxBy { it.id }.id
    .also { environment["isMaxID"] = localId == it }
