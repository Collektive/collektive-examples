package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.examples.spreading.distanceToSource
import it.unibo.collektive.stdlib.fields.maxValueBy

/**
 * Determine the furthest paths (the maximum number of hops) between the source and 
 * other nodes in the network.
*/
fun Aggregate<Int>.furthestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)
    val distanceToSource = distanceToSource(sourceID)
    environment["distanceToSource"] = distanceToSource
    val res = share(distanceToSource){ field ->
        val maxValue = field.maxValueBy {
            if(localId == it.value.sourceID) Int.MIN_VALUE 
            else it.value.distance
        }
        maxValue ?: field.local.value
    }
    environment["isFurthest"] = res.distance == distanceToSource.distance 
    return res.distance
}
