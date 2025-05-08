package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.examples.spreading.distanceToSource
import it.unibo.collektive.stdlib.fields.minValueBy

/**
 * Determine the shortest paths (the minimum number of hops) between the source and 
 * other nodes in the network.
*/
fun Aggregate<Int>.shortestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)
    val distanceToSource =  distanceToSource(sourceID)
    environment["distanceToSource"] = distanceToSource
    val res = share(distanceToSource){ field ->
        val minValue = field.minValueBy {
            if(localId == it.value.sourceID) Int.MAX_VALUE 
            else it.value.distance
        }
        minValue ?: field.local.value
    }
    environment["isCloser"] = res.distance == distanceToSource.distance 
    return res.distance
}
