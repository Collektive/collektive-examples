package it.unibo.collektive.examples.hopDistanceTo

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring

/**
 * Calculate the distance to the nearest source in terms of hops
*/ 
fun Aggregate<Int>.hopDistanceTo(source: Boolean): Int {
    return distanceTo(
        source,                        
        0,                             
        Int.MAX_VALUE,             
        { a: Int, b: Int ->            
            if (a == Int.MAX_VALUE || b == Int.MAX_VALUE) Int.MAX_VALUE
            else (a + b).coerceAtMost(Int.MAX_VALUE) 
        }
    ) {
        neighboring(1)                 
    }
}