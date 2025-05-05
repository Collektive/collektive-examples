package it.unibo.collektive.examples.counter

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.gradientCast

/**
 * Computes the hops from the source to the target.
 * If the device is the source, it returns 0.
 */
fun Aggregate<Int>.hopsFromSource(distances: Field<Int, Double>, source: Boolean): Double = gradientCast(
    source = source,
    local = 0.0,
    metric = distances,
    accumulateData = { _, _, data ->
        data + 1 // hops from source to me
    },
)

/**
 * [collektiveDevice] is a representation of the device that runs a Collektive program.
 * It is used to access the device's properties and methods,
 * such as the [distances] method, which returns a field of distances from the source.
 * In this case, the source is the device with [localId] 0.
 */
fun Aggregate<Int>.hopsFromSourceEntrypoint(collektiveDevice: CollektiveDevice<*>): Double = hopsFromSource(
    distances = with(collektiveDevice) { distances() },
    source = localId == 0,
)
