package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.android.things.contrib.driver.button.Button.OnButtonEventListener
import com.google.android.things.contrib.driver.button.Button as DriverLayerButton
import com.nilhcem.kidsroom.data.Button as DataLayerButton

class ButtonsLiveData : LiveData<Pair<DataLayerButton, Boolean>>() {

    companion object {
        private val TAG = ButtonsLiveData::class.java.simpleName!!
        private const val BUTTON_DEBOUNCE_DELAY_MS = 20L
    }

    private val DataLayerButton.gpio: String
        get() = when (this) {
            DataLayerButton.RED -> "GPIO_37"
            DataLayerButton.GREEN -> "GPIO_32"
            DataLayerButton.BLUE -> "GPIO_39"
            DataLayerButton.YELLOW -> "GPIO_34"
            DataLayerButton.WHITE -> "GPIO_33"
            DataLayerButton.BLACK -> "GPIO_174"
        }

    private var buttons: Map<DriverLayerButton, DataLayerButton>? = null

    private val listener = OnButtonEventListener { button: DriverLayerButton, pressed: Boolean ->
        buttons?.get(button)?.let {
            value = it to pressed
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
