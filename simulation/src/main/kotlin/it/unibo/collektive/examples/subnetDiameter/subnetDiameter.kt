package it.unibo.collektive.examples.subnetDiameter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.field.operations.maxBy

/**
 * Third part:
 * 1. Determine the diameter of the subnetworks corresponding to the nodes with the maximum ID values in the network of the last exercise.
 * 
 * Collektive & Alchemist:
 * 2. The nodes that are the farthest, in terms of hop count, from the nodes with the maximum ID values in the network (which serve as the center of the connected subnetwork) must be colored with different colors.
*/

// Preliminary step: define a data class to represent the association between a source node and its distance
data class SourceDistance(val sourceID: Int, val distance: Int)

fun Aggregate<Int>.subnetDiameter(sourceID: Int, distanceToSource: Int): SourceDistance {
    // Step 1: retrieve the distances from neighboring nodes, including the distance of the current node
    val distances = neighboring(SourceDistance(sourceID, distanceToSource))

    // Step 2: find the neighbor with the maximum distance for the given sourceID
    return distances.maxBy(SourceDistance(sourceID, distanceToSource)){ 

        // If the sourceID matches, return the actual distance; otherwise, Int.MIN_VALUE is used to exclude it
        if(sourceID == it.sourceID) it.distance else Int.MIN_VALUE 
    }
}



