package it.unibo.collektive.examples.counter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.hopDistanceTo

/**
 * Gossips the maximum [localId] in the network,
 * if the device has the max [localId], it returns true.
 */
fun Aggregate<Int>.isMaxId() = gossipMax(localId) == localId

/**
 * The entrypoint of the simulation.
 * It computes the hops from the source to self.
 * The source is the device with the maximum [localId].
 * It uses the [env] from the simulation for visualization purposes.
 */
fun Aggregate<Int>.hopsFromSourceEntrypoint(env: EnvironmentVariables): Int {
    val isLeader = isMaxId().also { env["source"] = it }
    return hopDistanceTo(source = isLeader)
}
