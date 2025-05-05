package it.unibo.collektive.examples.counter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.accumulation.countDevices

/**
 * Count the number of devices in the network.
 * The total is accumulated in the device with [localId] 0 (the [sink]).
 * Other devices hold the number of devices in their subtree towards the network edge, inlcuding themselves.
 * A leaf node (with no outward neighbors) will hold 1.
 */
fun Aggregate<Int>.deviceCounter(): Int = countDevices(sink = localId == 0)
