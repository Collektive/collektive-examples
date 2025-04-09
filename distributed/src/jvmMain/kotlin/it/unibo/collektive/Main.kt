package it.unibo.collektive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * TODO add documentation.
 */
@Suppress("InjectDispatcher")
fun main() = runBlocking {
    mainEntrypoint(
        startDeviceId = 0,
        deviceCount = 3,
        dispatcher = Dispatchers.IO,
    )
}
