package it.unibo.collektive.examples.aggregate

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.neighboringViaExchange
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.field.Field.Companion.hood
import it.unibo.collektive.field.plus
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * TODO.
 */
fun Aggregate<Int>.neighborCounter(): Int = neighboringViaExchange(1).hood(0) { acc, _ -> acc + 1 }

/**
 * TODO.
 */
context(DistanceSensor)
fun Aggregate<Int>.gradient(id: Int): Double {
    val paths = distances()
    return share(POSITIVE_INFINITY) { dist ->
        val minByPath: Double = (paths + dist).excludeSelf().map { it.value }.minOrNull() ?: POSITIVE_INFINITY
        if (id == 0) {
            0.0
        } else {
            minByPath
        }
    }
}

/**
 * TODO.
 */
context(DistanceSensor)
fun Aggregate<Int>.entrypoint(): Double = gradient(localId)
