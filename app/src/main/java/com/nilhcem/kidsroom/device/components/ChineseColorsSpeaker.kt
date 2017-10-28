package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.nilhcem.kidsroom.R
import com.nilhcem.kidsroom.data.Button

class ChineseColorsSpeaker(private val context: Context) : LifecycleObserver {

    companion object {
        private val TAG = ChineseColorsSpeaker::class.java.simpleName
    }

    private val Button.chineseColor: Int
        get() = when (this) {
            Button.RED -> R.raw.zh_red
            Button.GREEN -> R.raw.zh_green
            Button.BLUE -> R.raw.zh_blue
            Button.YELLOW -> R.raw.zh_yellow
            Button.WHITE -> R.raw.zh_white
            Button.BLACK -> R.raw.zh_black
        }

    private var soundPool: SoundPool? = null
    private var loaded: Boolean = false
    private val sounds = mutableMapOf<Button, Int>()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()

        soundPool!!.setOnLoadCompleteListener { _, _, _ -> loaded = true }

        sounds.clear()
        sounds.putAll(Button.values().map { it to soundPool!!.load(context, it.chineseColor, 1) }.toMap())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        soundPool!!.release()
        soundPool = null
    }

    fun onButtonPressed(button: Button) {
        if (loaded) {
            soundPool!!.play(sounds.getValue(button), 0.5f, 0.5f, 1, 0, 1f)
        } else {
            Log.w(TAG, "Soundpool is not fully initialized yet")
        }
    }
}
