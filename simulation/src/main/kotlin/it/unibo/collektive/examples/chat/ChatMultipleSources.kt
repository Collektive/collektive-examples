package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.field.Field.Companion.fold
import it.unibo.collektive.stdlib.spreading.multiGradientCast
import kotlin.Double.Companion.POSITIVE_INFINITY


fun Aggregate<Int>.chatMultipleSources(
    distanceSensor: DistanceSensor,
    environment: EnvironmentVariables,
    message: String = "Hello"
): Map<String, Message>{

    val isSource = environment.get<Boolean>("source")
    val sourceName = environment.getOrDefault("sourceName", "node")

    val idToName : Map<Int, String> = share(emptyMap<Int, String>()) { neighborSources ->
        neighborSources.fold(emptyMap<Int, String>()) { accumulator, neighborMap ->
            accumulator + neighborMap}.let { collected ->
            if (isSource) collected + (localId to sourceName) else collected
            }
        }
    val sources = idToName.keys
    val multiState : Map<Int, Double> =  multiGradientCast(
        sources = sources,
        local = if (localId in sources) 0.0 else POSITIVE_INFINITY,
        bottom = 0.0,
        top = POSITIVE_INFINITY,
        accumulateData = {fromSource, toNeighbor, _ -> fromSource + toNeighbor },
        accumulateDistance = { fromSource, toNeighbor -> fromSource + toNeighbor },
        metric = { with(distanceSensor){distances()} }
    )
    val transformedState : Map<String, Double> =  multiState
        .map {(key, value) -> idToName[key]!! to value}
        .toMap()

    val content : MutableMap<String, Message> = mutableMapOf()

    transformedState.forEach{ entry ->
        when{
            entry.value <= REACHABLE -> content[entry.key] = Message(message, entry.value)
            entry.value < THRESHOLD -> content[entry.key] = Message("$message ${"%.0f".format(calculateFaint(entry.value))}%", entry.value)
            else -> content[entry.key] = Message("Unreachable", entry.value)
        }
    }

    return content.toMap()
}


fun Aggregate<Int>.chatMultipleEntryPoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): String = chatMultipleSources(distanceSensor, environment).toString()