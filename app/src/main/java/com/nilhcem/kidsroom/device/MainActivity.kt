package com.nilhcem.kidsroom.device

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.nilhcem.kidsroom.device.components.MagicBlueRgbBulbBle
import com.nilhcem.kidsroom.device.components.TtsSpeaker

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName!!
    }

    private val lightbulb by lazy { MagicBlueRgbBulbBle(applicationContext) }
    private val ttsSpeaker by lazy { TtsSpeaker(this) }
    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        lifecycle.addObserver(lightbulb)
        lifecycle.addObserver(ttsSpeaker)

        viewModel.rc522LiveData.observe({ lifecycle }) { uid ->
            Log.i(TAG, "Uid=$uid")
        }

        viewModel.buttonsLiveData.observe({ lifecycle }) { button ->
            Log.i(TAG, "Button pressed: $button")
            Log.i(TAG, "Last value: ${viewModel.rc522LiveData.value}")
            ttsSpeaker.say("Button pressed: $button")
            lightbulb.onButtonPressed(button!!)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
    }
}
