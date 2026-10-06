package hr.vascharlie.lana3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import hr.vascharlie.lana3.core.model.CapabilityGate
import hr.vascharlie.lana3.core.model.CapabilityGateResult
import hr.vascharlie.lana3.core.model.CapabilityIds
import hr.vascharlie.lana3.core.model.CapabilityRequirement

enum class LanaVisualState { IDLE, LISTENING, THINKING, SPEAKING, OFFLINE, ERROR }

class MainActivity : Activity() {
    companion object {
        private const val REQUEST_LANA_PERMISSIONS = 3101
        private const val SETUP_PREFS = "lana_device_setup"
        private const val KEY_INITIAL_PERMISSION_REQUESTED = "initial_permission_requested"
    }

    private lateinit var avatar: TextView
    private lateinit var stateLabel: TextView
    private lateinit var status: TextView
    private lateinit var updateStatus: TextView
    private lateinit var readinessStatus: TextView
    private lateinit var readinessButton: Button
    private lateinit var autoUpdater: AutoUpdater
    private var visualState = LanaVisualState.IDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 56, 40, 40)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }

        root.addView(TextView(this).apply {
            text = "LANA 3"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })

        root.addView(TextView(this).apply {
            text = "Developer Preview ${BuildConfig.VERSION_NAME}"
            textSize = 14f
            setTextColor(Color.rgb(90, 180, 255))
        })

        updateStatus = TextView(this).apply {
            text = "Provjeravam ima li nove verzije..."
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
            setTextColor(Color.rgb(150, 165, 180))
        }
        root.addView(updateStatus)

        readinessStatus = TextView(this).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 14, 0, 8)
            setTextColor(Color.rgb(180, 195, 210))
        }
        root.addView(readinessStatus)

        readinessButton = Button(this).apply {
            isAllCaps = false
            setOnClickListener { requestMissingLanaPermissions() }
        }
        root.addView(readinessButton)

        val avatarStage = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(10, 27, 48))
        }
        avatar = TextView(this).apply {
            text = "LANA"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(15, 39, 67))
            contentDescription = "Lana visual presence"
        }
        avatarStage.addView(
            avatar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply { setMargins(22, 22, 22, 22) }
        )
        root.addView(
            avatarStage,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply { setMargins(0, 24, 0, 20) }
        )

        stateLabel = TextView(this).apply {
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 180, 255))
        }
        root.addView(stateLabel)

        status = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 18)
        }
        root.addView(status)

        val stateButton = Button(this).apply {
            text = "Promijeni stanje Lane"
            isAllCaps = false
            setOnClickListener { cycleVisualState() }
        }
        root.addView(stateButton)

        root.addView(Button(this).apply {
            text = "Smart Ride Acceptance - TESTNO"
            isAllCaps = false
            setOnClickListener {
                status.text =
                    "Smart Ride Acceptance je u jezgri. Sljedece ga spajamo na ovaj ekran."
            }
        })

        setContentView(root)
        renderState(LanaVisualState.IDLE)
        refreshDeviceReadiness()
        maybeRequestInitialPermissions()

        autoUpdater = AutoUpdater(this) { message ->
            updateStatus.text = message
        }
        autoUpdater.start()
    }

    override fun onResume() {
        super.onResume()
        if (::readinessStatus.isInitialized) {
            refreshDeviceReadiness()
        }
        if (::autoUpdater.isInitialized) {
            autoUpdater.onResume()
        }
    }

    override fun onDestroy() {
        if (::autoUpdater.isInitialized) {
            autoUpdater.stop()
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LANA_PERMISSIONS) {
            refreshDeviceReadiness()
        }
    }

    private fun maybeRequestInitialPermissions() {
        val prefs = getSharedPreferences(SETUP_PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_INITIAL_PERMISSION_REQUESTED, false)) return

        val permissions = AndroidDeviceReadinessProbe.permissionsToRequest(this)
        prefs.edit().putBoolean(KEY_INITIAL_PERMISSION_REQUESTED, true).apply()

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions, REQUEST_LANA_PERMISSIONS)
        }
    }

    private fun requestMissingLanaPermissions() {
        val permissions = AndroidDeviceReadinessProbe.permissionsToRequest(this)
        if (permissions.isEmpty()) {
            refreshDeviceReadiness()
            return
        }

        requestPermissions(permissions, REQUEST_LANA_PERMISSIONS)
    }

    private fun refreshDeviceReadiness() {
        val snapshot = AndroidDeviceReadinessProbe.snapshot(this)
        val permissions = AndroidDeviceReadinessProbe.permissionsToRequest(this)
        val coreSnapshot = AndroidCapabilityBridge.toCoreSnapshot(snapshot)
        val gateResult = CapabilityGate().evaluate(
            coreSnapshot,
            listOf(
                CapabilityRequirement(CapabilityIds.CAMERA),
                CapabilityRequirement(CapabilityIds.MICROPHONE),
                CapabilityRequirement(CapabilityIds.LOCATION, allowDegraded = true),
            )
        )

        val coreStatus = when (gateResult) {
            CapabilityGateResult.Ready ->
                "Jezgra senzora: spremna."

            is CapabilityGateResult.Blocked ->
                "Jezgra senzora ceka: " +
                    gateResult.failures.joinToString { it.capabilityId }
        }

        readinessStatus.text = snapshot.toDisplayText() + "\n" + coreStatus
        readinessButton.isEnabled = permissions.isNotEmpty()
        readinessButton.text =
            if (permissions.isEmpty()) {
                "Dozvole za osnovne senzore su spremne"
            } else {
                "Dopusti potrebne senzore (${permissions.size})"
            }
    }

    private fun cycleVisualState() {
        val next = when (visualState) {
            LanaVisualState.IDLE -> LanaVisualState.LISTENING
            LanaVisualState.LISTENING -> LanaVisualState.THINKING
            LanaVisualState.THINKING -> LanaVisualState.SPEAKING
            LanaVisualState.SPEAKING -> LanaVisualState.OFFLINE
            LanaVisualState.OFFLINE -> LanaVisualState.ERROR
            LanaVisualState.ERROR -> LanaVisualState.IDLE
        }
        renderState(next)
    }

    private fun renderState(state: LanaVisualState) {
        visualState = state
        avatar.alpha = 1f
        avatar.scaleX = 1f
        avatar.scaleY = 1f

        when (state) {
            LanaVisualState.IDLE -> {
                stateLabel.text = "IDLE"
                status.text = "Tu sam, Charlie."
                avatar.setBackgroundColor(Color.rgb(15, 39, 67))
            }

            LanaVisualState.LISTENING -> {
                stateLabel.text = "LISTENING"
                status.text = "Slusam."
                avatar.setBackgroundColor(Color.rgb(12, 55, 82))
                avatar.scaleX = 1.025f
                avatar.scaleY = 1.025f
            }

            LanaVisualState.THINKING -> {
                stateLabel.text = "THINKING"
                status.text = "Razmisljam..."
                avatar.setBackgroundColor(Color.rgb(31, 43, 72))
                avatar.alpha = 0.88f
            }

            LanaVisualState.SPEAKING -> {
                stateLabel.text = "SPEAKING"
                status.text = "Govorim."
                avatar.setBackgroundColor(Color.rgb(18, 65, 77))
                avatar.scaleX = 1.035f
                avatar.scaleY = 1.035f
            }

            LanaVisualState.OFFLINE -> {
                stateLabel.text = "OFFLINE"
                status.text = "Offline sam. Dostupne su lokalne sposobnosti."
                avatar.setBackgroundColor(Color.rgb(48, 52, 61))
                avatar.alpha = 0.72f
            }

            LanaVisualState.ERROR -> {
                stateLabel.text = "ERROR"
                status.text = "Nesto nije u redu. Necu pogadjati."
                avatar.setBackgroundColor(Color.rgb(74, 38, 45))
            }
        }
    }
}
