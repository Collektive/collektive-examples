package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.hopGradientCast

/** Defined a data class to represent the association between a source
 * node and its distance. */
data class DistanceToSource(
    /** ID of the identified source node. */
    val sourceID: Int,
    /** Distance from identified source node. */
    val distance: Int,
)

/** Calculating the distance from a node to a given source. */
fun Aggregate<Int>.distanceToSource(sourceID: Int) = DistanceToSource(
    sourceID,
    hopGradientCast(
        source = sourceID == localId,
        local = 0,
        accumulateData = { _, _, value ->
            value + 1
        },
    ),
)
