package it.unibo.collektive

import kotlinx.coroutines.Dispatchers

suspend fun main() {
    mainEntrypoint(
        startDeviceId = 10,
        deviceCount = 10,
        dispatcher = Dispatchers.Default
    )
}