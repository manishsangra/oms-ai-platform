
package com.manish.springaiassistant

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechManager(
    context: Context,
    private val onInitialized: (Boolean) -> Unit
) {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->

            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("en", "IN"))

                isReady =
                    result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED

                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.setPitch(1.0f)

                onInitialized(isReady)
            } else {
                isReady = false
                onInitialized(false)
            }
        }
    }

    fun speak(text: String) {
        if (!isReady || text.isBlank()) return

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MANISH_AI_RESPONSE"
        )
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
