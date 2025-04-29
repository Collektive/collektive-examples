package it.unibo.collektive.examples.spreading

import it.unibo.collektive.stdlib.consensus.globalElection
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import kotlin.Double.Companion.POSITIVE_INFINITY
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.mapNeighborhood

fun Aggregate<Int>.getSources(sources: Map<Int, Double>) : Map<Int, Double>{
    return mapNeighborhood { id -> 
        if (isSource(sources, id)) { sources.get(id)!! } else { POSITIVE_INFINITY }
    }.toMap()
}

fun Aggregate<Int>.isSource(sources: Map<Int, Double>, id: Int): Boolean{
    return sources.containsKey(id)
}
