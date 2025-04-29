package it.unibo.collektive.examples.fieldComputation

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import it.unibo.collektive.examples.spreading.getListOfDevicesValues
import it.unibo.collektive.aggregate.api.neighboring

data class SourceDistances(val to: Int, val from: Int, val distanceForComunicate: Double, val distance: Double, val comunicateWithSource: Boolean)

fun Aggregate<Int>.computeFieldForDistance(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>) : Map<Int, List<SourceDistances>> {
    val sources: Map<Int, Double> = environment["comunicationDistanceToSources"]
    val devicesValues = getListOfDevicesValues(sources)
    environment["source"] = sources.containsKey(localId)
    return neighboring(devicesValues).alignedMap(with(distanceSensor) { distances() }) { deviceValues, distance ->
        deviceValues.entries.map { (to, distanceForComunicate) ->
            SourceDistances(
                to, 
                localId,
                distanceForComunicate, 
                distance,
                sources.containsKey(to) && distanceForComunicate != POSITIVE_INFINITY && to != localId
            )
        }
    }.toMap()
    .filterKeys { sources.containsKey(it) && it != localId }
    .mapValues { (key, list) ->
        list.filter { it.comunicateWithSource && it.to == key}
    }
}