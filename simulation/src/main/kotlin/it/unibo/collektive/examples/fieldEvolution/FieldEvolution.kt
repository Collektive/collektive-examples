package it.unibo.collektive.examples.fieldEvolution

import it.unibo.collektive.aggregate.api.Aggregate

/**
 * A simple example of field evolution using the [evolve] function.
 */
fun Aggregate<Int>.fieldEvolution(): Int = evolve(0) { it + 1 }
