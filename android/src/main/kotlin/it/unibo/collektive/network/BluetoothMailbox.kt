package it.unibo.collektive.network

import android.content.Context
import android.util.Log
import it.unibo.collektive.network.services.ble.AndroidBleClientService
import it.unibo.collektive.network.services.ble.AndroidBleServerService
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.SerializedMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BluetoothMailbox(
    deviceId: String,
    context: Context,
    scope: CoroutineScope,
    private val serializer: SerialFormat = Json,
    retentionTime: Duration = 5.seconds,
) : AbstractSerializableMailbox<String>(serializer, retentionTime) {
    private val androidBleService = AndroidBleServerService(deviceId, context, scope)
    private val androidBleClient = AndroidBleClientService(deviceId, context, scope)

    init {
        Log.i("BluetoothMailbox", "Initializing BluetoothMailbox for device $deviceId")
        androidBleService.startGattServer()
        scope.launch(Dispatchers.IO) { androidBleClient.startScanning() }
        scope.launch {
            while (true) {
                val messages = androidBleClient.getNeighborsData()
                messages.forEach { (_, value) ->
                    val deserialized = serializer.decodeString(value)
                    deliverableReceived(deserialized)
                }
                delay(1.seconds)
            }
        }
    }

    override suspend fun close() {
        androidBleService.close()
    }

    override fun onDeliverableReceived(message: Message<String, Any?>) {
        require(message is SerializedMessage<String>) { "Message must be serialized" }
        val payload = serializer.encode(message)
        Log.i("CollektiveBleMailbox", "Sending message: ${payload.decodeToString()}")
        androidBleService.setNewOutboundMessage(payload)
    }
}
