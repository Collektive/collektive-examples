package it.unibo.collektive.examples.accumulating

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.accumulation.countDevices

/**
 * Count the devices in the network,
 *
 */
fun Aggregate<Int>.countDevicesEntrypoint(): Int = countDevices(sink = localId == 0)
