package it.unibo.collektive.examples.fieldComputation

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import it.unibo.collektive.examples.spreading.getListOfDevicesValues
import it.unibo.collektive.aggregate.api.neighboring

/**
 * Data class representing the distances from a source device to another non-source device.
 */
data class SourceDistances(
    /**
     * Contains the id of the source node being considered
    */
    val to: Int, 
    /**
     * Contains the id of the non-source node being considered
    */
    val from: Int, 
    /**
     * Contains the distance set by the source to be able to receive its messages
    */
    val distanceForMessaging: Double,
    /**
     * Contains the distance between node in [to] value and node in [from] value
    */
    val distance: Double, 
    /**
     * Is a boolean value indicating whether the identified messaging distance has been 
     * communicated by a source node
    */
    val isSourceValues: Boolean 
)

/**
 * (Row 29) First of all the list of devices in the network is computed where in this list the distance 
 * is indicated to which a device wants to send messages. 
 * If it does not want to send messages the distance is set to infinity.
 * (Rows 31-45) A map is then built in which the source nodes communicate to each device in the network 
 * the values ​​indicated in the [SourceDistances] data class.
 * (Row 28) Note: the sources and distance values ​​for messaging with sources are decided by the user. 
 * In this case they are passed through the simulator environment.
 */
fun Aggregate<Int>.computeDistances(
    environment: EnvironmentVariables, 
    distanceSensor: CollektiveDevice<*>
) : Map<Int, List<SourceDistances>> {
    val sources: Map<Int, Double> = environment["comunicationDistanceToSources"]
    val devicesValues = getListOfDevicesValues(sources)
    environment["source"] = sources.containsKey(localId)
    return neighboring(devicesValues).alignedMap(with(distanceSensor) { distances() }) { 
        _: Int, deviceValues: Map<Int, Double>, distance: Double ->
        deviceValues.entries.map { (to, distanceForMessaging) ->
            SourceDistances(
                to, 
                localId,
                distanceForMessaging, 
                distance,
                sources.containsKey(to) && 
                distanceForMessaging != POSITIVE_INFINITY && 
                to != localId
            )
        }
    }.toMap()
    .filterKeys { sources.containsKey(it) && it != localId }
    .mapValues { (key, list) ->
        list.filter { it.isSourceValues && it.to == key}
    }
}