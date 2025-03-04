package it.unibo.collektive.network

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import io.reactivex.Flowable
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.SerializedMessage
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.protobuf.ProtoBuf
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MqttMailbox
    @OptIn(ExperimentalSerializationApi::class)
    private constructor(
        private val deviceId: Int,
        host: String,
        port: Int = 1883,
        private val serializer: SerialFormat = ProtoBuf,
        retentionTime: Duration = 5.seconds,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : AbstractSerializableMailbox<Int>(serializer, retentionTime) {
        private val channel = MutableSharedFlow<Message<Int, Any?>>()
        private val internalScope = CoroutineScope(dispatcher)
        private val client =
            Mqtt5Client
                .builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(host)
                .serverPort(port)
                .buildRx()

        private suspend fun initializeHivemq(): Unit =
            coroutineScope {
                client.connect().await()
                logger.info("Device $deviceId with clientId ${client.config.clientIdentifier.get()} connected to HiveMQ")
            }

        private suspend fun registerReceiverListener(): Unit =
            coroutineScope {
                fun processMessage(payload: ByteBuffer): SerializedMessage<Int> {
                    val encoded = StandardCharsets.UTF_8.decode(payload).toString()
                    return serializer.decode(encoded.encodeToByteArray())
                }
                initializeHivemq()
                // Listen for incoming messages
                launch(dispatcher) {
                    client
                        .subscribePublishesWith()
                        .topicFilter("drone/+/neighbors")
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .applySubscribe()
                        .asFlow()
                        .filter {
                            it.topic
                                .toString()
                                .split("/")[1]
                                .toInt() != deviceId
                        } // Ignore messages from self
                        .collect { mqtt5Publish ->
                            mqtt5Publish.payload.getOrNull()?.let {
                                deliverableReceived(processMessage(it))
                            }
                        }
                }
                // Listen for outgoing messages
                launch(dispatcher) {
                    channel.collect { message ->
                        Mqtt5Publish
                            .builder()
                            .topic("drone/$deviceId/neighbors")
                            .qos(MqttQos.AT_LEAST_ONCE)
                            .payload(ByteBuffer.wrap(serializer.encode(message as SerializedMessage<Int>)))
                            .build()
                            .let { client.publish(Flowable.just(it)).awaitSingle() }
                    }
                }
            }

        override suspend fun close() {
            internalScope.cancel()
            client.disconnect().await()
            logger.info("$deviceId disconnected from HiveMQ")
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
                dispatcher: CoroutineDispatcher = Dispatchers.Default,
            ): MqttMailbox =
                MqttMailbox(deviceId, host, port, serializer, retentionTime, dispatcher).apply {
                    internalScope.launch { registerReceiverListener() }
                }
        }
    }
