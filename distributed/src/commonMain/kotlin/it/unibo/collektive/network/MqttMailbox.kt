package it.unibo.collektive.network

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
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
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
//    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val channel = MutableSharedFlow<Message<Int, Any?>>()
    private val internalScope = CoroutineScope(dispatcher)
    private val client = MkttClient(dispatcher) {
        brokerUrl = host
        this.port = port
    }

    private suspend fun initializeMqttClient(): Unit = coroutineScope {
        client.connect()
//        logger.info("Device $deviceId with clientId ${client.config.clientIdentifier.get()} connected to HiveMQ")
    }

    private suspend fun registerReceiverListener(): Unit = coroutineScope {
        initializeMqttClient()
        // Listen for incoming messages
        launch(dispatcher) {
            client.subscribe("drone/+/neighbors").collect {
                println("OOO")
                val deserialized = serializer.decode(it.payload)
                messages[deserialized.senderId] = TimedMessage(deserialized, Clock.System.now())
            }
        }
        // Listen for outgoing messages
        launch(dispatcher) {
            channel.collect { message ->
                val payload = serializer.encode(message as SerializedMessage<Int>)
                client.publish(
                    topic = "drone/$deviceId/neighbors",
                    qos = MqttQoS.AtLeastOnce,
                    message = payload
                )
            }
        }
    }

    override suspend fun close() {
        internalScope.cancel()
        client.disconnect()
//        logger.info("$deviceId disconnected from HiveMQ")
    }

    override fun onDeliverableReceived(message: Message<Int, Any?>) {
        internalScope.launch { channel.emit(message) }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        operator fun invoke(
            deviceId: Int,
            host: String,
            port: Int = 1883,
            serializer: SerialFormat = ProtoBuf,
            retentionTime: Duration = 5.seconds,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ): MqttMailbox =
            MqttMailbox(deviceId, host, port, serializer, retentionTime, dispatcher).apply {
                internalScope.launch { registerReceiverListener() }
            }
    }
}