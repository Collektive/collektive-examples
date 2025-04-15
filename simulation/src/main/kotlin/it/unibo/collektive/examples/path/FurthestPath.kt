package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.maxBy
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.examples.path.SourceDistance

/**
 * Determine the furthest paths (the maximum number of hops) between the source and other nodes in the network.
*/
fun Aggregate<Int>.furthestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)

    val distanceToSource =  SourceDistance(sourceID, hopDistanceTo(sourceID == localId))

    environment["distanceToSource"] = distanceToSource

    val furthestToSource = share(distanceToSource){ previous ->
        previous.maxBy(distanceToSource) { 
            if(sourceID == it.sourceID && sourceID != localId) it.distance else Int.MIN_VALUE  
        }
    }.also { 
        environment["isFurthest"] = it.distance == distanceToSource.distance 
    }

    return furthestToSource.distance
}