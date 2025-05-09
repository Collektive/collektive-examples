package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.spreading.distanceToSource
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.fields.maxValue

/** Determine the furthest paths (the maximum number of hops) between the source and
 * other nodes in the network. */
fun Aggregate<Int>.furthestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)
    val distanceToSource = distanceToSource(sourceID)
    environment["distanceToSource"] = distanceToSource
    val maxDistance: Int = when { 
        distanceToSource.sourceID != localId ->
            share(distanceToSource.distance) {
                it.maxValue(distanceToSource.distance)
            }
        else -> Int.MIN_VALUE
    }
    return neighboring(maxDistance).maxValue(maxDistance).also {
        environment["isFurthest"] = it == distanceToSource.distance
    }
}
