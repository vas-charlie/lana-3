package hr.vascharlie.lana3

import android.app.Activity
import android.content.Intent
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
    }

    private lateinit var avatar: TextView
    private lateinit var stateLabel: TextView
    private lateinit var status: TextView
    private lateinit var updateStatus: TextView
    private lateinit var readinessStatus: TextView
    private lateinit var readinessButton: Button
    private lateinit var autoUpdater: AutoUpdater
    private lateinit var avatarMotion: LanaAvatarMotionController
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
            text = getString(R.string.app_name)
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.developer_preview, BuildConfig.VERSION_NAME)
            textSize = 14f
            setTextColor(Color.rgb(90, 180, 255))
        })

        updateStatus = TextView(this).apply {
            text = getString(R.string.checking_updates)
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
            text = getString(R.string.avatar_name)
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(15, 39, 67))
            contentDescription = getString(R.string.avatar_content_description)
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
            text = getString(R.string.change_lana_state)
            isAllCaps = false
            setOnClickListener { cycleVisualState() }
        }
        root.addView(stateButton)

        root.addView(Button(this).apply {
            text = getString(R.string.smart_ride_test_button)
            isAllCaps = false
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, SmartRideTestActivity::class.java)
                )
            }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.voice_lab_test_button)
            isAllCaps = false
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, VoiceTestActivity::class.java)
                )
            }
        })

        root.addView(Button(this).apply {
            text = getString(R.string.notes_lab_test_button)
            isAllCaps = false
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, NotesTestActivity::class.java)
                )
            }
        })

        avatarMotion = LanaAvatarMotionController(avatar)
        setContentView(root)
        renderState(LanaVisualState.IDLE)
        refreshDeviceReadiness()

        autoUpdater = AutoUpdater(this) { status ->
            updateStatus.text = formatAutoUpdateStatus(status)
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
        if (::avatarMotion.isInitialized) {
            avatarMotion.stop()
        }
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

    private fun requestMissingLanaPermissions() {
        val permissions = AndroidDeviceReadinessProbe.permissionsToRequest(this)
        if (permissions.isEmpty()) {
            refreshDeviceReadiness()
            return
        }

        requestPermissions(permissions, REQUEST_LANA_PERMISSIONS)
    }

    private fun formatAutoUpdateStatus(
        status: AutoUpdateStatus,
    ): String = when (status) {
        AutoUpdateStatus.UpToDate ->
            getString(R.string.updater_up_to_date)

        AutoUpdateStatus.AlreadyDownloading ->
            getString(R.string.updater_already_downloading)

        AutoUpdateStatus.CheckUnavailable ->
            getString(R.string.updater_check_unavailable)

        AutoUpdateStatus.DownloadStarted ->
            getString(R.string.updater_download_started)

        AutoUpdateStatus.Downloading ->
            getString(R.string.updater_downloading)

        AutoUpdateStatus.DownloadFailed ->
            getString(R.string.updater_download_failed)

        AutoUpdateStatus.VerificationFailed ->
            getString(R.string.updater_verification_failed)

        AutoUpdateStatus.InstallPermissionRequired ->
            getString(R.string.updater_install_permission_required)

        AutoUpdateStatus.ApkUnavailable ->
            getString(R.string.updater_apk_unavailable)

        AutoUpdateStatus.ReadyToInstall ->
            getString(R.string.updater_ready_to_install)
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
                getString(R.string.sensor_core_ready)

            is CapabilityGateResult.Blocked ->
                getString(
                    R.string.sensor_core_waiting,
                    gateResult.failures.joinToString { it.capabilityId },
                )
        }

        readinessStatus.text = getString(
            R.string.readiness_with_core_status,
            formatDeviceReadiness(snapshot),
            coreStatus,
        )
        readinessButton.isEnabled = permissions.isNotEmpty()
        readinessButton.text =
            if (permissions.isEmpty()) {
                getString(R.string.sensor_permissions_ready)
            } else {
                getString(R.string.allow_required_sensors, permissions.size)
            }
    }

    private fun formatDeviceReadiness(
        snapshot: AndroidDeviceReadiness,
    ): String {
        val formFactor = when (snapshot.formFactor) {
            AndroidFormFactor.PHONE ->
                getString(R.string.device_form_factor_phone)

            AndroidFormFactor.TABLET ->
                getString(R.string.device_form_factor_tablet)
        }

        val camera = when {
            !snapshot.cameraAvailable ->
                getString(R.string.device_camera_unavailable)

            snapshot.cameraPermissionGranted ->
                getString(R.string.device_camera_ready)

            else ->
                getString(R.string.device_camera_permission_needed)
        }

        val microphone = when {
            !snapshot.microphoneAvailable ->
                getString(R.string.device_microphone_unavailable)

            snapshot.microphonePermissionGranted ->
                getString(R.string.device_microphone_ready)

            else ->
                getString(R.string.device_microphone_permission_needed)
        }

        val location = when {
            !snapshot.locationAvailable ->
                getString(R.string.device_location_unavailable)

            snapshot.preciseLocationGranted ->
                getString(R.string.device_location_precise)

            snapshot.coarseLocationGranted ->
                getString(R.string.device_location_approximate)

            else ->
                getString(R.string.device_location_permission_needed)
        }

        val identity = getString(
            R.string.device_identity_line,
            snapshot.manufacturer,
            snapshot.model,
            formFactor,
            snapshot.androidVersion,
            snapshot.totalMemoryGb,
        )
        val sensors = getString(
            R.string.device_sensor_line,
            camera,
            microphone,
            location,
        )

        return getString(
            R.string.device_readiness_block,
            identity,
            sensors,
        )
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
        avatarMotion.stop()
        avatar.alpha = 1f
        avatar.scaleX = 1f
        avatar.scaleY = 1f

        when (state) {
            LanaVisualState.IDLE -> {
                stateLabel.text = getString(R.string.state_idle)
                status.text = getString(R.string.status_idle)
                avatar.setBackgroundColor(Color.rgb(15, 39, 67))
            }

            LanaVisualState.LISTENING -> {
                stateLabel.text = getString(R.string.state_listening)
                status.text = getString(R.string.status_listening)
                avatar.setBackgroundColor(Color.rgb(12, 55, 82))
                avatar.scaleX = 1.025f
                avatar.scaleY = 1.025f
            }

            LanaVisualState.THINKING -> {
                stateLabel.text = getString(R.string.state_thinking)
                status.text = getString(R.string.status_thinking)
                avatar.setBackgroundColor(Color.rgb(31, 43, 72))
                avatar.alpha = 0.88f
            }

            LanaVisualState.SPEAKING -> {
                stateLabel.text = getString(R.string.state_speaking)
                status.text = getString(R.string.status_speaking)
                avatar.setBackgroundColor(Color.rgb(18, 65, 77))
                avatar.scaleX = 1.035f
                avatar.scaleY = 1.035f
            }

            LanaVisualState.OFFLINE -> {
                stateLabel.text = getString(R.string.state_offline)
                status.text = getString(R.string.status_offline)
                avatar.setBackgroundColor(Color.rgb(48, 52, 61))
                avatar.alpha = 0.72f
            }

            LanaVisualState.ERROR -> {
                stateLabel.text = getString(R.string.state_error)
                status.text = getString(R.string.status_error)
                avatar.setBackgroundColor(Color.rgb(74, 38, 45))
            }
        }

        avatarMotion.applyState(state)
    }
}
