package com.nilhcem.kidsroom.device

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.nilhcem.assistant.androidthings.googleassistant.AssistantHelper
import com.nilhcem.kidsroom.R
import com.nilhcem.kidsroom.data.RfidDevice
import com.nilhcem.kidsroom.device.components.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName!!
    }

    private val ttsSpeakerListener = object : TtsSpeaker.Listener {
        override fun onTtsInitialized(speaker: TtsSpeaker) {
            Log.i(TAG, "onTtsInitialized")
            speaker.say(getString(R.string.ready_to_play))
        }

        override fun onTtsSpoken(speaker: TtsSpeaker) {
        }
    }

    private val assistant by lazy { AssistantHelper(this) }
    private val lightbulb by lazy { MagicBlueRgbBulbBle(applicationContext) }
    private val ttsSpeaker by lazy { TtsSpeaker(this, ttsSpeakerListener) }
    private val chineseColors by lazy { ChineseColorsSpeaker(this) }
    private val musicPlayer by lazy { MusicPlayer(this) }
    private val relayFan by lazy { RelayFan() }
    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        lifecycle.addObservers(lightbulb, ttsSpeaker, assistant, chineseColors, musicPlayer, relayFan)

        viewModel.rc522LiveData.observe({ lifecycle }) { uid ->
            Log.i(TAG, "Uid=$uid")

            val device = RfidDevice.getFromRfidId(uid!!)
            device?.let {
                ttsSpeaker.say(getString(device.actionStringResId))
            }
        }

        viewModel.buttonsLiveData.observe({ lifecycle }) { buttonData ->
            Log.i(TAG, "Button pressed: $buttonData")
            val button = buttonData!!.first
            val isPressed = buttonData.second

            viewModel.rc522LiveData.value?.let { rfid ->
                val rfidDevice = RfidDevice.getFromRfidId(rfid)
                if (rfidDevice != RfidDevice.BLAISE && !isPressed) {
                    return@observe
                }

                when (rfidDevice) {
                    RfidDevice.TOTAKEKE -> {
                        // Music player
                        musicPlayer.onButtonPressed(button)
                    }
                    RfidDevice.CRUZ -> {
                        // Control the fan
                        relayFan.onButtonPressed(button)
                    }
                    RfidDevice.FLASH -> {
                        // Learn colors
                        chineseColors.onButtonPressed(button)
                    }
                    RfidDevice.SPLATOON -> {
                        // Lights color
                        lightbulb.onButtonPressed(button)
                    }
                    RfidDevice.BLAISE -> {
                        // Google Assistant
                        assistant.onButtonPressed(isPressed)
                    }
                    RfidDevice.KEYRING -> {
                        // Parents Mode
                        // This could be where you setup your new rfid tags to custom actions
                    }
                }
            }
        }
    }

    private fun Lifecycle.addObservers(vararg observers: LifecycleObserver) {
        observers.forEach {
            addObserver(it)
        }
    }
}
