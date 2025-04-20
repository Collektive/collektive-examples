package it.unibo.collektive.examples.spreading

import it.unibo.collektive.examples.fieldComputation.sourceIDsChoice
import it.unibo.collektive.examples.spreading.MessagesSendedTo
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.collections.filter

data class MessagesSendedTo(val sender: Int, var receivers: Set<Int>)

fun Aggregate<Int>.messaging(environment: EnvironmentVariables, distanceSensor: CollektiveDevice<*>): List<String> {
    val senders = sourceIDsChoice(environment, distanceSensor)
    environment["distances"] = senders
    return listOf(
        messagingWith(senders[0].distanceForComunicate >= senders[0].distanceToSource, senders[0].sourceID),
        messagingWith(senders[1].distanceForComunicate >= senders[1].distanceToSource, senders[1].sourceID)
    )
}

fun Aggregate<Int>.messagingWith(inDistance: Boolean, sender: Int): String {
    val messageToSend = MessagesSendedTo(
        sender,
        receiversList(inDistance).filter { (_, value) ->
            value == true 
        }.keys
    )
    return share("") { 
        if (messageToSend.receivers.contains(it.localId)) {
            "Hello i'm device with ID $sender"
        }else{
            ""
        }
    }
}

fun Aggregate<Int>.receiversList(inDistance: Boolean): Map<Int, Boolean> = neighboring(inDistance).toMap()