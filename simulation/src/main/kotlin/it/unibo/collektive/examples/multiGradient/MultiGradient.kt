package it.unibo.collektive.examples.multiGradient

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.fold
import it.unibo.collektive.stdlib.spreading.multiGradientCast
import kotlin.Double.Companion.POSITIVE_INFINITY

fun Aggregate<Int>.multiGradient(
    distanceSensor: DistanceSensor,
    environment: EnvironmentVariables,
): Map<Int, Double> {

    val isSource = environment.get<Boolean>("source")
    val sources = share(emptySet<Int>()) { neighborSources ->
        neighborSources.fold(emptySet<Int>()) { accumulated, neighborSet -> accumulated union neighborSet }.let { collected ->
            if (isSource) collected + localId else collected
        }
    }

    return multiGradientCast(
        sources = sources,
        local = if (localId in sources) 0.0 else POSITIVE_INFINITY,
        bottom = 0.0,
        top = POSITIVE_INFINITY,
        accumulateData = {fromSource, toNeighbor, _ -> fromSource + toNeighbor },
        accumulateDistance = { fromSource, toNeighbor -> fromSource + toNeighbor },
        metric = { with(distanceSensor){distances()} }
    )
}


fun Aggregate<Int>.multiGradientEntryPoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): Map<Int, Double> = multiGradient(distanceSensor, environment)