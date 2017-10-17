package com.nilhcem.kidsroom

import android.arch.lifecycle.LiveData
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback

class Rc522LiveData : LiveData<String>() {

    companion object {
        private val TAG = Rc522LiveData::class.java.simpleName!!
        private const val UART_NAME = "UART3"
        private const val UART_BUFFER_SIZE = 512
    }

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    private var uartDevice: UartDevice? = null
    private var pendingUartData = ""

    private val callback = object : UartDeviceCallback() {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            readFromUart(uart)

            while (true) {
                val line = getNextLine() ?: break
                postValue(line)
            }

            // Continue listening for more interrupts.
            return true
        }

        override fun onUartDeviceError(uart: UartDevice, error: Int) {
            Log.e(TAG, "UART device error ($error)");
        }

        private fun readFromUart(uart: UartDevice) {
            var read: Int
            val buffer = ByteArray(UART_BUFFER_SIZE)

            while (true) {
                read = uart.read(buffer, UART_BUFFER_SIZE)
                if (read == 0) break
                pendingUartData += String(buffer, 0, read, Charsets.UTF_8)
            }
        }

        private fun getNextLine(): String? {
            val index = pendingUartData.indexOf('\n')

            if (index != -1) {
                val line = pendingUartData.substring(0, index + 1).trim()
                pendingUartData = if (pendingUartData.length == index) "" else pendingUartData.substring(index + 1)
                return line
            }
            return null
        }
    }

    override fun onActive() {
        Log.d(TAG, "onActive")
        openUart()
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive")
        closeUart()
    }

    private fun openUart() {
        handlerThread = HandlerThread(TAG).also { handlerThread ->
            handlerThread.start()
            handler = Handler(handlerThread.looper)
        }

        uartDevice = PeripheralManagerService().openUartDevice(UART_NAME).apply {
            setBaudrate(9600)
            setDataSize(8)
            setParity(UartDevice.PARITY_NONE)
            setStopBits(1)

            registerUartDeviceCallback(callback, handler)
        }
    }

    private fun closeUart() {
        handler = null
        handlerThread?.quitSafely().also { handlerThread = null }

        uartDevice?.let {
            it.unregisterUartDeviceCallback(callback)
            it.close()
        }.also { uartDevice = null }
    }
}
