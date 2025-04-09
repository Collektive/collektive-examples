package it.unibo.collektive

import kotlinx.coroutines.Dispatchers

/**
 * TODO add documentation.
 */
suspend fun main() {
    mainEntrypoint(
        startDeviceId = 2,
        deviceCount = 2,
        dispatcher = Dispatchers.Default,
    )
}
