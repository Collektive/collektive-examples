package it.unibo.collektive.network

import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.aggregate.api.Serialize
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.networking.SerializedMessage
import it.unibo.collektive.networking.SerializedMessageFactory
import it.unibo.collektive.path.Path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

abstract class AbstractSerializableMailbox<ID : Any>(
    private val serializer: SerialFormat,
    private val retentionTime: Duration,
) : Mailbox<ID> {
    protected data class TimedMessage<ID : Any>(val message: Message<ID, Any?>, val timestamp: Instant)
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)
    protected val messages = ConcurrentHashMap<ID, TimedMessage<ID>>()
    private val factory = object : SerializedMessageFactory<ID, Any?>(serializer) {}
    private val neighborMessageFlow = MutableSharedFlow<Message<ID, Any?>>()

    /**
     * Typically, a network-based mailbox provides a way to gracefully close the connection.
     * This method should be called when the mailbox is no longer needed.
     */
    abstract suspend fun close()

    /**
     * This method is called when a message is ready to be sent to the network.
     */
    abstract fun onDeliverableReceived(message: Message<ID, Any?>)

    /**
     * Returns an asynchronous flow of messages received from neighbors.
     */
    fun neighborsMessageFlow(): Flow<Message<ID, Any?>> = neighborMessageFlow

    final override val inMemory: Boolean
        get() = false

    final override fun deliverableFor(id: ID, outboundMessage: OutboundEnvelope<ID>) {
        logger.debug("Message {} ready to be sent", outboundMessage)
        val message = outboundMessage.prepareMessageFor(id, factory)
        onDeliverableReceived(message)
    }

    final override fun deliverableReceived(message: Message<ID, *>) {
        logger.debug("Received message from {}", message.senderId)
        messages[message.senderId] = TimedMessage(message, System.now())
        neighborMessageFlow.tryEmit(message)
    }

    final override fun currentInbound(): NeighborsData<ID> =
        object : NeighborsData<ID> {
            // First, remove all messages that are older than the retention time
            init {
                val nowInstant = System.now()
                messages.values.removeIf { it.timestamp < nowInstant - retentionTime }
            }
            override val neighbors: Set<ID> get() = messages.keys

            override fun <Value> dataAt(
                path: Path,
                dataSharingMethod: DataSharingMethod<Value>
            ): Map<ID, Value> {
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
                        serializer.decode(dataSharingMethod.serializer, byteArrayPayload)
                    }
            }

        }
    private object NoValue

    /**
     * Utilities for encoding and decoding messages.
     */
    companion object {
        /**
         * Encodes the [value] into a [ByteArray] according to the [SerialFormat].
         */
        fun SerialFormat.encode(value: SerializedMessage<Int>): ByteArray =
            when (this) {
                is StringFormat -> encodeToString(value).toByteArray()
                is BinaryFormat -> encodeToByteArray(value)
                else -> error("Unsupported serializer")
            }

        /**
         * Decodes the [value] from a [ByteArray] according to the [SerialFormat].
         */
        fun SerialFormat.decode(value: ByteArray): SerializedMessage<Int> =
            when (this) {
                is StringFormat -> decodeFromString(value.decodeToString())
                is BinaryFormat -> decodeFromByteArray(value)
                else -> error("Unsupported serializer")
            }

        /**
         * Decodes the [value] from a [ByteArray] using the [kSerializer] according to the [SerialFormat].
         */
        private fun <Value> SerialFormat.decode(
            kSerializer: KSerializer<Value>,
            value: ByteArray
        ): Value =
            when (this) {
                is StringFormat -> decodeFromString(kSerializer, value.decodeToString())
                is BinaryFormat -> decodeFromByteArray(kSerializer, value)
                else -> error("Unsupported serializer")
            }
    }
}