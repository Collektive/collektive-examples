package it.unibo.collektive.examples.spreading

import it.unibo.collektive.examples.fieldComputation.computeDistances
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.examples.fieldComputation.SourceDistances

/**
 * (Row 19) The communication values ​​are indicated by the source node.
 * (Row 20) Each node in the network builds a map indicating whether or not it is enabled to receive a 
 * message from a given source.
 * (Rows 21-23) Finally, only messages that a device is enabled to read are displayed.
 * (Row 18) Note: the sources and message values are decided by the user. In this case they are passed 
 * through the simulator environment.
 */
fun Aggregate<Int>.messaging(
    environment: EnvironmentVariables, 
    distanceSensor: CollektiveDevice<*>
): Map<Int, String>{
    val sources: Map<Int, String> = environment["messageFromSources"]
    val distanceOfSenders = computeDistances(environment, distanceSensor)
    val messagesToReceive = toReceiveList(distanceOfSenders)
    return sources.filterKeys { 
        messagesToReceive.values.flatten().contains(it to true)
    } 
}

/**
 * A map is passed as a parameter to this function where the keys are the ids of the source devices 
 * and the values ​​are the distances calculated in the [computeDistances] function in the 
 * [fieldComputation/FieldForDistance.kt] file. 
 * Each node in the network, using the values ​​indicated by the source node, indicates via a boolean value 
 * whether or not it should receive the message that the source node wants to send.
 */
fun Aggregate<Int>.toReceiveList(
    senders: Map<Int, List<SourceDistances>>
): Map<Int, List<Pair<Int, Boolean>>> = mapNeighborhood{ _ ->
    senders.entries.map { (id, distance) ->
        val entry = distance.find { it.to == id && it.from == localId}
        id to (entry!!.distanceForMessaging >= entry.distance)
    }
}
.toMap()
.filterKeys { it == localId }
