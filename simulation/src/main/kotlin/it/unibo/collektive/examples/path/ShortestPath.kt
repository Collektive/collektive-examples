package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.minBy
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.examples.spreading.distanceToSource

/**
 * Determine the shortest paths (the minimum number of hops) between the source and other nodes in the network.
*/
fun Aggregate<Int>.shortestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)
    val distanceToSource =  distanceToSource(sourceID)
    environment["distanceToSource"] = distanceToSource
    return share(distanceToSource){ previous ->
        previous.minBy(distanceToSource) { 
            if(sourceID == it.sourceID && sourceID != localId) it.distance else Int.MAX_VALUE  
        }
    }.also { 
        environment["isCloser"] = it.distance == distanceToSource.distance 
    }.distance
}