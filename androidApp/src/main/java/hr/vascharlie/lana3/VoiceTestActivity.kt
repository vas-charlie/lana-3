package hr.vascharlie.lana3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import hr.vascharlie.lana3.avatar.AvatarController
import hr.vascharlie.lana3.core.ride.LabeledRideOfferTranscriptNormalizer
import hr.vascharlie.lana3.core.ride.RideOfferTranscriptNormalizer
import hr.vascharlie.lana3.core.ride.RideOfferTranscriptResult
import hr.vascharlie.lana3.ports.SpeechInputEvent
import hr.vascharlie.lana3.ports.SpeechInputPort
import hr.vascharlie.lana3.ports.SpeechInputRequest
import hr.vascharlie.lana3.ports.SpeechOutputEvent
import hr.vascharlie.lana3.ports.SpeechOutputPort
import hr.vascharlie.lana3.ports.SpeechOutputRequest
import java.util.Locale

class VoiceTestActivity : Activity() {
    companion object {
        private const val REQUEST_MICROPHONE = 3201
    }

    private lateinit var speechInput: SpeechInputPort
    private lateinit var speechOutput: SpeechOutputPort
    private val rideTranscriptNormalizer: RideOfferTranscriptNormalizer =
        LabeledRideOfferTranscriptNormalizer()
    private lateinit var inputAdapter: AndroidSpeechInputAdapter
    private lateinit var outputAdapter: AndroidSpeechOutputAdapter

