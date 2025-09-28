package it.unibo.collektive.examples.temperature

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables

private const val HEAT_SOURCE_TEMPERATURE = 30.0
private const val COLD_SOURCE_TEMPERATURE = -20.0
private const val INITIAL_TEMPERATURE = 0.0

/**
 * Entry point for the program which computes the temperature's field.
 */
fun Aggregate<Int>.temperatureEntrypoint(env: EnvironmentVariables): Double =
    temperature(env["heat_source"], env["cold_source"])

/**
 * Represents an evolving temperature field where each device updates its temperature over time.
 * Some devices act as fixed heat sources at 30°C, others as fixed cold sources at -20°C.
 * All other devices calculate their temperature dynamically as the average temperature of their neighboring devices.
 */
fun Aggregate<Int>.temperature(heatSource: Boolean, coldSource: Boolean): Double =
    share(INITIAL_TEMPERATURE) { previousTemperatures ->
        val averageTemperature = previousTemperatures.neighbors
            .values
            .sequence
            .average().takeIf { it.isFinite() } ?: INITIAL_TEMPERATURE
        when {
            heatSource -> HEAT_SOURCE_TEMPERATURE
            coldSource -> COLD_SOURCE_TEMPERATURE
            else -> averageTemperature
        }
    }
