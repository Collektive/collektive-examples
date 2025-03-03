package it.unibo.collektive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    mainEntrypoint(
        startDeviceId = 0,
        deviceCount = 10,
        dispatcher = Dispatchers.IO,
    )
}
