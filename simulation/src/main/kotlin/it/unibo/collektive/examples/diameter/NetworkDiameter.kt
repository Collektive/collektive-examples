package it.unibo.collektive.examples.diameter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.gossipMin
import kotlin.random.Random

/** Estimates the **diameter of the network** (i.e., the maximum
 * hop-distance between any two devices). */
fun Aggregate<Int>.networkDiameter(): Double {
    val randomId = evolve(Random.Default.nextInt()) { it }
    val distanceFromRandomPoint = distanceTo(
        gossipMin(randomId) == randomId,
    )
    val isFurthest = gossipMax(distanceFromRandomPoint) == distanceFromRandomPoint
    val distanceToFurthest = distanceTo(isFurthest)
    return gossipMax(distanceToFurthest)
}
