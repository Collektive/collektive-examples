package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate

/**
 * Return the node identifier
*/
fun Aggregate<Int>.getLocalId(): Int {
    return localId
}