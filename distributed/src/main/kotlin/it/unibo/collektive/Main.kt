package it.unibo.collektive

import it.unibo.collektive.Collektive.Companion.aggregate
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.network.MqttMailbox
import it.unibo.collektive.path.Path
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

private const val DEFAULT_DEVICE_COUNT = 50
private val DEFAULT_ROUND_TIME = 1.seconds
private val DEFAULT_EXECUTE_FOR = 10.seconds

fun main() = runBlocking {
    val logger = LoggerFactory.getLogger(javaClass)
    val roundTime = System.getenv("ROUND_TIME")?.toInt()?.seconds ?: DEFAULT_ROUND_TIME
    val executeFor = System.getenv("EXECUTE_FOR")?.toInt()?.seconds ?: DEFAULT_EXECUTE_FOR
    val deviceCount = System.getenv("DEVICE_COUNT")?.toInt() ?: DEFAULT_DEVICE_COUNT
    logger.info("Starting Collektive with $deviceCount devices")
    val jobRefs = mutableSetOf<Job>()
    val networks = mutableSetOf<MqttMailbox>()
    // Create a network of devices
    for (id in 0..deviceCount) {
        val job = launch {
            val network = MqttMailbox(id, "broker.hivemq.com")
            networks.add(network)
            var previousState = emptyMap<Path, Any?>()
            while (true) {
                val result = aggregate(id, network, previousState) {
                    neighboring(id)
                }
                logger.info(result.result.toString())
                previousState = result.newState
                delay(roundTime)
            }
        }
        jobRefs.add(job)
    }
    delay(executeFor)
    // Gracefully shutdown
    jobRefs.forEach { it.cancel() }
    networks.forEach { it.close() }
    logger.info("Bye, Collektive!")
}