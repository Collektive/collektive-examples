package it.unibo.collektive.examples.gradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * The entrypoint of the simulation running a gradient, considering the device with id 0 as the source.
 */
fun Aggregate<Int>.gradientEntrypoint(distanceSensor: CollektiveDevice<*>): Double = with(distanceSensor) {
    distanceTo(localId == 0, metric = distances())
}
