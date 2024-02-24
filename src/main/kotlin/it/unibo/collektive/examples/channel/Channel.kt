package it.unibo.collektive.examples.channel

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.impl.project
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.examples.gradient.gradient
import it.unibo.collektive.field.Field.Companion.fold
import kotlin.io.path.fileVisitor

context(DistanceSensor)
fun Aggregate<Int>.channel(source: Boolean, destination: Boolean, width: Double): Boolean {
    val distancesToSource = distanceTo(source)
    val distanceToDestination = distanceTo(destination)
    val dBetween = distanceBetween(source, destination)
    return !((distancesToSource + distanceToDestination).isInfinite() && dBetween.isInfinite()) &&
            distancesToSource + distanceToDestination <= dBetween + width
}

context(DistanceSensor)
fun Aggregate<Int>.broadcast(source: Boolean, value: Double, accumulator: (Double) -> Double): Double =
    share(Double.POSITIVE_INFINITY to value) { field ->
        val dist = distances()
        when {
            source -> 0.0 to value
            else -> {
                val resultField = dist.alignedMap(field) { distField, (currDist, value) ->
                    distField + currDist to accumulator(value)
                }
                resultField.fold(Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY) { acc, value ->
                    if (value.first < acc.first) value else acc
                }
            }
        }
    }.second

context(DistanceSensor)
fun Aggregate<Int>.distanceBetween(source: Boolean, destination: Boolean): Double =
    broadcast(source, distanceTo(destination)) { it }

context(DistanceSensor)
fun Aggregate<Int>.distanceTo(target: Boolean): Double = gradient(target)