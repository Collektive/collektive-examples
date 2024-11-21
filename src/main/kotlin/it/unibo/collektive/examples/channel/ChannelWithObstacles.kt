package it.unibo.collektive.examples.channel

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.gradient.gradient
import it.unibo.collektive.field.Field.Companion.fold
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
 * Compute the channel between the source and the target with obstacles.
 */
fun Aggregate<Int>.channelWithObstacles(
    environmentVariables: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): Boolean =
    if (environmentVariables["obstacle"]) {
        false
    } else {
        channel(distanceSensor, environmentVariables["source"], environmentVariables["target"], channelWidth = 0.5)
    }

/**
 * Compute the channel between the [source] and the [target] with a specific [channelWidth].
 */
fun Aggregate<Int>.channel(
    distanceSensor: DistanceSensor,
    source: Boolean,
    destination: Boolean,
    channelWidth: Double
): Boolean {
    require(channelWidth.isFinite() && channelWidth > 0)
    val toSource = gradient(distanceSensor, source)
    val toDestination = gradient(distanceSensor, destination)
    val sourceToDestination = broadcast(distanceSensor, from = source, payload = toDestination)
    val channel = toSource + toDestination - sourceToDestination
    return if (channel.isFinite()) channel <= channelWidth else false
}

/**
 * Computes the [gradientCast] from the [source] with the [value] that is the distance from the [source] to the target.
 */
fun Aggregate<Int>.broadcast(distanceSensor: DistanceSensor, from: Boolean, payload: Double): Double =
    gradientCast(distanceSensor, from, payload) { it }

/**
 * Compute the gradient of the aggregate from the [source] to the [target].
 * The [accumulate] function is used to accumulate the value of the aggregate.
 */
fun Aggregate<Int>.gradientCast(
    distanceSensor: DistanceSensor,
    source: Boolean,
    initial: Double,
    accumulate: (Double) -> Double
): Double = share(POSITIVE_INFINITY to initial) { field ->
    val dist = with(distanceSensor) { distances() }
    when {
        source -> 0.0 to initial
        else -> {
            val resultField = dist.alignedMap(field) { distField, (currentDist, value) ->
                distField + currentDist to accumulate(value)
            }
            resultField.fold(POSITIVE_INFINITY to POSITIVE_INFINITY) { acc, value ->
                if (value.first < acc.first) value else acc
            }
        }
    }
}.second
