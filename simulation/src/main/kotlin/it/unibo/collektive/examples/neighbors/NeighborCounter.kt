package it.unibo.collektive.examples.neighbors

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.count

/**
 * Function to evaluate the number of neighbors of a node in an [Aggregate] context.
 */
fun Aggregate<Int>.neighborCounter(): Int = neighboring(1).count()
