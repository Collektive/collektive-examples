package it.unibo.collektive

import kotlinx.coroutines.Dispatchers

suspend fun main() {
    mainEntrypoint(
        startDeviceId = 2,
        deviceCount = 2,
        dispatcher = Dispatchers.Default,
    )
}
