package it.unibo.collektive.network.services.ble

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

class AndroidBleClientService(
    deviceId: String,
    private val context: Context,
    scope: CoroutineScope,
) {
    private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothScanner = bluetoothAdapter.bluetoothLeScanner
    private val buildScanFilters = listOf<ScanFilter>(
        ScanFilter.Builder().apply {
            setServiceUuid(ParcelUuid(serviceUuid))
        }.build()
    )
    private val scanSettings: ScanSettings = ScanSettings.Builder().apply {
        setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
    }.build()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) = context.checkBluetoothPermission {
            super.onScanResult(callbackType, result)
            val device = result.device
            scope.launch {
                // Try read the device name
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
                        Log.i("CollektiveBleClient", "Connected to GATT server")
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i("CollektiveBleClient", "Disconnected from GATT server")
                        lastNeighborData.remove(gatt?.device?.name)
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
                if (characteristic.uuid == characteristicUuid) {
                    Log.i("CollektiveBleClient", "Characteristic read: ${value.decodeToString()}")
                    gatt.device?.let {
                        lastNeighborData[it.name] = value
                    }
                }
            }
        }
    }
    private val lastNeighborData = mutableMapOf<String, ByteArray>()

    init {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth not supported or disabled", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun startScanning() = withContext(Dispatchers.IO) {
        Log.i("CollektiveBleClient", "Starting BLE scan")
        context.checkBluetoothPermission {
            bluetoothScanner.startScan(buildScanFilters, scanSettings, scanCallback)
        }
    }

    fun getNeighborsData(): Map<String, ByteArray> = lastNeighborData
}