package hr.vascharlie.lana3

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.concurrent.thread

class AutoUpdater(
    private val activity: Activity,
    private val onStatus: (String) -> Unit
) {
    companion object {
        private const val RELEASE_API =
            "https://api.github.com/repos/vas-charlie/lana-3/releases/latest"
        private const val APK_ASSET_NAME = "lana-3.apk"
        private const val CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L

        private const val PREFS = "lana_auto_updater"
        private const val KEY_LAST_CHECK = "last_check"
        private const val KEY_DOWNLOAD_ID = "download_id"
        private const val KEY_REMOTE_VERSION = "remote_version"
    }

    private val downloadManager =
        activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var checking = false
    private var receiverRegistered = false
    private var openingInstaller = false

    private val periodicCheck = object : Runnable {
        override fun run() {
            checkNow(force = false)
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

            val completedId =
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val expectedId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
            if (completedId == expectedId) {
                resumePendingInstall()
            }
        }
    }

    fun start() {
        registerReceiver()
        clearCompletedUpdateState()
        resumePendingInstall()
        checkNow(force = false)
        handler.removeCallbacks(periodicCheck)
        handler.postDelayed(periodicCheck, CHECK_INTERVAL_MS)
    }

    fun stop() {
        handler.removeCallbacks(periodicCheck)
        if (receiverRegistered) {
            runCatching { activity.unregisterReceiver(downloadReceiver) }
            receiverRegistered = false
        }
    }

    fun onResume() {
        if (openingInstaller) {
            openingInstaller = false
            clearCompletedUpdateState()
            return
        }

        resumePendingInstall()
        checkNow(force = false)
    }

    fun checkNow(force: Boolean = true) {
        if (checking) return

        val now = System.currentTimeMillis()
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        if (!force && now - lastCheck < CHECK_INTERVAL_MS) return

        checking = true
        thread(name = "lana-auto-update-check") {
            try {
                val release = fetchLatestRelease()
                prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()

                if (release.versionCode <= BuildConfig.VERSION_CODE) {
                    postStatus("LANA je ažurna.")
                    return@thread
                }

                val existingVersion = prefs.getInt(KEY_REMOTE_VERSION, -1)
                val existingId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
                if (existingVersion == release.versionCode && existingId != -1L) {
                    postStatus("Nova verzija je već preuzeta ili se preuzima.")
                    activity.runOnUiThread { resumePendingInstall() }
                    return@thread
                }

                activity.runOnUiThread {
                    downloadUpdate(release)
                }
            } catch (e: Exception) {
                postStatus("Provjera ažuriranja trenutačno nije dostupna.")
            } finally {
                checking = false
            }
        }
    }

    private data class ReleaseInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String
    )

    private fun fetchLatestRelease(): ReleaseInfo {
        val connection = URL(RELEASE_API).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "LANA-3-Android-Updater")

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("GitHub release API returned $responseCode")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.getString("tag_name")
            val versionCode = Regex("""dev-(\d+)""")
                .matchEntire(tag)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?: throw IllegalStateException("Unsupported release tag: $tag")

            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                if (asset.optString("name") == APK_ASSET_NAME) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }

            return ReleaseInfo(
                versionCode = versionCode,
                versionName = tag.removePrefix("dev-"),
                downloadUrl = apkUrl
                    ?: throw IllegalStateException("Release APK asset is missing")
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadUpdate(release: ReleaseInfo) {
        val fileName = apkFileName(release.versionCode)

        val request = DownloadManager.Request(Uri.parse(release.downloadUrl))
            .setTitle("LANA 3 ažuriranje")
            .setDescription("Preuzimam verziju ${release.versionName}.")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(
                activity,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

        val downloadId = downloadManager.enqueue(request)
        prefs.edit()
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .putInt(KEY_REMOTE_VERSION, release.versionCode)
            .apply()

        postStatus("Nova verzija LANE se automatski preuzima.")
    }

    fun resumePendingInstall() {
        val remoteVersion = prefs.getInt(KEY_REMOTE_VERSION, -1)
        if (remoteVersion == -1 || remoteVersion <= BuildConfig.VERSION_CODE) {
            clearCompletedUpdateState()
            return
        }

        val downloadId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return

            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex < 0) return

            when (cursor.getInt(statusIndex)) {
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING,
                DownloadManager.STATUS_PAUSED -> {
                    postStatus("Nova verzija LANE se preuzima.")
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    promptInstall(downloadId, remoteVersion)
                }

                DownloadManager.STATUS_FAILED -> {
                    clearPendingUpdate()
                    postStatus("Preuzimanje ažuriranja nije uspjelo. Pokušat ću ponovno.")
                }
            }
        }
    }

    private fun promptInstall(downloadId: Long, remoteVersion: Int) {
        if (!verifyDownloadedApk(remoteVersion)) {
            clearPendingUpdate()
            postStatus("Sigurnosna provjera ažuriranja nije prošla. APK neće biti instaliran.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            postStatus("Ažuriranje je provjereno. Dopusti LANI instaliranje novih verzija.")
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivity(settingsIntent)
            return
        }

        val apkUri = downloadManager.getUriForDownloadedFile(downloadId)
        if (apkUri == null) {
            postStatus("Preuzeti APK nije dostupan za instalaciju.")
            return
        }

        openingInstaller = true
        postStatus("Ažuriranje je provjereno i spremno za instalaciju.")

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(installIntent)
    }

    private fun verifyDownloadedApk(remoteVersion: Int): Boolean {
        val downloadsDir =
            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
        val apkFile = File(downloadsDir, apkFileName(remoteVersion))
        if (!apkFile.isFile) return false

        val packageManager = activity.packageManager
        val archiveInfo = getArchivePackageInfo(packageManager, apkFile) ?: return false
        if (archiveInfo.packageName != activity.packageName) return false

        val archiveVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archiveInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            archiveInfo.versionCode.toLong()
        }

        if (archiveVersion != remoteVersion.toLong()) return false
        if (archiveVersion <= BuildConfig.VERSION_CODE.toLong()) return false

        val installedInfo = getInstalledPackageInfo(packageManager) ?: return false
        val installedSigners = currentSignerDigests(installedInfo)
        val archiveSigners = currentSignerDigests(archiveInfo)

        return installedSigners.isNotEmpty() &&
            installedSigners == archiveSigners
    }

    private fun getArchivePackageInfo(
        packageManager: PackageManager,
        apkFile: File
    ): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.GET_SIGNATURES
            )
        }
    }

    private fun getInstalledPackageInfo(packageManager: PackageManager): PackageInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    activity.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    activity.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
        }.getOrNull()
    }

    private fun currentSignerDigests(packageInfo: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners?.toList().orEmpty()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures?.toList().orEmpty()
        }

        return signatures.map { signature ->
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
            digest.joinToString(separator = "") { byte -> "%02X".format(byte) }
        }.toSet()
    }

    private fun apkFileName(versionCode: Int): String =
        "lana-3-$versionCode.apk"

    private fun registerReceiver() {
        if (receiverRegistered) return

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                downloadReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            activity.registerReceiver(downloadReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun clearCompletedUpdateState() {
        val remoteVersion = prefs.getInt(KEY_REMOTE_VERSION, -1)
        if (remoteVersion != -1 && BuildConfig.VERSION_CODE >= remoteVersion) {
            clearPendingUpdate()
        }
    }

    private fun clearPendingUpdate() {
        prefs.edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_REMOTE_VERSION)
            .apply()
    }

    private fun postStatus(message: String) {
        activity.runOnUiThread {
            onStatus(message)
        }
    }
}
