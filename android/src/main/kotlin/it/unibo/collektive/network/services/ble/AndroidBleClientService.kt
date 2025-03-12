package it.unibo.collektive.network.services.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import it.unibo.collektive.network.services.ble.BleUtils.characteristicUuid
import it.unibo.collektive.network.services.ble.BleUtils.checkBluetoothPermission
import it.unibo.collektive.network.services.ble.BleUtils.serviceUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class AndroidBleClientService(
    deviceId: String,
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothScanner = bluetoothAdapter.bluetoothLeScanner
    private val buildScanFilters = listOf(
        ScanFilter.Builder().apply {
            val mask = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
            val maskUuid = ParcelUuid.fromString(mask)
            setServiceUuid(ParcelUuid(serviceUuid), maskUuid)
        }.build()
    )
    private val scanSettings: ScanSettings = ScanSettings.Builder().apply {
        setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
    }.build()
    private val connectedDevices = mutableSetOf<BluetoothGatt>()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) = context.checkBluetoothPermission {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (connectedDevices.map { it.device }.contains(device)) {
                Log.i("CollektiveBleClient", "Device already connected: ${device.name ?: "Unnamed"}")
                return@checkBluetoothPermission
            }
            scope.launch {
                Log.i("CollektiveBleClient", "Found device: ${device.name ?: "Unnamed"}")
                device.connectGatt(context, false, gattCallback)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scope.launch {
                Toast.makeText(context, "Scan Failed: $errorCode", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) =
            context.checkBluetoothPermission {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i("CollektiveBleClient", "Connected to GATT server: ${gatt?.device?.name}")
                        gatt?.let { connectedDevices.add(it) }
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i("CollektiveBleClient", "Disconnected from GATT server: ${gatt?.device?.name}")
                        gatt?.let { connectedDevices.remove(it) }
                        gatt?.device?.name?.let {
                            Log.i("CollektiveBleClient", "Removing data for device: $it")
                        }
                    }
                    else -> Log.i("CollektiveBleClient", "Bluetooth state changed: $newState")
                }
            }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            context.checkBluetoothPermission {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val characteristic = gatt?.getService(serviceUuid)?.getCharacteristic(characteristicUuid)
                    if (characteristic != null) {
                        gatt.readCharacteristic(characteristic)
                    } else {
                        Log.e("CollektiveBleClient", "Characteristic not found")
                    }
                } else {
                    Log.e("CollektiveBleClient", "Service discovery failed with status: $status")
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            context.checkBluetoothPermission {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("CollektiveBleClient", "Characteristic read: ${value.decodeToString()}")
                    if (characteristic.uuid == characteristicUuid) {
                        gatt.device?.let {
                            _messagesFlow.tryEmit(value)
                        }
                    }
                } else {
                    Log.e("CollektiveBleClient", "Characteristic read failed with status: $status")
                }
            }
        }
    }
    private val _messagesFlow = MutableSharedFlow<ByteArray>(1)

    init {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth not supported or disabled", Toast.LENGTH_SHORT).show()
        }
        scope.launch {
            while (true) {
                context.checkBluetoothPermission {
                    connectedDevices.forEach {
                        val characteristic = it.getService(serviceUuid)?.getCharacteristic(characteristicUuid)
                        characteristic?.let { charact ->
                            it.readCharacteristic(charact)
                        }
                    }
                }
                delay(1.seconds)
            }
        }
    }

    suspend fun startScanning() = withContext(Dispatchers.IO) {
        Log.i("CollektiveBleClient", "Starting BLE scan")
        context.checkBluetoothPermission {
            bluetoothScanner.startScan(buildScanFilters, scanSettings, scanCallback)
        }
    }

    fun getNeighborsData(): Flow<ByteArray> = _messagesFlow
}