package com.nilhcem.kidsroom.device.components

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nilhcem.kidsroom.BuildConfig
import java.util.*

class TtsSpeaker(private val context: Context, private val listener: Listener? = null, private val locale: Locale = Locale.US) :
        LifecycleObserver, TextToSpeech.OnInitListener {

    companion object {
        private val TAG = TtsSpeaker::class.java.simpleName!!
        private const val UTTERANCE_ID = BuildConfig.APPLICATION_ID + ".UTTERANCE_ID"
    }

    interface Listener {
        fun onTtsInitialized()
        fun onTtsSpoken()
    }

    private var isInitialized = false
    private var ttsEngine: TextToSpeech? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        ttsEngine = TextToSpeech(context, this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        ttsEngine?.let {
            it.stop()
            it.shutdown()
        }.also {
            ttsEngine = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsEngine!!.language = locale
            ttsEngine!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    Log.i(TAG, "onStart")
                }

                override fun onDone(utteranceId: String) {
                    Log.i(TAG, "onDone")
                    listener?.onTtsSpoken()
                }

                override fun onError(utteranceId: String, errorCode: Int) {
                    Log.w(TAG, "onError ($utteranceId). Error code: $errorCode")
                }

                override fun onError(utteranceId: String) {
                    Log.w(TAG, "onError")
                }
            })

            ttsEngine!!.setPitch(1f)
            ttsEngine!!.setSpeechRate(1f)

            isInitialized = true
            Log.i(TAG, "TTS initialized successfully")
            listener?.onTtsInitialized()
        } else {
            Log.w(TAG, "Could not open TTS Engine (onInit status=$status). Ignoring text to speech")
            ttsEngine = null
        }
    }

    fun say(message: String) {
        if (!isInitialized || ttsEngine == null) {
            Log.w(TAG, "TTS is not initialized yet, be patient")
            return
        }

        ttsEngine!!.speak(message, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID)
    }
}
