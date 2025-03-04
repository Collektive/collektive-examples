package it.unibo.collektive.network

import io.github.oshai.kotlinlogging.KotlinLogging
import it.nicolasfarabegoli.mktt.MkttClient
import it.nicolasfarabegoli.mktt.MqttQoS
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.SerializedMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MqttMailbox private constructor(
    private val deviceId: Int,
    host: String,
    port: Int = 1883,
    private val serializer: SerialFormat = Json,
    retentionTime: Duration = 5.seconds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AbstractSerializableMailbox<Int>(serializer, retentionTime) {
    private val logger = KotlinLogging.logger(MqttMailbox::class.simpleName!!)
    private val channel = MutableSharedFlow<Message<Int, Any?>>()
    private val internalScope = CoroutineScope(dispatcher)
    private val client =
        MkttClient(dispatcher) {
            brokerUrl = host
            this.port = port
        }

    private suspend fun initializeMqttClient(): Unit =
        coroutineScope {
            client.connect()
            logger.info { "Device $deviceId with clientId connected to MQTT Broker" }
        }

    private suspend fun registerReceiverListener(): Unit =
        coroutineScope {
            initializeMqttClient()
            // Listen for incoming messages
            launch(dispatcher) {
                client.subscribe("drone/+/neighbors").collect {
                    try {
                        val deserialized = serializer.decode(it.payload)
                        // Find a better way to prevent storing the self-message
                        if (deserialized.senderId != deviceId) {
                            deliverableReceived(deserialized)
                        }
                    } catch (exception: SerializationException) {
                        logger.error { "Error decoding message from ${it.topic}: ${exception.message}" }
                    }
                }
            }
            // Listen for outgoing messages
            launch(dispatcher) {
                channel.collect { message ->
                    val payload = serializer.encode(message as SerializedMessage<Int>)
                    client.publish(
                        topic = "drone/$deviceId/neighbors",
                        qos = MqttQoS.AtLeastOnce,
                        message = payload,
                    )
                }
            }
        }

    override suspend fun close() {
        internalScope.cancel()
        client.disconnect()
        logger.info { "$deviceId disconnected from HiveMQ" }
    }

    override fun onDeliverableReceived(message: Message<Int, Any?>) {
        internalScope.launch { channel.emit(message) }
    }

    companion object {
        operator fun invoke(
            deviceId: Int,
            host: String,
            port: Int = 1883,
            serializer: SerialFormat = Json,
            retentionTime: Duration = 5.seconds,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
        ): MqttMailbox =
            MqttMailbox(deviceId, host, port, serializer, retentionTime, dispatcher).apply {
                internalScope.launch { registerReceiverListener() }
            }
    }
}
