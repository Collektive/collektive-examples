package it.unibo.collektive.examples.localAverage

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.stdlib.collapse.fold
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.pow
import kotlin.math.sqrt

private const val DEFAULT_RANDOM_VECTOR_MAX = 10
private const val ROUNDING_MULTIPLIER = 10000.0

/**
 * Entry point for the local average computation algorithm.
 * This function initializes each device with a random 3D vector that remains constant
 * throughout the simulation, then computes the local average of neighboring vectors
 * and returns their magnitude as a scalar field.
 */
fun Aggregate<Int>.localAverageEntryPoint(collektiveDevice: CollektiveDevice<*>): Double = with(collektiveDevice) {
    // v is initialized to a random vector at the first round, keeping its value constant afterwards
    val v = evolve(randomVector(randomGenerator)) { it }
    localAverage(v)
}

/**
 * Computes the local average of the vectors in the neighborhood of the current device.
 * Each device has a vector of integers [v] \in R^3 which represents its current state.
 * The function calculates the component-wise average of all neighboring vectors and
 * returns the Euclidean magnitude of the resulting average vector.
 */
fun Aggregate<Int>.localAverage(v: Triple<Int, Int, Int>): Double = neighboring(v).neighbors.let { neighbors ->
    // Summing up the vectors using folding on the Field (foldHood)
    neighbors.fold(Triple(0, 0, 0)) { (accX, accY, accZ), neighbor ->
        val (x, y, z) = neighbor.value
        Triple(accX + x, accY + y, accZ + z)
    }.let { (sumX, sumY, sumZ) ->
        // Count the number of neighbors
        val count = neighbors.size.toDouble()
        // Compute the average vector
        Triple(sumX / count, sumY / count, sumZ / count)
    }
}
    // Return its magnitude
    .magnitude()
    .roundTo4Decimals()
    .takeIf { it.isFinite() } ?: 0.0

/**
 * Computes the Euclidean magnitude of a 3D vector represented as a Triple.
 */
fun Triple<Double, Double, Double>.magnitude(): Double = sqrt(first.pow(2) + second.pow(2) + third.pow(2))

/**
 * Generates a random 3D vector with each component being a random integer between 0 and [top].
 */
fun randomVector(rgn: RandomGenerator, top: Int = DEFAULT_RANDOM_VECTOR_MAX): Triple<Int, Int, Int> =
    Triple(rgn.nextInt(top), rgn.nextInt(top), rgn.nextInt(top))

/**
 * A simple function to round a Double to a specific decimal places.
 */
fun Double.roundTo4Decimals(): Double = kotlin.math.round(this * ROUNDING_MULTIPLIER) / ROUNDING_MULTIPLIER
