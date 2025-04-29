package it.unibo.collektive.examples.spreading

import it.unibo.collektive.examples.fieldComputation.computeFieldForDistance
import it.unibo.collektive.examples.spreading.MessagesSendedTo
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.collections.filter
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.examples.fieldComputation.SourceDistances

data class MessagesSendedTo(val sender: Int, val receivers: Set<Int>, val text: String)

fun Aggregate<Int>.messaging(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): Map<Int, MessagesSendedTo> {
    val senders = computeFieldForDistance(environment, distanceSensor)
    environment["distances"] = senders
    val receivers = receiversList(senders)
    return mapNeighborhood{ id ->
        if(senders.containsKey(id)){
            MessagesSendedTo(
                id,
                receivers.filter { (key, value) ->
                    value && key == id
                }.keys,
                "Hello i'm device with ID ${id}"
            )
        }else{
            MessagesSendedTo(
                -1,
                emptySet(),
                ""
            )
        }
    }.toMap()
}

fun Aggregate<Int>.receiversList(senders: Map<Int, List<SourceDistances>>): Map<Int, Boolean> = mapNeighborhood{ id ->
    if(senders.containsKey(id)){
        val value = senders.get(id)!![0]
        value.distanceForComunicate <= value.distance
    }else{
        false
    }
}.toMap()