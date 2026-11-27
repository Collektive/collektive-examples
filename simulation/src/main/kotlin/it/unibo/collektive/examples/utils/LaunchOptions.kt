package it.unibo.collektive.examples.utils

import it.unibo.alchemist.boundary.kotlindsl.SimulationContext
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime

/**
 * System properties read by the `*.alchemist.kts` scenarios.
 * Alchemist does not apply `--override` to Kotlin scripts,
 * so the Gradle run tasks pass these options as system properties instead.
 */
object LaunchOptions {
    /**
     * Path of the effects file: when set, the scenario opens the Swing GUI, paused.
     */
    const val EFFECTS = "collektive.examples.effects"

    /**
     * Simulated time after which the simulation terminates.
     */
    const val END_TIME = "collektive.examples.endTime"
}

/**
 * Applies the [LaunchOptions] set as system properties to the current scenario:
 * a GUI monitor with a paused launcher when [LaunchOptions.EFFECTS] is set,
 * an [AfterTime] terminator when [LaunchOptions.END_TIME] is set.
 * Call it inside the `environment { }` block.
 */
context(environment: Environment<T, P>)
fun <T, P : Position2D<P>> SimulationContext<T, P>.applyLaunchOptions() {
    System.getProperty(LaunchOptions.END_TIME)?.let { endTime ->
        environment.addTerminator(AfterTime(DoubleTime(endTime.toDouble())))
    }
    System.getProperty(LaunchOptions.EFFECTS)?.let { effects ->
        @Suppress("DEPRECATION") // Same monitor the YAML runs used: no replacement is available yet.
        monitor(SwingGUI(environment, effects))
        launcher(DefaultLauncher(batch = emptyList(), autoStart = false))
    }
}
