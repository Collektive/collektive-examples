package it.unibo.collektive.examples.spreading

import it.unibo.collektive.examples.fieldComputation.computeFieldForDistance
import it.unibo.collektive.examples.spreading.MessagesSendedTo
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.collections.filter

data class MessagesSendedTo(val sender: Int, var receivers: Set<Int>, val payload: String)

fun Aggregate<Int>.messaging(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): MessagesSendedTo {
    val sender = computeFieldForDistance(environment, distanceSensor).sourceID
    val messageToSend = MessagesSendedTo(
        sender,
        receiversList(environment["inDistance"]).filter { (_, value) ->
            value == true 
        }.keys,
        "Hello i'm device with ID $sender"
    )
    messageToSend.receivers.forEach{ receiverID ->
        // TODO: send message to receiverID
    }
    return messageToSend
}

fun Aggregate<Int>.receiversList(inDistance: Boolean): Map<Int, Boolean> = neighboring(inDistance).toMap()