package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * Computes the hop distance from the closest [source].
 */
fun <ID : Any> Aggregate<ID>.hopDistanceTo(source: Boolean): Int = distanceTo(
    source = source,
    bottom = 0,
    top = Int.MAX_VALUE,
    accumulateDistance = { a, b ->
        if (a == Int.MAX_VALUE || b == Int.MAX_VALUE) Int.MAX_VALUE else (a + b).coerceAtMost(Int.MAX_VALUE)
    },
    metric = { neighboring(1) },
)