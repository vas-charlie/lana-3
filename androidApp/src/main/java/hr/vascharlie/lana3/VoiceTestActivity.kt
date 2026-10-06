package hr.vascharlie.lana3

import android.Manifest
import android.app.Activity
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
    private lateinit var inputAdapter: AndroidSpeechInputAdapter
    private lateinit var outputAdapter: AndroidSpeechOutputAdapter

    private lateinit var languageTag: EditText
    private lateinit var transcript: EditText
    private lateinit var status: TextView
    private lateinit var listenButton: Button

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
            text = "LANA Voice Lab"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text =
                "Test Android govora kroz zajedničke LANA portove. " +
                    "Jezik nije fiksiran na hrvatski i može se promijeniti BCP-47 oznakom."
            textSize = 14f
            setPadding(0, 10, 0, 22)
            setTextColor(Color.rgb(180, 195, 210))
        })

        root.addView(TextView(this).apply {
            text = "Jezik govora"
            textSize = 14f
            setTextColor(Color.rgb(200, 210, 220))
        })

        languageTag = EditText(this).apply {
            setText(Locale.getDefault().toLanguageTag())
            hint = "npr. hr-HR, en-US, de-DE"
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
            hint = "Ovdje će se pojaviti prepoznati govor..."
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
            text = "Počni slušati"
            isAllCaps = false
            setOnClickListener { startListening() }
        }
        root.addView(listenButton)

        root.addView(Button(this).apply {
            text = "Zaustavi slušanje"
            isAllCaps = false
            setOnClickListener {
                speechInput.stop()
                status.text = "Zaustavljam slušanje..."
            }
        })

        root.addView(Button(this).apply {
            text = "LANA izgovori tekst"
            isAllCaps = false
            setOnClickListener { speakTranscript() }
        })

        root.addView(Button(this).apply {
            text = "Zaustavi govor"
            isAllCaps = false
            setOnClickListener {
                speechOutput.stop()
                status.text = "Govor zaustavljen."
            }
        })

        root.addView(Button(this).apply {
            text = "Natrag"
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
                status.text = "Mikrofon nije dopušten. LANA neće pokušavati slušati."
            }
        }
    }

    private fun refreshAvailability() {
        val stt = if (speechInput.isAvailable) "STT dostupan" else "STT nije dostupan"
        val tts = if (speechOutput.isAvailable) "TTS spreman" else "TTS se priprema ili nije dostupan"
        status.text = stt + " • " + tts
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
            status.text = "Na ovom uređaju nema dostupne Android STT usluge."
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
                        status.text = "Slušam..."
                    }

                    is SpeechInputEvent.PartialTranscript -> {
                        transcript.setText(event.text)
                        transcript.setSelection(transcript.text.length)
                        status.text = "Prepoznajem govor..."
                    }

                    is SpeechInputEvent.FinalTranscript -> {
                        transcript.setText(event.text)
                        transcript.setSelection(transcript.text.length)
                        status.text = "Govor prepoznat."
                    }

                    is SpeechInputEvent.Error -> {
                        status.text =
                            "STT: " + event.message +
                                if (event.retryable) " Možeš pokušati ponovno." else ""
                    }
                }
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
            status.text = "Nema teksta koji bi LANA izgovorila."
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
                    SpeechOutputEvent.Started -> "LANA govori..."
                    SpeechOutputEvent.Completed -> "Govor završen."
                    is SpeechOutputEvent.Error -> "TTS: " + event.message
                }
            }
        }
    }
}
