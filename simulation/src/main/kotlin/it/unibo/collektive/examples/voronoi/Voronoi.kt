package it.unibo.collektive.examples.voronoi

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.examples.multiGradient.multiGradient

private const val BORDER_COLOR = 1024
private const val MIN_COLOR_DISTANCE = 128
private const val VERTEX_COLOR = BORDER_COLOR - MIN_COLOR_DISTANCE
private const val MAX_COLOR_VALUE = VERTEX_COLOR - 1

/**
 * Computes the Voronoi tessellation (https://en.wikipedia.org/wiki/Voronoi_diagram)
 * based on a set of sources, producing a field of integers which identify
 * the region each device belongs to.
 * A device can take one of the following roles:
 * - **Vertex**: it is at the junction of three or more Voronoi cells.
 *   Its color will be [VERTEX_COLOR].
 * - **Border**: it is at the junction of two Voronoi cells.
 *   Its color will be [BORDER_COLOR].
 * - **Cell Member**: it is neither a vertex nor a border. Its color
 *   is calculated based on the ID of the closest source.
 * The sources are identified through environment variables.
 */
fun Aggregate<Int>.voronoi(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Int {
    // Find the closest source
    val closestSource = closestSource(collektiveDevice, env)
    // Share the id of the closest source with neighbors
    val neighborClosestSources = neighboring(closestSource)
    // Count how many distinct sources are in the neighborhood (including the current device)
    val distinctSources = neighborClosestSources.all
        .sequence
        .map { it.value }
        .toSet()
        .count()
    // A device is a border if it's at the junction of 3 or more Voronoi regions meet.
    val isVertex = distinctSources >= 3
    // A device is a border if it's at the junction of 2 Voronoi regions meet.
    val isBorder = distinctSources == 2
    return when {
        isVertex -> VERTEX_COLOR
        isBorder -> BORDER_COLOR
        else -> closestSource.toColor()
    }
}

/**
 * Find the closest source by computing a multi-gradient from all sources.
 * If there are no sources, return 0.
 */
private fun Aggregate<Int>.closestSource(collektiveDevice: CollektiveDevice<*>, env: EnvironmentVariables): Int =
    multiGradient(collektiveDevice, env).let { toSources ->
        toSources.minByOrNull { it.value }?.key ?: 0
    }

/**
 * Map a source ID to a color value.
 */
private fun Int.toColor(): Int = (this * MIN_COLOR_DISTANCE) % (MAX_COLOR_VALUE + 1)
