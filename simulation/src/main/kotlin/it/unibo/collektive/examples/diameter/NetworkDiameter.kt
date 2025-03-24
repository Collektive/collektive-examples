package it.unibo.collektive.examples.diameter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.operations.maxBy
import it.unibo.collektive.examples.spreading.maxNetworkID
import it.unibo.collektive.stdlib.spreading.hopDistanceTo

/**
 * Determine the diameter of the subnetworks corresponding to the nodes with the maximum ID values in the network.
 * 
 * The nodes that are the farthest, in terms of hop count, from the nodes with the maximum ID values in the network (which serve as the center of the connected subnetwork) must be colored with different colors.
*/

// Define a data class to represent the association between a source node and its distance
data class SourceDistance(val sourceID: Int, val distance: Int)

fun Aggregate<Int>.subnetDiameter(environment: EnvironmentVariables): SourceDistance {
    val sourceID = maxNetworkID(environment)

    val distanceToSource = hopDistanceTo(sourceID == localId)

    val distances = neighboring(SourceDistance(sourceID, distanceToSource))

    val diameter = distances.maxBy(SourceDistance(sourceID, distanceToSource)){ 

        if(sourceID == it.sourceID) it.distance else Int.MIN_VALUE 
    }

    environment["diameter"] = diameter.distance
    environment["isPeripheral"] = diameter.distance == distanceToSource

    return diameter
}
