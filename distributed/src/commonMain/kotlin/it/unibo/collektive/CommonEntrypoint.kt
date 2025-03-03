package it.unibo.collektive

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

fun aggregateProgram(id: Int, network: Mailbox<Int>, previousState: Map<Path, Any?>): AggregateResult<Int, Int> {
    return aggregate(id, network, previousState) {
        neighboring(id).neighbors.size
    }
}

suspend fun mainEntrypoint(
    roundTime: Duration = DEFAULT_ROUND_TIME,
    executeFor: Duration = DEFAULT_EXECUTE_FOR,
    startDeviceId: Int = 0,
    deviceCount: Int = DEFAULT_DEVICE_COUNT,
    asyncNetwork: Boolean = false,
    dispatcher: CoroutineDispatcher,
) = coroutineScope {
//    val logger = LoggerFactory.getLogger(javaClass)

//    logger.info("Starting Collektive with $deviceCount devices")
    val jobRefs = mutableSetOf<Job>()
    val networks = mutableSetOf<MqttMailbox>()
    // Create a network of devices
    for (id in startDeviceId..(startDeviceId + deviceCount)) {
        val job = launch {
            val network = MqttMailbox(id, "broker.hivemq.com", dispatcher = dispatcher)
            networks.add(network)
            var previousState = emptyMap<Path, Any?>()
            when (asyncNetwork) {
                true -> network.neighborsMessageFlow().collect {
                    val result = aggregateProgram(id, network, previousState)
//                    logger.info(result.result.toString())
                    previousState = result.newState
                }
                false -> {
                    while (true) {
                        val result = aggregateProgram(id, network, previousState)
                        println(result.result.toString())
                        previousState = result.newState
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
//    logger.info("Bye, Collektive!")
}