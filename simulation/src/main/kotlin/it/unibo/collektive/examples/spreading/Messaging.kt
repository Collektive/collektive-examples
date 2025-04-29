package it.unibo.collektive.examples.spreading

import it.unibo.collektive.examples.fieldComputation.computeFieldForDistance
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.examples.fieldComputation.SourceDistances

data class MessagesSendedTo(val sender: Int, val receivers: Set<Int>, val text: String)

/**
 * TODO: build complete list in source devices
 */
fun Aggregate<Int>.messaging(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): Map<Int, List<Pair<Int, Boolean>>>{
    val senders = computeFieldForDistance(environment, distanceSensor)
    return toReceiveList(senders)
}

fun Aggregate<Int>.toReceiveList(senders: Map<Int, List<SourceDistances>>): Map<Int, List<Pair<Int, Boolean>>> = mapNeighborhood{ _ ->
    senders.entries.map { (id, distance) ->
        val entry = distance.find { it.to == id && it.from == localId}
        id to (entry!!.distanceForComunicate >= entry.distance)
    }
}
.toMap()
.filterKeys { it == localId }