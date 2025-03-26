package it.unibo.collektive.examples.path

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.maxBy
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.aggregate.api.operators.share

/**
 * Determine the shortest paths (the minimum number of hops) between the source and other nodes in the network.
 * 
 * The nodes that are the farthest from the source must be colored differently.
*/

/**
 * Defined a data class to represent the association between a source node and its distance.
*/ 
data class SourceDistance(val sourceID: Int, val distance: Int)

fun Aggregate<Int>.shortestPathToSource(environment: EnvironmentVariables): Int {
    val sourceID = maxNetworkID(environment)

    val distanceToSource =  SourceDistance(sourceID, hopDistanceTo(sourceID == localId))

    environment["distanceToSource"] = distanceToSource

    val furthestToSource = share(distanceToSource){ previous ->
        previous.maxBy(distanceToSource) { 
            if(sourceID == it.sourceID) it.distance else Int.MIN_VALUE  
        }
    }

    environment["isFurthest"] = furthestToSource.distance == distanceToSource.distance

    return furthestToSource.distance
}
