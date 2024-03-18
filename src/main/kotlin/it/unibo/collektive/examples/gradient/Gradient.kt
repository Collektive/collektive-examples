package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.field.min
import it.unibo.collektive.field.plus
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Extension function to evaluate the gradient in an [Aggregate] context.
 */
context(DistanceSensor)
fun Aggregate<Int>.gradient(source: Boolean): Double =
    share(POSITIVE_INFINITY) {
        val dist = distances()
        when {
            source -> 0.0
            else -> (it + dist).min(POSITIVE_INFINITY)
        }
    }
