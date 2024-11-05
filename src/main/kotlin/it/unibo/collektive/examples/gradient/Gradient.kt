package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.min
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
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

/**
 * The entrypoint of the simulation running a gradient.
 */
context(EnvironmentVariables,DistanceSensor)
fun Aggregate<Int>.gradientEntrypoint(): Double = gradient(get("source"))
