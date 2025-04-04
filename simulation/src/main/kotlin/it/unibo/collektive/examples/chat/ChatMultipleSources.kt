package it.unibo.collektive.examples.chat

import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

fun Aggregate<Int>.chatAlgorithmMultipleSources(distanceSensor: DistanceSensor, source : String) : String{

}

fun Aggregate<Int>.chatAlgorithmEntrypoint(
    environment: EnvironmentVariables,
    distanceSensor: DistanceSensor,
): String = chatAlgorithmMultipleSources(distanceSensor, environment["source"])