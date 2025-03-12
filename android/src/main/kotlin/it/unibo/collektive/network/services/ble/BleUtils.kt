package it.unibo.collektive.network.services.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

object BleUtils {
    val serviceUuid: UUID = UUID.fromString("0000AAAA-0000-1000-8000-00805F9B34FB")
    val characteristicUuid: UUID = UUID.fromString("0000BBBB-0000-1000-8000-00805F9B34FB")

    private fun Context.hasBluetoothPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.checkBluetoothPermission(onPermissionGrant: () -> Unit) {
        if (!hasBluetoothPermissions()) {
            Log.i("CollektiveBlePermission", "Asking for permission")
            return
        }
        onPermissionGrant()
    }

    fun Context.requestBluetoothPermissions() {
        Log.i("BLERequest", "Requesting permissions")
        if (!hasBluetoothPermissions() && this is Activity) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ),
                1
            )
        }
    }

    fun BluetoothManager.isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = adapter
        return bluetoothAdapter?.isEnabled == true
    }

    fun Context.ensureBluetoothEnabled(bleManager: BluetoothManager) = checkBluetoothPermission {
        val bluetoothAdapter = bleManager.adapter
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled && this is Activity) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }
    }
}