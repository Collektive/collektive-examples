package it.unibo.collektive.examples.temperature

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values

private const val HEAT_SOURCE_TEMPERATURE = 30.0
private const val COLD_SOURCE_TEMPERATURE = 20.0
private const val INITIAL_TEMPERATURE = 0.0

private const val MIN_HEAT_SOURCE_ID = 0
private const val MAX_HEAT_SOURCE_ID = 4
private const val MIN_COLD_SOURCE_ID = 5
private const val MAX_COLD_SOURCE_ID = 9

private val HEAT_SOURCES: IntRange
    get() = MIN_HEAT_SOURCE_ID..MAX_HEAT_SOURCE_ID

private val COLD_SOURCES: IntRange
    get() = MIN_COLD_SOURCE_ID..MAX_COLD_SOURCE_ID

/**
 * Entry point for the program which computes the temperature's field.
 */
fun Aggregate<Int>.temperatureEntrypoint(device: CollektiveDevice<*>): Double = with(device) {
    temperature(localId in HEAT_SOURCES, localId in COLD_SOURCES)
}

/**
 * Represents an evolving temperature field where each device updates its temperature over time.
 * Some devices act as fixed heat sources at 30°C, others as fixed cold sources at 20°C.
 * All other devices calculate their temperature dynamically as the average temperature of their neighboring devices.
 */
fun Aggregate<Int>.temperature(heatSource: Boolean, coldSource: Boolean): Double =
    share(INITIAL_TEMPERATURE) { previousTemperatures ->
        val averageTemperature = previousTemperatures.neighbors
            .values
            .sequence
            .average().takeUnless { it.isNaN() } ?: INITIAL_TEMPERATURE
        when {
            heatSource -> HEAT_SOURCE_TEMPERATURE
            coldSource -> COLD_SOURCE_TEMPERATURE
            else -> averageTemperature
        }
    }
