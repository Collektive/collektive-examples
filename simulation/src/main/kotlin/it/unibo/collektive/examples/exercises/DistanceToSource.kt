package it.unibo.collektive.examples.exercises

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.spreading.bellmanFordGradientCast

/** Compute the distances between any node and the source using the adaptive bellman-ford algorithm. */
fun Aggregate<Int>.distanceToSource(distanceSensor: CollektiveDevice<*>) = bellmanFordGradientCast(
    source = searchSource(),
    local = 0,
    accumulateData = { _, _, dist -> dist + 1 },
    metric = with(distanceSensor) { distances() },
)
