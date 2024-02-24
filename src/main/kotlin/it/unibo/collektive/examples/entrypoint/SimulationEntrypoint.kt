package it.unibo.collektive.examples.entrypoint

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.examples.gradient.gradient
import it.unibo.collektive.examples.neighbors.neighborCounter
import it.unibo.collektive.examples.channel.channel

/**
 * The entrypoint of the simulation running a gradient.
 */
context(DistanceSensor)
fun Aggregate<Int>.gradientEntrypoint(): Double = gradient(localId == 0)

/**
 * The entrypoint of the simulation running a counter of neighbors.
 */
fun Aggregate<Int>.neighborCounterEntrypoint(): Int = neighborCounter()

context(DistanceSensor)
fun Aggregate<Int>.channelEntrypoint(): Boolean {
    return if (localId !in 181..199) {
        return channel(localId == 0, localId == 399, 0.1)
    } else false
}
