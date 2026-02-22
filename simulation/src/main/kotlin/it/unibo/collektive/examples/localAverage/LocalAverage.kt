package it.unibo.collektive.examples.localAverage

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.util.Point3D
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import org.apache.commons.math3.random.RandomGenerator

/**
 * Entry point for the local average computation algorithm.
 * Initializes each device with a random vector in R^3 (represented as a 3D point)
 * that remains constant throughout the simulation. Then, computes the local average
 * between the device's vector and those of its neighbors, returning their magnitude
 * as a scalar field.
 */
fun Aggregate<Int>.localAverageEntryPoint(collektiveDevice: CollektiveDevice<*>): Double = with(collektiveDevice) {
    // `vector` is initialized to a random vector at the first round,
    // keeping its value constant afterward.
    val vector = evolve(randomPoint(randomGenerator)) { it }
    localAverage(vector).magnitude().round(4)
}

/**
 * Computes the local average of vectors in the neighborhood of the current device.
 * Each device holds a point [v] in R^3 representing its current state.
 * The function returns the component-wise average of the vectors from the device and its neighbors.
 */
fun Aggregate<Int>.localAverage(v: Point3D): Point3D = with(neighboring(v).all) {
    fold(point(0.0, 0.0, 0.0)) { acc, nbr -> acc + nbr.value } / size.toDouble()
}

/**
 * Computes the Euclidean magnitude of a [Point3D].
 */
fun Point3D.magnitude(): Double = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

/**
 * Generates a random [Point3D].
 */
fun randomPoint(rgn: RandomGenerator): Point3D = with(rgn) {
    point(nextDouble(), nextDouble(), nextDouble())
}

/**
 * Rounds a Double to the specified number of [decimals] places.
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

/**
 * Creates a [Point3D] given [x], [y], and [z] coordinates.
 */
fun point(x: Double, y: Double, z: Double): Point3D = Point3D(Triple(x, y, z))
