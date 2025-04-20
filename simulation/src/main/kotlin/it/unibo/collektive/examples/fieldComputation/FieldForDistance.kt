package it.unibo.collektive.examples.fieldComputation

import kotlin.random.Random
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.collections.listOf
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.field.operations.minBy

data class SourceDistances(val sourceID: Int, var distanceForComunicate: Double, val distanceToSource: Double)

fun Aggregate<Int>.sourceIDsChoice(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): List<SourceDistances> {
    var sourceIDs = listOf(globalElection(localId == 0), globalElection(localId == 9))
    environment["source"] = sourceIDs.contains(localId)
    val sourcesDistances = listOf(
        computeFieldForDistance(distanceSensor, sourceIDs[0]),
        computeFieldForDistance(distanceSensor, sourceIDs[1])
    )
    sourcesDistances.forEach{ sourceDistances ->
        environment["inDistance"] = environment["inDistance"] || sourceDistances.distanceToSource <= sourceDistances.distanceForComunicate && sourceDistances.sourceID != localId
    }
    return sourcesDistances
}

fun Aggregate<Int>.computeFieldForDistance(distanceSensor: CollektiveDevice<*>, sourceID: Int): SourceDistances {
    val sourceDistances = SourceDistances(
        sourceID, 
        Random.nextDouble(from = 5.0, until = 20.0), 
        with(distanceSensor) { distances().get(sourceID) }
    )
    sourceDistances.distanceForComunicate = share(sourceDistances){ field ->
        field.minBy(sourceDistances){
            if(sourceID == it.sourceID) it.distanceToSource else POSITIVE_INFINITY  
        }
    }.distanceForComunicate 
    return sourceDistances
}