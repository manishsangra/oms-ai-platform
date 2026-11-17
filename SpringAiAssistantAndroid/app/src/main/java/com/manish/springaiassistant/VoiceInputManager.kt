package com.manish.springaiassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceInputManager(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onVoiceError: (String) -> Unit,
    private val onListeningChanged: (Boolean) -> Unit
) {

    private val appContext = context.applicationContext

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {

            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(appContext)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        onListeningChanged(true)
                    }

                    override fun onBeginningOfSpeech() {
                        onListeningChanged(true)
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        onListeningChanged(false)
                        onVoiceError(getErrorMessage(error))
                    }

                    override fun onResults(results: Bundle?) {
                        onListeningChanged(false)

                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                        val recognizedText = matches?.firstOrNull()

                        if (!recognizedText.isNullOrBlank()) {
                            onResult(recognizedText)
                        } else {
                            onVoiceError(
                                "No speech detected. Please try again."
                            )
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )
        }
    }

    fun startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onVoiceError(
                "Speech recognition is not available on this device."
            )
            return
        }

        if (speechRecognizer == null) {
            onVoiceError("Unable to initialize speech recognition.")
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale("en", "IN")
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en-IN"
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
            )
        }

        try {
            onListeningChanged(true)
            speechRecognizer?.startListening(intent)

        } catch (exception: Exception) {
            onListeningChanged(false)

            onVoiceError(
                exception.localizedMessage
                    ?: "Unable to start voice recognition."
            )
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        onListeningChanged(false)
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun getErrorMessage(error: Int): String {

        return when (error) {

            SpeechRecognizer.ERROR_AUDIO ->
                "Audio recording error."

            SpeechRecognizer.ERROR_CLIENT ->
                "Speech recognition client error."

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is required."

            SpeechRecognizer.ERROR_NETWORK ->
                "Network error during voice recognition."

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Voice recognition network timeout."

            SpeechRecognizer.ERROR_NO_MATCH ->
                "Sorry, I couldn't understand. Please try again."

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Speech recognizer is busy. Please try again."

            SpeechRecognizer.ERROR_SERVER ->
                "Speech recognition server error."

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "No speech detected."

            else ->
                "Voice recognition failed. Please try again."
        }
    }
}