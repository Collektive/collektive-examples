package it.unibo.collektive.examples.fieldComputation

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import it.unibo.collektive.examples.spreading.getSources
import it.unibo.collektive.aggregate.api.neighboring

data class SourceDistances(val to: Int, val from: Int, val distanceForComunicate: Double, val distance: Double, val distanceWithSource: Boolean)

fun Aggregate<Int>.computeFieldForDistance(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>) : Map<Int, List<SourceDistances>> {
    val sources: Map<Int, Double> = environment["sources"]
    val distancesForComunicate = getSources(sources)
    environment["source"] = sources.containsKey(localId)
    return neighboring(distancesForComunicate).alignedMap(with(distanceSensor) { distances() }) { sourceValues, distance ->
        sourceValues.entries.map { (to, distanceForComunicate) ->
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
        list.filter { it.distanceWithSource && it.to == key}
    }
}