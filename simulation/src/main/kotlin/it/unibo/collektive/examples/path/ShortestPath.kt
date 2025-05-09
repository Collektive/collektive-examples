package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.spreading.distanceToSource
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.fields.minValue

/** Determine the shortest paths (the minimum number of hops) between the source and
 * other nodes in the network. */
fun Aggregate<Int>.shortestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)
    val distanceToSource = distanceToSource(sourceID)
    environment["distanceToSource"] = distanceToSource
    val minDistance: Int = when { 
        distanceToSource.sourceID != localId ->
            share(distanceToSource.distance) {
                it.minValue(distanceToSource.distance)
            }
        else -> Int.MAX_VALUE
    }
    return neighboring(minDistance).minValue(minDistance).also {
        environment["isCloser"] = it == distanceToSource.distance
    }
}
