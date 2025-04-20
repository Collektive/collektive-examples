package it.unibo.collektive.examples.spreading

import kotlin.random.Random
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.spreading.neighborhoodForDistance

fun Aggregate<Int>.messaging(environment: EnvironmentVariables, distanceSensor: DistanceSensor) {
    //environment["source"] = Random.nextInt(from = 0, until = 99) == localId
    environment["source"] = localId == 0

    if(localId == 0){
        environment["inDistance"] = neighborhoodForDistance(distanceSensor)
    }
}