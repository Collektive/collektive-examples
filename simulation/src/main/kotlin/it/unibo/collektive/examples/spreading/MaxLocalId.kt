package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.fields.maxIDBy
import it.unibo.collektive.stdlib.util.IncludingSelf

/** Identify the maximum ID values among the neighboring nodes. */
fun Aggregate<Int>.maxNeighborID(environment: EnvironmentVariables): Int =
    checkNotNull(neighborhood().maxIDBy(IncludingSelf) { it.id })
        .also { environment["isMaxID"] = localId == it }
