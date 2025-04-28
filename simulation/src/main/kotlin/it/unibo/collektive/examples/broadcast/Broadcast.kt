package it.unibo.collektive.examples.broadcast

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.neighbors.neighborCounter
import it.unibo.collektive.stdlib.accumulation.countDevices
import it.unibo.collektive.stdlib.consensus.boundedElection
import it.unibo.collektive.stdlib.spreading.gradientCast

/**
 * Compute the broadcast from the source to the target.
 */
fun Aggregate<Int>.broadcastSourcesNeighbors(
    distanceMetric: Field<Int, Double>,
    isSource: Boolean,
    payload: Double,
): Double = gradientCast(
    source = isSource,
    local = payload,
    metric = distanceMetric,
)

/**
 * Computes the hops from the source to the target.
 */
fun Aggregate<Int>.hopsFromSource(distanceMetric: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distanceMetric,
        accumulateData = { fromSource, toNeighbor, data ->
            data + 1 // hops from source to me
        },
    )

/**
 *
 */
fun Aggregate<Int>.broadcastNeighbors(distanceMetric: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distanceMetric,
        accumulateData = { fromSource, toNeighbor, data ->
            payload
        },
    )

fun Aggregate<Int>.findAname(collektiveDevice: CollektiveDevice<*>): Double = broadcastNeighbors(
    distanceMetric = with(collektiveDevice) { distances() },
    isSource = localId == 0,
    payload = neighborCounter().toDouble(),
)

fun Aggregate<Int>.broadcastDistance(distanceMetric: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distanceMetric,
        accumulateData = { fromSource, toNeighbor, data ->
            toNeighbor
        },
    )

/**
 * Evaluates the number of neighbors of each device,
 * if the device is the source, it returns 0.
 */
fun Aggregate<Int>.broadcastNeighborsSourceZero(distanceMetric: Field<Int, Double>, isSource: Boolean): Double =
    gradientCast(
        source = isSource,
        local = 0.0,
        metric = distanceMetric,
        accumulateData = { fromSource, toNeighbor, data ->
            neighborCounter().toDouble()
        },
    )

/**
 * Broadcast the payload from the source to the peripheral devices.
 */
fun Aggregate<Int>.broadcast(distances: Field<Int, Double>, isSource: Boolean, payload: Int): Int = gradientCast(
    source = isSource,
    local = payload,
    metric = distances,
)

// ===================================

/**
 * Broadcast the number of devices connected in the network.
 */
fun Aggregate<Int>.broadcastDevices(distances: Field<Int, Double>, isSource: Boolean): Int = broadcast(
    distances = distances,
    isSource = isSource,
    payload = countDevices(isSource),
)

/**
 * The entrypoint of the simulation running a broadcast with the source set as the device with id 0.
 */
fun Aggregate<Int>.broadcastDevicesEntrypoint(collektiveDevice: CollektiveDevice<*>): Int = broadcastDevices(
    distances = with(collektiveDevice) { distances() },
    isSource = localId == 0,
)

// how to fix the number update when the source is no more available?
// with a leader election!

/**
 * Elects a leader in the network of devices within a bounded space.
 */
fun Aggregate<Int>.findLeader(env: EnvironmentVariables): Int = boundedElection(bound = 25).also {
    env["leader"] = it
    env["isSource"] = localId == it
}

/**
 * Broadcast the number of devices connected in the network with a leader election.
 */
fun Aggregate<Int>.broadcastDevicesWithLeaderElectionEntrypoint(
    collektiveDevice: CollektiveDevice<*>,
    env: EnvironmentVariables,
): Int {
    val leader = findLeader(env)
    return broadcastDevices(
        distances = with(collektiveDevice) { distances() },
        isSource = localId == leader,
    )
}
