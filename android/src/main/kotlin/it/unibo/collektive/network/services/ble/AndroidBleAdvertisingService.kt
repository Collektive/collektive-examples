package it.unibo.collektive.network.services.ble

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import it.unibo.collektive.network.services.ble.BleUtils.checkBluetoothPermission
import it.unibo.collektive.network.services.ble.BleUtils.serviceUuid
import kotlinx.coroutines.CoroutineScope

class AndroidBleAdvertisingService(
    private val deviceId: String,
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
    private var isAdvertising = true

    fun newOutboundMessage(message: ByteArray) = context.checkBluetoothPermission {
        if (isAdvertising) {
            stopAdvertising()
            val dataToAdvertise = AdvertiseData.Builder().apply {
                val serviceId = ParcelUuid(serviceUuid)
                addServiceData(serviceId, message)
                setIncludeDeviceName(true)
            }.build()
            bluetoothAdvertiser.startAdvertising(
                advertisingSettings,
                dataToAdvertise,
                advertiseCallback
            )
        } else {
            Log.w("CollektiveBleAdvertising", "Cannot advertise new message, advertising is stopped")
        }
    }

    fun stopAdvertising() = context.checkBluetoothPermission {
        isAdvertising = false
        bluetoothAdvertiser.stopAdvertising(advertiseCallback)
    }

    companion object {
        private val advertisingSettings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(ADVERTISE_MODE_LOW_LATENCY)
            setTxPowerLevel(ADVERTISE_TX_POWER_HIGH)
            setConnectable(false)
        }.build()

        private val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                Log.i("CollektiveBleAdvertising", "Advertising last export started")
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.e("CollektiveBleAdvertising", "Advertising failed with error code $errorCode")
            }
        }
    }
}
