package it.unibo.collektive.examples.diameter

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.gossipMin
import it.unibo.collektive.stdlib.spreading.hopGossipMax
import it.unibo.collektive.stdlib.spreading.hopGossipMin
import kotlin.random.Random

/** Estimates the **diameter of the network** (i.e., the maximum
 * hop-distance between any two devices). */
fun Aggregate<Int>.networkDiameter(): Double {
    val randomId = evolve(Random.Default.nextInt()) { it }
    val distanceFromRandomPoint = distanceTo(
        hopGossipMin(randomId) == randomId,
    )
    val isFurthest = hopGossipMax(distanceFromRandomPoint) == distanceFromRandomPoint
    val distanceToFurthest = distanceTo(isFurthest)
    return hopGossipMax(distanceToFurthest)
}
