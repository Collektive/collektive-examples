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
import kotlinx.coroutines.delay
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
) : AbstractSerializableMailbox<Int>(deviceId, serializer, retentionTime) {
    private val logger = KotlinLogging.logger("${MqttMailbox::class.simpleName!!}@$deviceId")
    private val internalScope: CoroutineScope = CoroutineScope(dispatcher)
    private val client = MkttClient(dispatcher) {
        brokerUrl = host
        this.port = port
    }

    private suspend fun initializeMqttClient() {
        client.connect()
        logger.info { "Device $deviceId with clientId connected to MQTT Broker" }
        internalScope.launch(dispatcher) {
            client.subscribe("drone/+").collect {
                val neighborDeviceId = it.topic.split("/").last().toInt()
                addNeighbor(neighborDeviceId)
                logger.debug { "Device $neighborDeviceId registered as neighbor of $deviceId" }
            }
        }
        internalScope.launch(dispatcher) {
            while (true) {
                client.publish("drone/$deviceId", byteArrayOf())
                delay(5.seconds)
            }
        }
        logger.info { "Complete MQTT initialization for device $deviceId" }
    }

    private suspend fun registerReceiverListener(): Unit = coroutineScope {
        initializeMqttClient()
        // Listen for incoming messages
        internalScope.launch(dispatcher) {
            client.subscribe("drone/$deviceId/neighbors").collect {
                try {
                    val deserialized = serializer.decode(it.payload)
                    logger.debug { "Received new message from ${deserialized.senderId} to $deviceId" }
                    deliverableReceived(deserialized)
                } catch (exception: SerializationException) {
                    logger.error { "Error decoding message from ${it.topic}: ${exception.message}" }
                }
            }
        }
    }

    override suspend fun close() {
        internalScope.cancel()
        client.disconnect()
        logger.info { "$deviceId disconnected from HiveMQ" }
    }

    override fun onDeliverableReceived(receiverId: Int, message: Message<Int, Any?>) {
        internalScope.launch {
            logger.debug { "From device ${message.senderId} to $receiverId: $message" }
            val payload = serializer.encode(message as SerializedMessage<Int>)
            client.publish(
                topic = "drone/$receiverId/neighbors",
                qos = MqttQoS.AtLeastOnce,
                message = payload,
            )
        }
    }

    companion object {
        suspend operator fun invoke(
            deviceId: Int,
            host: String,
            port: Int = 1883,
            serializer: SerialFormat = Json,
            retentionTime: Duration = 5.seconds,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
        ): MqttMailbox = coroutineScope {
            MqttMailbox(deviceId, host, port, serializer, retentionTime, dispatcher).apply {
                registerReceiverListener()
            }
        }
    }
}
