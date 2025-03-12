package it.unibo.collektive.network.services.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import it.unibo.collektive.network.services.ble.BleUtils.characteristicUuid
import it.unibo.collektive.network.services.ble.BleUtils.checkBluetoothPermission
import it.unibo.collektive.network.services.ble.BleUtils.ensureBluetoothEnabled
import it.unibo.collektive.network.services.ble.BleUtils.isBluetoothEnabled
import it.unibo.collektive.network.services.ble.BleUtils.requestBluetoothPermissions
import it.unibo.collektive.network.services.ble.BleUtils.serviceUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class AndroidBleServerService(
    private val deviceId: String,
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private lateinit var bluetoothGattServer: BluetoothGattServer

    private var lastOutboundMessage: ByteArray = byteArrayOf()

    init {
        context.requestBluetoothPermissions()
        context.ensureBluetoothEnabled(bluetoothManager)
    }

    fun setNewOutboundMessage(message: ByteArray) {
        lastOutboundMessage = message
    }

    fun startGattServer() = context.checkBluetoothPermission {
        Log.i("CollektiveBleServer", "Starting GATT server")
        bluetoothGattServer =
            bluetoothManager.openGattServer(context, gattServerCallback).apply {
                val service = BluetoothGattService(serviceUuid, SERVICE_TYPE_PRIMARY)
                val characteristic = BluetoothGattCharacteristic(
                    characteristicUuid,
                    PROPERTY_READ or PROPERTY_NOTIFY,
                    PERMISSION_READ
                )
                service.addCharacteristic(characteristic)
                addService(service)
            }
        advertise()
    }

    fun close() = context.checkBluetoothPermission {
        bluetoothGattServer.close()
    }

    private fun advertise() = context.checkBluetoothPermission {
        if (!bluetoothManager.isBluetoothEnabled()) {
            Log.e("CollektiveBleAdvertising", "Bluetooth is not enabled")
            return@checkBluetoothPermission
        }
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothAdapter.name = "collektive-$deviceId"
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(ADVERTISE_MODE_LOW_LATENCY)
            setTxPowerLevel(ADVERTISE_TX_POWER_HIGH)
            setConnectable(true)
        }.build()
        val data = AdvertiseData.Builder().apply {
            setIncludeDeviceName(true)
            addServiceUuid(ParcelUuid(serviceUuid))
        }.build()
        advertiser.startAdvertising(settings, data, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                Log.i("CollektiveBleAdvertising","Advertising started successfully")
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.e("CollektiveBleAdvertising","Advertising failed with error code: $errorCode")
            }
        })
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> Log.i("CollektiveGattCallback", "Connected BLE")
                BluetoothProfile.STATE_DISCONNECTED -> Log.i("CollektiveGattCallback", "Disconnected BLE")
                else -> Log.i("CollektiveGattCallback", "Bluetooth state changed: $newState")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if (characteristic?.uuid == characteristicUuid) {
                scope.launch(Dispatchers.IO) {
                    context.checkBluetoothPermission {
                        Log.i("CollektiveGattCallback", "Read request received")
                        val data = lastOutboundMessage.copyOfRange(offset, lastOutboundMessage.size)
                        bluetoothGattServer.sendResponse(
                            device,
                            requestId,
                            GATT_SUCCESS,
                            offset,
                            data
                        )
                    }
                }
            }
        }
    }
}
