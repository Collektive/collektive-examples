package it.unibo.collektive.examples.fieldComputation

import kotlin.random.Random
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.stdlib.spreading.gradientCast
import it.unibo.collektive.field.operations.minBy

data class SourceDistances(val sourceID: Int, var distanceForComunicate: Double, val distanceToSource: Double)

fun Aggregate<Int>.computeFieldForDistance(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): SourceDistances {
    val sourceID = globalElection()
    environment["source"] = sourceID == localId
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
    environment["inDistance"] = sourceDistances.distanceToSource <= sourceDistances.distanceForComunicate && sourceDistances.sourceID != localId
    return sourceDistances
}