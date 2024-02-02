package it.unibo.collektive.examples.aggregate

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.neighboringViaExchange
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.examples.gradient.gradient
import it.unibo.collektive.field.Field.Companion.hood

/**
 * Extension function to evaluate the number of neighbors of a node in an [Aggregate] context.
 */
fun Aggregate<Int>.neighborCounter(): Int = neighboringViaExchange(1).hood(0) { acc, _ -> acc + 1 }

/**
 * The entrypoint of the simulation.
 */
context(DistanceSensor)
fun Aggregate<Int>.entrypoint(): Double = gradient()
