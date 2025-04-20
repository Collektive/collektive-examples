package it.unibo.collektive.examples.spreading

import kotlin.random.Random
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.alchemist.collektive.device.DistanceSensor
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

fun Aggregate<Int>.neighborhoodForDistance(distanceSensor: DistanceSensor): MutableSet<Int>{
    val distanceToComunicate = Random.nextDouble(from = 1.0, until = 10.0)

    val distances = with(distanceSensor) { distances() }

    val neighborInDistance = mutableSetOf<Int>()

    distances.toMap().forEach { (neighbor, distance) ->
        if (distance <= distanceToComunicate) {
            neighborInDistance.add(neighbor)
        }
    }

    return neighborInDistance
}