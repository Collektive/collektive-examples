package it.unibo.collektive

import io.github.oshai.kotlinlogging.KotlinLogging
import it.unibo.collektive.Collektive.Companion.aggregate
import it.unibo.collektive.aggregate.AggregateResult
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.network.MqttMailbox
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.path.Path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val DEFAULT_DEVICE_COUNT = 50
private val DEFAULT_ROUND_TIME = 1.seconds
private val DEFAULT_EXECUTE_FOR = 60.seconds

fun aggregateProgram(id: Int, network: Mailbox<Int>): Collektive<Int, Collection<Int>> =
    Collektive(id, network) {
        neighboring(id).neighbors
    }

suspend fun mainEntrypoint(
    roundTime: Duration = DEFAULT_ROUND_TIME,
    executeFor: Duration = DEFAULT_EXECUTE_FOR,
    startDeviceId: Int = 0,
    deviceCount: Int = DEFAULT_DEVICE_COUNT,
    asyncNetwork: Boolean = false,
    dispatcher: CoroutineDispatcher,
) = coroutineScope {
    val logger = KotlinLogging.logger("Entrypoint")
    logger.info { "Starting Collektive with $deviceCount devices" }
    val jobRefs = mutableSetOf<Job>()
    val networks = mutableSetOf<MqttMailbox>()
    // Create a network of devices
    for (id in startDeviceId until (startDeviceId + deviceCount)) {
        val job =
            launch {
                val network = MqttMailbox(id, "test.mosquitto.org", dispatcher = dispatcher)
                logger.info { "Fdfdfdfdf" }
                networks.add(network)
                val program = aggregateProgram(id, network)
                when (asyncNetwork) {
                    true ->
                        network.neighborsMessageFlow().collect {
                            val result = program.cycle()
                            logger.info { "For device $id: $result" }
                        }
                    false -> {
                        while (true) {
                            val result = program.cycle()
                            logger.info { "For device $id: $result" }
                            delay(roundTime)
                        }
                    }
                }
            }
        jobRefs.add(job)
    }
    delay(executeFor)
    // Gracefully shutdown
    jobRefs.forEach { it.cancel() }
    networks.forEach { it.close() }
    logger.info { "Bye, Collektive!" }
}
