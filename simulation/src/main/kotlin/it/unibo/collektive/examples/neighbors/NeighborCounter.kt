package it.unibo.collektive.examples.neighbors

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighborhood

/**
 * Extension function to evaluate the number of neighbors of a node in an [Aggregate] context.
 */
fun Aggregate<Int>.neighborCounter(): Int = neighborhood().neighborsCount
