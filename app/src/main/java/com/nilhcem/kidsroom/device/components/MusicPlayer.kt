package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.media.MediaPlayer
import com.nilhcem.kidsroom.R
import com.nilhcem.kidsroom.data.Button

class MusicPlayer(private val context: Context) : LifecycleObserver {

    private val Button.musicResId: Int
        get() = when (this) {
            Button.RED -> R.raw.bensound_buddy
            Button.GREEN -> R.raw.bensound_happyrock
            Button.BLUE -> R.raw.bensound_happiness
            Button.YELLOW -> R.raw.bensound_ukulele
            Button.WHITE -> R.raw.bensound_littleidea
            Button.BLACK -> 0
        }

    private var mediaPlayer: MediaPlayer? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        mediaPlayer?.release().also { mediaPlayer = null }
    }

    fun onButtonPressed(button: Button) {
        mediaPlayer?.release()
        val musicResId = button.musicResId
        if (musicResId != 0) {
            mediaPlayer = MediaPlayer.create(context, musicResId).apply {
                start()
            }
        }
    }
}
