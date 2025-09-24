package it.unibo.collektive.examples.spreading

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.spreading.gossipMax
import it.unibo.collektive.stdlib.spreading.hopGossipMax

/** Identify the maximum ID values in the network. */
fun Aggregate<Int>.maxNetworkID(environment: EnvironmentVariables): Int =
    hopGossipMax(localId).also { environment["isMaxID"] = localId == it }
