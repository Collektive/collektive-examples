package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import it.unibo.collektive.stdlib.fields.minValue
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Given the [distances] from the source, this function computes the gradient from the [source] to self.
 */
fun Aggregate<Int>.gradient(
    distances: Field<Int, Double>,
    source: Boolean,
): Double =
    share(POSITIVE_INFINITY) { field ->
        val minGradient = (field + distances).minValue(POSITIVE_INFINITY)
        when {
            source -> 0.0
            else -> minGradient
        }
    }

/**
 * The entrypoint of the simulation running a gradient, considering the device with id 0 as the source.
 */
fun Aggregate<Int>.gradientEntrypoint(distanceSensor: CollektiveDevice<*>): Double =
    with(distanceSensor) {
        gradient(
            distances = distances(),
            source = localId == 0,
        )
    }
