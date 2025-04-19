package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.minBy
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.spreading.hopGradientCast
import it.unibo.collektive.aggregate.api.share

/**
 * Defined a data class to represent the association between a source node and its distance.
*/ 
data class SourceDistance(val sourceID: Int, val distance: Int)

/**
 * Determine the shortest paths (the minimum number of hops) between the source and other nodes in the network.
*/
fun Aggregate<Int>.shortestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)

    val distanceToSource =  SourceDistance(sourceID, hopGradientCast(
        source  = sourceID == localId,
        local = 0,
        accumulateData = { _, _, value -> 
            value + 1 
        }
        )
    )

    environment["distanceToSource"] = distanceToSource

    val closerToSource = share(distanceToSource){ previous ->
        previous.minBy(distanceToSource) { 
            if(sourceID == it.sourceID && sourceID != localId) it.distance else Int.MAX_VALUE  
        }
    }.also { 
        environment["isCloser"] = it.distance == distanceToSource.distance 
    }

    return closerToSource.distance
}
