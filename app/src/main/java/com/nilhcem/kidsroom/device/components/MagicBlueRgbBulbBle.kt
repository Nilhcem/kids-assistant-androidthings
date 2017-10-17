package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import com.nilhcem.kidsroom.data.Button
import java.util.*

class MagicBlueRgbBulbBle(private val context: Context) : LifecycleObserver {

    companion object {
        private val TAG = MagicBlueRgbBulbBle::class.java.simpleName!!

        private val DEVICE_ADDRESS = "F8:1D:78:62:92:2F"
        private val SERVICE_UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb")
        private val CHARACTERISTIC_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb")
    }

    private val Button.bleValue: ByteArray
        get() = when (this) {
            Button.RED -> colorToCharacteristicByteArray(Color.RED)
            Button.GREEN -> colorToCharacteristicByteArray(Color.GREEN)
            Button.BLUE -> colorToCharacteristicByteArray(Color.BLUE)
            Button.YELLOW -> colorToCharacteristicByteArray(Color.YELLOW)
            Button.WHITE -> colorToCharacteristicByteArray(Color.WHITE)
            Button.BLACK -> colorToCharacteristicByteArray(Color.BLACK)
        }

    private val bluetoothManager by lazy { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter by lazy { bluetoothManager.adapter }
    private var bluetoothGatt: BluetoothGatt? = null
    private var pending: Button? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT client. Attempting to start service discovery")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT client")
                    stopClient()
                }
                else -> Log.w(TAG, "Connection state changed: $newState")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Connected")
                pending?.let { button -> writeCharacteristic(gatt, button) }.also { pending = null }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_ON -> startClient()
                BluetoothAdapter.STATE_OFF -> stopClient()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        Log.d(TAG, "Start")
        require(context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { "App requires Bluetooth support" }

        // Register for system Bluetooth events
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothReceiver, filter)
        if (bluetoothAdapter.isEnabled) {
            Log.i(TAG, "Bluetooth enabled... starting client")
            startClient()
        } else {
            Log.w(TAG, "Bluetooth is currently disabled... enabling")
            bluetoothAdapter.enable()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        Log.d(TAG, "Stop")

        if (bluetoothAdapter.isEnabled) {
            stopClient()
        }
        context.unregisterReceiver(bluetoothReceiver)
    }

    fun onButtonPressed(button: Button) {
        val gatt = bluetoothGatt

        if (gatt == null) {
            pending = button
            startClient()
        } else {
            writeCharacteristic(gatt, button)
        }
    }

    fun startClient() {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS)
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)

        if (bluetoothGatt == null) {
            Log.w(TAG, "Unable to create GATT client")
            return
        }
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, button: Button) {
        val service = gatt.getService(SERVICE_UUID)
        if (service == null) {
            Log.e(TAG, "Service is null. Bluetooth device may not be reachable")
        } else {
            val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID).apply {
                value = button.bleValue
            }
            gatt.writeCharacteristic(characteristic)
        }
    }

    private fun stopClient() {
        bluetoothGatt?.close().also { bluetoothGatt = null }
    }

    private fun colorToCharacteristicByteArray(color: Int): ByteArray {
        val red = ((color and 0x00ff0000) shr 16).toByte()
        val green = ((color and 0x0000ff00) shr 8).toByte()
        val blue = ((color and 0x000000ff)).toByte()
        return byteArrayOf(0x56.toByte(), red, green, blue, 0x00.toByte(), 0xf0.toByte(), 0xaa.toByte())
    }
}
