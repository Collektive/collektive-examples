package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.gossipMin

/** Select a node identified as source, chosen by finding the node with minimum uid
 * in the network, assuming that the diameter of the network is no more than 10 hops. */
fun Aggregate<Int>.searchSource(): Boolean = gossipMin(localId).let { minValue -> localId == minValue }
