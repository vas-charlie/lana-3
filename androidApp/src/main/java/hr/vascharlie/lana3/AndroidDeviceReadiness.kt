package hr.vascharlie.lana3

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

enum class AndroidFormFactor {
    PHONE,
    TABLET,
}

data class AndroidDeviceReadiness(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val formFactor: AndroidFormFactor,
    val totalMemoryGb: Double,
    val cameraAvailable: Boolean,
    val cameraPermissionGranted: Boolean,
    val microphoneAvailable: Boolean,
    val microphonePermissionGranted: Boolean,
    val locationAvailable: Boolean,
    val coarseLocationGranted: Boolean,
    val preciseLocationGranted: Boolean,
) {
    val hasMissingRuntimePermissions: Boolean
        get() =
            (cameraAvailable && !cameraPermissionGranted) ||
                (microphoneAvailable && !microphonePermissionGranted) ||
                (locationAvailable && !coarseLocationGranted)
}

object AndroidDeviceReadinessProbe {
    fun snapshot(context: Context): AndroidDeviceReadiness {
        val packageManager = context.packageManager
        val configuration = context.resources.configuration

        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)

        val cameraAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val microphoneAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        val locationAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)

        return AndroidDeviceReadiness(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            formFactor =
                if (configuration.smallestScreenWidthDp >= 600) {
                    AndroidFormFactor.TABLET
                } else {
                    AndroidFormFactor.PHONE
                },
            totalMemoryGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0),
            cameraAvailable = cameraAvailable,
            cameraPermissionGranted =
                isGranted(context, Manifest.permission.CAMERA),
            microphoneAvailable = microphoneAvailable,
            microphonePermissionGranted =
                isGranted(context, Manifest.permission.RECORD_AUDIO),
            locationAvailable = locationAvailable,
            coarseLocationGranted =
                isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION),
            preciseLocationGranted =
                isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION),
        )
    }

    fun permissionsToRequest(context: Context): Array<String> {
        val snapshot = snapshot(context)
        val permissions = linkedSetOf<String>()

        if (snapshot.cameraAvailable && !snapshot.cameraPermissionGranted) {
            permissions += Manifest.permission.CAMERA
        }

        if (snapshot.microphoneAvailable && !snapshot.microphonePermissionGranted) {
            permissions += Manifest.permission.RECORD_AUDIO
        }

        if (snapshot.locationAvailable && !snapshot.preciseLocationGranted) {
            permissions += Manifest.permission.ACCESS_COARSE_LOCATION
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
        }

        return permissions.toTypedArray()
    }

    private fun isGranted(context: Context, permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
