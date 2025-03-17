package it.unibo.collektive.examples.exercises

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.exercises.searchSource
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * 2) Compute the distances between any node and the "source" using the adaptive bellman-ford algorithm.
*/

fun Aggregate<Int>.distanceToSource(environment: EnvironmentVariables): Int {
    // Individuate source from the previous exercise 
    searchSource(environment)

    // The `distanceTo` function calculates the minimum distance between adjacent neighboring nodes,
    // starting from the source node and propagating distances to all other nodes.
    // If a node is unreachable, its distance is set to `Int.MAX_VALUE`.
    //
    // The aggregation operation uses a reduction function that sums the distances of neighboring nodes,
    // with a limit of `Int.MAX_VALUE` to prevent overflow.
    environment["distanceToSource"] = distanceTo(
        environment["source"],                        
        0,                             
        Int.MAX_VALUE,             
        { a: Int, b: Int ->            
            if (a == Int.MAX_VALUE || b == Int.MAX_VALUE) Int.MAX_VALUE
            else (a + b).coerceAtMost(Int.MAX_VALUE) 
        }
    ) {
        neighboring(1)                 
    }

    return localId
}