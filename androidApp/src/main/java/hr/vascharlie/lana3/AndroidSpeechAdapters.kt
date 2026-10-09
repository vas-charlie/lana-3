package hr.vascharlie.lana3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import hr.vascharlie.lana3.ports.SpeechInputEvent
import hr.vascharlie.lana3.ports.SpeechInputPort
import hr.vascharlie.lana3.ports.SpeechInputRequest
import hr.vascharlie.lana3.ports.SpeechOutputEvent
import hr.vascharlie.lana3.ports.SpeechOutputPort
import hr.vascharlie.lana3.ports.SpeechOutputRequest
import java.util.Locale
import java.util.UUID

class AndroidSpeechInputAdapter(
    context: Context,
) : SpeechInputPort {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null
    private var eventCallback: ((SpeechInputEvent) -> Unit)? = null
    private var activeLanguageTag: String? = null

    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(appContext)

    override fun start(
        request: SpeechInputRequest,
        onEvent: (SpeechInputEvent) -> Unit,
    ) {
        mainHandler.post {
            if (!isAvailable) {
                onEvent(
                    SpeechInputEvent.Error(
                        code = "recognizer_unavailable",
                        message = "No Android speech recognition service is available.",
                        retryable = false,
                    )
                )
                return@post
            }

            cancelInternal()
            eventCallback = onEvent
            activeLanguageTag = request.languageTag

            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            recognizer = speechRecognizer
            speechRecognizer.setRecognitionListener(listener)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    request.enablePartialResults,
                )

                request.languageTag
                    ?.takeIf { it.isNotBlank() }
                    ?.let { putExtra(RecognizerIntent.EXTRA_LANGUAGE, it) }
            }

            speechRecognizer.startListening(intent)
            emit(SpeechInputEvent.ListeningStarted)
        }
    }

    override fun stop() {
        mainHandler.post {
            recognizer?.stopListening()
        }
    }

    override fun cancel() {
        mainHandler.post {
            cancelInternal()
        }
    }

    fun release() {
        mainHandler.post {
            cancelInternal()
            eventCallback = null
        }
    }

    private fun cancelInternal() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        activeLanguageTag = null
    }

    private fun emit(event: SpeechInputEvent) {
        eventCallback?.invoke(event)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {\n            emit(SpeechInputEvent.SpeechEnded)\n        }

        override fun onError(error: Int) {
            emit(
                SpeechInputEvent.Error(
                    code = errorCode(error),
                    message = errorMessage(error),
                    retryable = isRetryable(error),
                )
            )
            recognizer?.destroy()
            recognizer = null
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()

            if (text.isBlank()) {
                emit(
                    SpeechInputEvent.Error(
                        code = "empty_result",
                        message = "Speech recognition returned no transcript.",
                        retryable = true,
                    )
                )
            } else {
                emit(
                    SpeechInputEvent.FinalTranscript(
                        text = text,
                        languageTag = activeLanguageTag,
                    )
                )
            }

            recognizer?.destroy()
            recognizer = null
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()

            if (text.isNotBlank()) {
                emit(
                    SpeechInputEvent.PartialTranscript(
                        text = text,
                        languageTag = activeLanguageTag,
                    )
                )
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun errorCode(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "audio"
        SpeechRecognizer.ERROR_CLIENT -> "client"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "permission"
        SpeechRecognizer.ERROR_NETWORK -> "network"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "network_timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "no_match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "busy"
        SpeechRecognizer.ERROR_SERVER -> "server"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "speech_timeout"
        else -> "recognizer_error_$error"
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio capture failed."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was interrupted."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission is not granted."
        SpeechRecognizer.ERROR_NETWORK -> "Speech recognition network error."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout."
        SpeechRecognizer.ERROR_NO_MATCH -> "Speech was heard, but no reliable match was found."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
        SpeechRecognizer.ERROR_SERVER -> "Speech recognition service error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected."
        else -> "Speech recognition failed."
    }

    private fun isRetryable(error: Int): Boolean = when (error) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> false
        SpeechRecognizer.ERROR_CLIENT -> false
        else -> true
    }
}

class AndroidSpeechOutputAdapter(
    context: Context,
) : SpeechOutputPort {
    private val mainHandler = Handler(Looper.getMainLooper())

    private var initialized = false
    private var initFailed = false
    private var currentCallback: ((SpeechOutputEvent) -> Unit)? = null

    private val tts = TextToSpeech(context.applicationContext) { status ->
        initialized = status == TextToSpeech.SUCCESS
        initFailed = status != TextToSpeech.SUCCESS
    }.apply {
        setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    emit(SpeechOutputEvent.Started)
                }

                override fun onDone(utteranceId: String?) {
                    emit(SpeechOutputEvent.Completed)
                }

                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int,
                ) {
                    emit(SpeechOutputEvent.RangeStarted(start = start, end = end))
                }

                @Deprecated("Deprecated in Android API")
                override fun onError(utteranceId: String?) {
                    emit(
                        SpeechOutputEvent.Error(
                            code = "tts_error",
                            message = "Text-to-speech failed.",
                        )
                    )
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    emit(
                        SpeechOutputEvent.Error(
                            code = "tts_error_$errorCode",
                            message = "Text-to-speech failed.",
                        )
                    )
                }
            }
        )
    }

    override val isAvailable: Boolean
        get() = initialized && !initFailed

    override fun speak(
        request: SpeechOutputRequest,
        onEvent: (SpeechOutputEvent) -> Unit,
    ) {
        mainHandler.post {
            currentCallback = onEvent

            if (!isAvailable) {
                emit(
                    SpeechOutputEvent.Error(
                        code = if (initFailed) "tts_init_failed" else "tts_not_ready",
                        message =
                            if (initFailed) {
                                "Text-to-speech is unavailable."
                            } else {
                                "Text-to-speech is still initializing."
                            },
                    )
                )
                return@post
            }

            if (request.text.isBlank()) {
                emit(
                    SpeechOutputEvent.Error(
                        code = "empty_text",
                        message = "There is no text to speak.",
                    )
                )
                return@post
            }

            val locale = request.languageTag
                ?.takeIf { it.isNotBlank() }
                ?.let(Locale::forLanguageTag)
                ?: Locale.getDefault()

            when (tts.setLanguage(locale)) {
                TextToSpeech.LANG_MISSING_DATA,
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    emit(
                        SpeechOutputEvent.Error(
                            code = "language_not_supported",
                            message = "The selected TTS language is not supported on this device.",
                        )
                    )
                    return@post
                }
            }

            val utteranceId = "lana-" + UUID.randomUUID().toString()
            val result = tts.speak(
                request.text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId,
            )

            if (result == TextToSpeech.ERROR) {
                emit(
                    SpeechOutputEvent.Error(
                        code = "tts_start_failed",
                        message = "Text-to-speech could not start.",
                    )
                )
            }
        }
    }

    override fun stop() {
        mainHandler.post {
            tts.stop()
        }
    }

    fun release() {
        mainHandler.post {
            tts.stop()
            tts.shutdown()
            currentCallback = null
        }
    }

    private fun emit(event: SpeechOutputEvent) {
        mainHandler.post {
            currentCallback?.invoke(event)
        }
    }
}