    private lateinit var languageTag: EditText
    private lateinit var transcript: EditText
    private lateinit var status: TextView
    private lateinit var listenButton: Button
    private val avatarController = AvatarController()
    private lateinit var avatarView: LanaConversationAvatarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputAdapter = AndroidSpeechInputAdapter(this)
        outputAdapter = AndroidSpeechOutputAdapter(this)
        speechInput = inputAdapter
        speechOutput = outputAdapter

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 44, 36, 44)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = getString(R.string.voice_lab_title)
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.voice_lab_description)
            textSize = 14f
            setPadding(0, 10, 0, 22)
            setTextColor(Color.rgb(180, 195, 210))
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.voice_language_label)
            textSize = 14f
            setTextColor(Color.rgb(200, 210, 220))
        })

        languageTag = EditText(this).apply {
            setText(Locale.getDefault().toLanguageTag())
            hint = getString(R.string.voice_language_hint)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 135, 150))
        }
        root.addView(languageTag)

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 18, 0, 12)
            setTextColor(Color.rgb(90, 180, 255))
        }
        root.addView(status)

        transcript = EditText(this).apply {
            hint = getString(R.string.voice_transcript_hint)
            minLines = 4
            gravity = Gravity.TOP
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(120, 135, 150))
        }
        root.addView(
            transcript,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        )

        listenButton = Button(this).apply {
            text = getString(R.string.voice_start_listening)
            isAllCaps = false
            setOnClickListener { startListening() }
        }
        root.addView(listenButton)

        root.addView(Button(this).apply {
            text = getString(R.string.voice_stop_listening)
            isAllCaps = false
            setOnClickListener {
                speechInput.stop()
                status.text = getString(R.string.voice_stopping_listen)
            }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.voice_speak_text)
            isAllCaps = false
            setOnClickListener { speakTranscript() }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.voice_stop_speaking)
            isAllCaps = false
            setOnClickListener {
                speechOutput.stop()
                avatarController.onLanaSpeechEnded()
                avatarView.render(avatarController.frame)
                status.text = getString(R.string.voice_speaking_stopped)
            }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.voice_to_note)
            isAllCaps = false
            setOnClickListener { handOffToNotes() }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.voice_to_smart_ride)
            isAllCaps = false
            setOnClickListener { handOffToSmartRide() }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.back)
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(scroll)
        refreshAvailability()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) {
            refreshAvailability()
        }
    }

    override fun onDestroy() {
        inputAdapter.release()
        outputAdapter.release()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_MICROPHONE) {
            val granted =
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

            if (granted) {
                startListening()
            } else {
                status.text = getString(R.string.voice_microphone_denied)
            }
        }
    }

    private fun refreshAvailability() {
        val stt = if (speechInput.isAvailable) {
            getString(R.string.voice_stt_available)
        } else {
            getString(R.string.voice_stt_unavailable)
        }
        val tts = if (speechOutput.isAvailable) {
            getString(R.string.voice_tts_ready)
        } else {
            getString(R.string.voice_tts_unavailable)
        }
        status.text = getString(R.string.voice_availability_status, stt, tts)
        listenButton.isEnabled = speechInput.isAvailable
    }

    private fun startListening() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_MICROPHONE,
            )
            return
        }

        if (!speechInput.isAvailable) {
            status.text = getString(R.string.voice_no_stt_service)
            return
        }

        val requestedLanguage = languageTag
            .text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        speechInput.start(
            SpeechInputRequest(
                languageTag = requestedLanguage,
                enablePartialResults = true,
            )
        ) { event ->
            runOnUiThread {
                when (event) {
                    SpeechInputEvent.ListeningStarted -> {
                        avatarController.onUserSpeechStarted()
                        avatarView.render(avatarController.frame)
                        status.text = getString(R.string.voice_listening)
                    }

                    is SpeechInputEvent.PartialTranscript -> {
                        transcript.setText(event.text)
                        transcript.setSelection(transcript.text.length)
                        status.text = getString(R.string.voice_recognizing)
                    }

                    is SpeechInputEvent.FinalTranscript -> {
                        avatarController.onUserSpeechEnded()
                        avatarView.render(avatarController.frame)
                        transcript.setText(event.text)
                        transcript.setSelection(transcript.text.length)
                        status.text = getString(R.string.voice_recognized)
                    }

                    is SpeechInputEvent.Error -> {
                        avatarController.onError()
                        avatarView.render(avatarController.frame)
                        status.text = getString(
                            R.string.voice_stt_error,
                            localizedSpeechInputError(event.code),
                            if (event.retryable) {
                                getString(R.string.voice_retry_suffix)
                            } else {
                                ""
                            },
                        )
                    }
                }
            }
        }
    }

    private fun localizedSpeechInputError(code: String): String = when (code) {
        "recognizer_unavailable" ->
            getString(R.string.voice_stt_error_recognizer_unavailable)

        "empty_result" ->
            getString(R.string.voice_stt_error_empty_result)

        "audio" ->
            getString(R.string.voice_stt_error_audio)

        "client" ->
            getString(R.string.voice_stt_error_client)

        "permission" ->
            getString(R.string.voice_stt_error_permission)

        "network" ->
            getString(R.string.voice_stt_error_network)

        "network_timeout" ->
            getString(R.string.voice_stt_error_network_timeout)

        "no_match" ->
            getString(R.string.voice_stt_error_no_match)

        "busy" ->
            getString(R.string.voice_stt_error_busy)

        "server" ->
            getString(R.string.voice_stt_error_server)

        "speech_timeout" ->
            getString(R.string.voice_stt_error_speech_timeout)

        else ->
            getString(R.string.voice_stt_error_generic)
    }

    private fun localizedSpeechOutputError(code: String): String = when {
        code == "tts_init_failed" ->
            getString(R.string.voice_tts_error_unavailable)

        code == "tts_not_ready" ->
            getString(R.string.voice_tts_error_initializing)

        code == "empty_text" ->
            getString(R.string.voice_tts_error_empty_text)

        code == "language_not_supported" ->
            getString(R.string.voice_tts_error_language)

        code == "tts_start_failed" ->
            getString(R.string.voice_tts_error_start_failed)

        code == "tts_error" || code.startsWith("tts_error_") ->
            getString(R.string.voice_tts_error_failed)

        else ->
            getString(R.string.voice_tts_error_failed)
    }

    private fun handOffToNotes() {
        val text = transcript
            .text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (text.isBlank()) {
            status.text = getString(R.string.voice_no_text_for_note)
            return
        }

        startActivity(
            Intent(this, NotesTestActivity::class.java).apply {
                putExtra(NotesTestActivity.EXTRA_PREFILL_NOTE_TEXT, text)
            }
        )
        status.text = getString(R.string.voice_sent_to_notes)
    }

    private fun handOffToSmartRide() {
        val text = transcript
            .text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (text.isBlank()) {
            status.text = getString(R.string.voice_no_text_for_smart_ride)
            return
        }

        val requestedLanguage = languageTag
            .text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: Locale.getDefault().toLanguageTag()

        when (
            val parsed = rideTranscriptNormalizer.normalize(
                transcript = text,
                languageTag = requestedLanguage,
            )
        ) {
            is RideOfferTranscriptResult.UnsupportedLanguage -> {
                status.text = getString(
                    R.string.voice_smart_ride_unsupported_language,
                    parsed.languageTag ?: requestedLanguage,
                )
            }

            RideOfferTranscriptResult.NoRecognizedData -> {
                status.text = getString(
                    R.string.voice_smart_ride_no_recognized_data,
                )
            }

            is RideOfferTranscriptResult.Parsed -> {
                val smartRideIntent =
                    Intent(this, SmartRideTestActivity::class.java)

                SmartRidePrefillContract.write(
                    intent = smartRideIntent,
                    offer = parsed.offer,
                    source = SmartRidePrefillContract.SOURCE_VOICE,
                )

                startActivity(smartRideIntent)
                status.text = getString(
                    R.string.voice_sent_to_smart_ride,
                    parsed.recognizedFields.size,
                )
            }
        }
    }

    private fun speakTranscript() {
        val text = transcript
            .text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (text.isBlank()) {
            status.text = getString(R.string.voice_no_text_to_speak)
            return
        }

        val requestedLanguage = languageTag
            .text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        speechOutput.speak(
            SpeechOutputRequest(
                text = text,
                languageTag = requestedLanguage,
            )
        ) { event ->
            runOnUiThread {
                status.text = when (event) {
                    SpeechOutputEvent.Started -> {
                        avatarController.onLanaSpeechStarted()
                        avatarController.onLanaSpeechLevel(0.65f)
                        avatarView.render(avatarController.frame)
                        getString(R.string.voice_tts_started)
                    }
                    SpeechOutputEvent.Completed -> {
                        avatarController.onLanaSpeechEnded()
                        avatarView.render(avatarController.frame)
                        getString(R.string.voice_tts_completed)
                    }
                    is SpeechOutputEvent.Error -> {
                        avatarController.onError()
                        avatarView.render(avatarController.frame)
                        getString(
                            R.string.voice_tts_error,
                            localizedSpeechOutputError(event.code),
                        )
                    }
                }
            }
        }
    }
}
