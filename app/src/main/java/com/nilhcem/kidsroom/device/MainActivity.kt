package com.nilhcem.kidsroom.device

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.nilhcem.assistant.androidthings.googleassistant.AssistantHelper
import com.nilhcem.kidsroom.R
import com.nilhcem.kidsroom.data.RfidDevice
import com.nilhcem.kidsroom.device.components.MagicBlueRgbBulbBle
import com.nilhcem.kidsroom.device.components.TtsSpeaker

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
    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        lifecycle.addObserver(lightbulb)
        lifecycle.addObserver(ttsSpeaker)
        lifecycle.addObserver(assistant)

        viewModel.rc522LiveData.observe({ lifecycle }) { uid ->
            Log.i(TAG, "Uid=$uid")

            val device = RfidDevice.getFromRfidId(uid!!)
            device?.let {
                ttsSpeaker.say(getString(device.actionStringResId))
            }
        }

        viewModel.buttonsLiveData.observe({ lifecycle }) { button ->
            Log.i(TAG, "Button pressed: $button")

            viewModel.rc522LiveData.value?.let { rfid ->
                val rfidDevice = RfidDevice.getFromRfidId(rfid)
                if (rfidDevice != RfidDevice.BLAISE && !button!!.second) {
                    return@observe
                }

                when (rfidDevice) {
                    RfidDevice.TOTAKEKE -> {
                        // Nursery rhymes
                    }
                    RfidDevice.CRUZ -> {
                        // Control the fan
                    }
                    RfidDevice.FLASH -> {
                        // Learn colors
                    }
                    RfidDevice.SPLATOON -> {
                        // Lights color
                        lightbulb.onButtonPressed(button!!.first)
                    }
                    RfidDevice.BLAISE -> {
                        // Google Assistant
                        assistant.onButtonPressed(button!!.second)
                    }
                    RfidDevice.KEYRING -> {
                        // Parents Mode
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
    }
}
