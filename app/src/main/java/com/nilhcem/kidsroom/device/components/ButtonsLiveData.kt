package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.android.things.contrib.driver.button.Button as DriverLayerButton
import com.nilhcem.kidsroom.data.Button as DataLayerButton

class ButtonsLiveData : LiveData<DataLayerButton>() {

    companion object {
        private val TAG = ButtonsLiveData::class.java.simpleName!!
        private const val BUTTON_DEBOUNCE_DELAY_MS = 20L
    }

    private val DataLayerButton.gpio: String
        get() = when (this) {
            DataLayerButton.RED -> "GPIO1_IO18"
            DataLayerButton.GREEN -> "GPIO4_IO19"
            DataLayerButton.BLUE -> "GPIO5_IO02"
            DataLayerButton.YELLOW -> "GPIO4_IO22"
            DataLayerButton.WHITE -> "GPIO4_IO21"
            DataLayerButton.BLACK -> "GPIO2_IO03"
        }

    private var buttons: Map<DriverLayerButton, DataLayerButton>? = null

    private val listener = { button: DriverLayerButton, pressed: Boolean ->
        if (pressed) {
            buttons?.get(button)?.let {
                value = it
            }
        }
    }

    override fun onActive() {
        Log.d(TAG, "onActive")
        buttons = DataLayerButton.values()
                .map {
                    DriverLayerButton(it.gpio, DriverLayerButton.LogicState.PRESSED_WHEN_LOW).apply {
                        setDebounceDelay(BUTTON_DEBOUNCE_DELAY_MS)
                        setOnButtonEventListener(listener)
                    } to it
                }
                .toMap()
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive")
        buttons?.let { buttons ->
            buttons.forEach { (button, _) ->
                button.setOnButtonEventListener(null)
                button.close()
            }
        }.also {
            buttons = null
        }
    }
}
