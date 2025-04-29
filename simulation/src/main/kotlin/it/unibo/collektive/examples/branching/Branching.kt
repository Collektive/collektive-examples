package it.unibo.collektive.examples.branching

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.examples.neighbors.neighborCounter

/**
 * If the node is the source, it returns 0, otherwise it returns the number of neighbors.
 */
fun Aggregate<Int>.branching(isSource: Boolean): Int {
    val count = neighborCounter()
    return when {
        isSource -> 0
        else -> count
    }
}

/**
 * The entrypoint of the simulation running a branching program.
 */
fun Aggregate<Int>.branchingEntrypoint(): Int = branching(localId == 0)
