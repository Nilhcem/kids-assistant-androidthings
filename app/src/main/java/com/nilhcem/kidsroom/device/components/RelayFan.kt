package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.nilhcem.kidsroom.data.Button

class RelayFan : LifecycleObserver {

    companion object {
        private const val GPIO_NAME = "GPIO_35"
    }

    private val Button.fanState: Boolean
        get() = when (this) {
            Button.RED -> true
            Button.GREEN -> false
            Button.BLUE -> true
            Button.YELLOW -> false
            Button.WHITE -> true
            Button.BLACK -> false
        }

    private var relay: Gpio? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        relay = PeripheralManagerService().openGpio(GPIO_NAME).apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            setActiveType(Gpio.ACTIVE_LOW)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        relay?.close().also { relay = null }
    }

    fun onButtonPressed(button: Button) {
        relay?.value = button.fanState
    }
}
