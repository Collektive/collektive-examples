package it.unibo.collektive.examples.tutorialExample

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.field.operations.max
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.subnetDiameter.subnetDiameter
import it.unibo.collektive.stdlib.spreading.hopDistanceTo
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * First part:
 * 1. Identify the maximum ID values among the neighboring nodes.
 * 
 * Collektive & Alchemist:
 * 2. Assign a distinct color to the nodes with the identified maximum local ID values.
 * 
 * Second part:
 * 3. Identify the maximum ID values in the network.
 * 
 * Collektive & Alchemist:
 * 4. Assign a distinct color to the nodes with the identified maximum ID values in the network.
*/

fun Aggregate<Int>.maxNeighborID(): Int {
    // Step 1: Exchange the localId with neighbors and obtain a field of values
    val neighborValues = neighboring(local = localId)

    // Step 2: Find the maximum value among neighbors (including self)
    val maxValue = neighborValues.max(base = localId)

    return maxValue
}

fun Aggregate<Int>.maxID(environment: EnvironmentVariables): Int {
    val maxLocalValue = maxNeighborID()

    // Collektive & Alchemist: Assign the result to a molecule
    environment["localID"] = localId
    environment["isMaxLocalID"] = localId == maxLocalValue
    environment["maxNeighborID"] = maxLocalValue

    // Step 1: Exchange the maxNeighborID with neighbors and obtain a field of values
    val neighborValues = neighboring(local = maxLocalValue)

    // Step 2: Find the maximum value among neighbors (including self)
    val maxValue = neighborValues.max(base = maxLocalValue)

    // Collektive & Alchemist: Assign the result to a molecule 
    environment["isMaxID"] = localId == maxValue
    environment["maxNetworkID"] = maxValue

    /* Third part */

    // Preliminary step: the distance from the nearest source is calculated using the distanceTo library function 
    environment["distanceToSource"] = distanceToSource(environment["isMaxID"])

    // Calculate subnets diameter 
    val subnetDiameterValue = subnetDiameter(environment["maxNetworkID"], environment["distanceToSource"])

    // Collektive & Alchemist: Assign the result to a molecule
    environment["subnetDiameter"] = subnetDiameterValue

    val subnetDiameterDistance = subnetDiameterValue.distance

    // Collektive & Alchemist: Assign the result to a molecule
    environment["subnetDiameterValue"] = subnetDiameterDistance
    environment["isSubnetDiameterDistance"] = subnetDiameterDistance == environment["distanceToSource"]
    environment["nothing"] =  !(environment["isSubnetDiameterDistance"] || environment["isMaxID"] || environment["isMaxLocalID"])

    return maxValue
}

fun <ID : Any> Aggregate<ID>.distanceToSource(source: Boolean): Int = 
    distanceTo(
        source,                        
        0,                             
        Int.MAX_VALUE,             
        { a: Int, b: Int ->            
            if (a == Int.MAX_VALUE || b == Int.MAX_VALUE) Int.MAX_VALUE
            else (a + b).coerceAtMost(Int.MAX_VALUE) 
        }
    ) {
        neighboring(1)                 
    }