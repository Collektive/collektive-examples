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
fun Aggregate<Int>.hopsFromSource(distances: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distances,
        accumulateData = { fromSource, toNeighbor, data ->
            data + 1 // hops from source to me
        },
    )

/**
 *
 */
fun Aggregate<Int>.broadcastNeighbors(distances: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distances,
        accumulateData = { fromSource, toNeighbor, data ->
            payload
        },
    )

fun Aggregate<Int>.findAname(distances: Field<Int, Double>): Double = broadcastNeighbors(
    distances = distances,
    isSource = localId == 0,
    payload = neighborCounter().toDouble(),
)

fun Aggregate<Int>.broadcastDistance(distances: Field<Int, Double>, isSource: Boolean, payload: Double): Double =
    gradientCast(
        source = isSource,
        local = payload,
        metric = distances,
        accumulateData = { fromSource, toNeighbor, data ->
            toNeighbor
        },
    )

/**
 * Evaluates the number of neighbors of each device,
 * if the device is the source, it returns 0.
 */
fun Aggregate<Int>.broadcastNeighborsSourceZero(distances: Field<Int, Double>, isSource: Boolean): Double =
    gradientCast(
        source = isSource,
        local = 0.0,
        metric = distances,
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
 * Broadcast from the [source] the number of devices connected in the network.
 * Need a [distances] field used as metric to compute the distance from the source to the target.
 */
fun Aggregate<Int>.broadcastDevices(distances: Field<Int, Double>, source: Boolean): Int = gradientCast(
    metric = distances,
    source = source,
    local = countDevices(sink = source),
)

/**
 * [collektiveDevice] is a representation of the device that runs a Collektive program.
 * It is used to access the device's properties and methods,
 * such as the [distances] method, which returns a field of distances from the source.
 * In this case, the source is the device with [localId] 0.
 */
fun Aggregate<Int>.broadcastDevicesEntrypoint(collektiveDevice: CollektiveDevice<*>): Int = broadcastDevices(
    distances = with(collektiveDevice) { distances() },
    source = localId == 0,
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
    val leaderId = findLeader(env)
    return broadcastDevices(
        distances = with(collektiveDevice) { distances() },
        source = localId == leaderId,
    )
}
