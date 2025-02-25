package it.unibo.collektive.network

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import io.reactivex.Flowable
import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.aggregate.api.Serialize
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.networking.SerializedMessage
import it.unibo.collektive.networking.SerializedMessageFactory
import it.unibo.collektive.path.Path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.rx2.await
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MqttMailbox private constructor(
    private val deviceId: Int,
    host: String,
    port: Int = 1883,
    private val serializer: SerialFormat = Json,
    private val retentionTime: Duration = 5.seconds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Mailbox<Int> {
    private data class TimedMessage(val message: Message<Int, Any?>, val timestamp: Instant)
    private val logger = LoggerFactory.getLogger(javaClass)
    private val factory = object : SerializedMessageFactory<Int, Any?>(serializer) {}
    private val messages = ConcurrentHashMap<Int, TimedMessage>()
    private val channel = MutableSharedFlow<Message<Int, Any?>>()
    private val internalScope = CoroutineScope(dispatcher)
    private val client =
        Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(host)
            .serverPort(port)
            .buildRx()

    private suspend fun initializeHivemq(): Unit = coroutineScope {
        client.connect().await()
        logger.info("Device $deviceId with clientId ${client.config.clientIdentifier.get()} connected to HiveMQ")
    }

    private suspend fun registerReceiverListener(): Unit = coroutineScope {
        fun processMessage(payload: ByteBuffer): SerializedMessage<Int> {
            val encoded = StandardCharsets.UTF_8.decode(payload).toString()
            return serializer.decode(encoded.encodeToByteArray())
        }
        initializeHivemq()
        // Listen for incoming messages
        launch(dispatcher) {
            client.subscribePublishesWith()
                .topicFilter("drone/+/neighbors")
                .qos(MqttQos.AT_LEAST_ONCE)
                .applySubscribe()
                .asFlow()
                .filter { it.topic.toString().split("/")[1].toInt() != deviceId } // Ignore messages from self
                .collect { mqtt5Publish ->
                    mqtt5Publish.payload.getOrNull()?.let {
                        val serializedMessage = processMessage(it)
                        messages[serializedMessage.senderId] = TimedMessage(serializedMessage, System.now())
                    }
                }
        }
        // Listen for outgoing messages
        launch(dispatcher) {
            channel.collect { message ->
                Mqtt5Publish.builder()
                    .topic("drone/$deviceId/neighbors")
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(ByteBuffer.wrap(serializer.encode(message as SerializedMessage<Int>)))
                    .build()
                    .let { client.publish(Flowable.just(it)).awaitSingle() }
            }
        }
    }

    suspend fun close() {
        internalScope.cancel()
        client.disconnect().await()
        logger.info("$deviceId disconnected from HiveMQ")
    }

    override val inMemory: Boolean
        get() = false

    override fun deliverableFor(
        id: Int,
        outboundMessage: OutboundEnvelope<Int>
    ) {
        val message = outboundMessage.prepareMessageFor(id, factory)
        internalScope.launch { channel.emit(message) }
    }

    override fun deliverableReceived(message: Message<Int, *>) {
        messages[message.senderId] = TimedMessage(message, System.now())
    }

    override fun currentInbound(): NeighborsData<Int> =
        object : NeighborsData<Int> {
            // First, remove all messages that are older than the retention time
            init {
                val nowInstant = System.now()
                messages.values.removeIf { it.timestamp < nowInstant - retentionTime }
            }
            override val neighbors: Set<Int> get() = messages.keys

            override fun <Value> dataAt(
                path: Path,
                dataSharingMethod: DataSharingMethod<Value>
            ): Map<Int, Value> {
                require(dataSharingMethod is Serialize<Value>) {
                    "Serialization has been required for in-memory messages. This is likely a misconfiguration."
                }
                return messages
                    .mapValues { (_, timedMessage) ->
                        require(timedMessage.message.sharedData.all { it.value is ByteArray }) {
                            "Message ${timedMessage.message.senderId} is not serialized"
                        }
                        timedMessage.message.sharedData.getOrElse(path) { NoValue }
                    }.filterValues { it != NoValue }
                    .mapValues { (_, payload) ->
                        val byteArrayPayload = payload as ByteArray
                        when (serializer) {
                            is StringFormat ->
                                serializer.decodeFromString(
                                    dataSharingMethod.serializer,
                                    byteArrayPayload.decodeToString()
                                )
                            is BinaryFormat ->
                                serializer.decodeFromByteArray(
                                    dataSharingMethod.serializer,
                                    byteArrayPayload
                                )
                            else -> error("Unsupported serializer: ${serializer::class}")
                        }
                    }
            }

        }
    private object NoValue

    companion object {
        private fun SerialFormat.encode(value: SerializedMessage<Int>): ByteArray =
            when (this) {
                is StringFormat -> encodeToString(value).toByteArray()
                is BinaryFormat -> encodeToByteArray(value)
                else -> error("Unsupported serializer")
            }
        private fun SerialFormat.decode(value: ByteArray): SerializedMessage<Int> =
            when (this) {
                is StringFormat -> decodeFromString(value.decodeToString())
                is BinaryFormat -> decodeFromByteArray(value)
                else -> error("Unsupported serializer")
            }
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