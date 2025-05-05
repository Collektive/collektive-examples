package it.unibo.collektive.examples.counter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.accumulation.countDevices

/**
 * Count the number of devices in the network.
 * The total is accumulated in the device with [localId] 0
 * (where the [sink] is true).
 * Other devices will have a value that represents the number of devices
 * from self (included) to the closest edge of the network.
 */
fun Aggregate<Int>.deviceCounter(): Int = countDevices(sink = localId == 0)
