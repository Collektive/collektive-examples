package it.unibo.collektive.examples.gradient

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.field.min
import it.unibo.collektive.field.plus

/**
 * Extension function to evaluate the gradient in an [Aggregate] context.
 */
context(DistanceSensor)
fun Aggregate<Int>.gradient(): Double =
    share(Double.POSITIVE_INFINITY) { dist ->
        val paths = distances()
        when (localId) {
            0 -> 0.0
            else -> (paths + dist).min(Double.POSITIVE_INFINITY)
        }
    }
