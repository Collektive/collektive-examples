package it.unibo.collektive.examples.multiGradient

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.fields.fold
import it.unibo.collektive.stdlib.spreading.multiGradientCast
import kotlin.Double.Companion.POSITIVE_INFINITY

/**
Computes the distance from the current node to all sources in the network using the [multiGradientCast] algorithm.
 */
fun Aggregate<Int>.multiGradient(
    distanceSensor: CollektiveDevice<*>,
    environment: EnvironmentVariables,
): Map<Int, Double> {
    val isSource = environment.get<Boolean>("source")
    val sources = share(emptySet<Int>()) { neighborSources ->
        neighborSources.fold(emptySet<Int>()) { accumulated, neighborSet ->
            accumulated union neighborSet.value
        }.let { collected ->
            if (isSource) collected + localId else collected
        }
    }

    return multiGradientCast(
        sources = sources,
        local = if (localId in sources) 0.0 else POSITIVE_INFINITY,
        metric = with(distanceSensor) { distances() },
        accumulateData = { fromSource, toNeighbor, _ -> fromSource + toNeighbor },
    )
}

/**
 * Entry point for the multi-source gradient computation, delegates to [multiGradient].
 *
 * The [environment] provides environmental variables for the simulation.
 * The [distanceSensor] measures distances to neighboring nodes.
 *
 * Returns a map where each key is the node ID of a source and the corresponding value is the distance to that source.
 */
fun Aggregate<Int>.multiGradientEntryPoint(
    environment: EnvironmentVariables,
    distanceSensor: CollektiveDevice<*>,
): Map<Int, Double> = multiGradient(distanceSensor, environment)
