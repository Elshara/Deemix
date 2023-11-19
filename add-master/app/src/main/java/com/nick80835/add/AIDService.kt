package com.nick80835.add

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.net.Uri
import android.os.*
import android.util.ArrayMap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import ealvatag.audio.AudioFileIO
import ealvatag.tag.FieldKey
import ealvatag.tag.images.ArtworkFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

const val serviceChannelID = "${BuildConfig.APPLICATION_ID}.service"
const val serviceNotificationID = 69
const val downloadChannelID = "${BuildConfig.APPLICATION_ID}.downloads"

private var aidService: AIDService? = null

class AIDService : Service() {
    private var activityMessenger: Messenger? = null
    private var serviceMessenger: Messenger = Messenger(IncomingHandler(this))
    private var sharedPreferences: SharedPreferences? = null
    private var sessionToken: String? = null
    private val decryptor = Decryptor()
    private val backend = BackendFunctions()
    private var bound = false
    private var downloadTasksInternal: ArrayMap<String, Downloader> = ArrayMap()

    override fun onCreate() {
        super.onCreate()

        for (file in cacheDir.listFiles { file ->
            file.name.matches(Regex("(.*)encrypted"))
        }!!) {
            file.delete()
        }

        createNotificationChannel()
        startService()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(CancelReceiver(), IntentFilter())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        aidService = this
        activityMessenger = intent?.getParcelableExtra("Messenger")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service bound")
        activityMessenger = intent?.getParcelableExtra("Messenger")
        bound = true
        return serviceMessenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        activityMessenger = null

        return if (super.onUnbind(intent)) {
            bound = true
            true
        } else {
            bound = false
            killIfNeeded()
            true
        }
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "Service rebound")
        activityMessenger = intent?.getParcelableExtra("Messenger")
        bound = true
        super.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        for (task in downloadTasksInternal) {
            cancelDownload(task.key)
        }

        for (file in cacheDir.listFiles { file ->
            file.name.matches(Regex("(.*)encrypted"))
        }!!) {
            file.delete()
        }
    }

    private fun killIfNeeded() {
        if (downloadTasksInternal.isEmpty() && !bound) {
            Log.d(TAG, "Killing service")
            stopSelf()
        }
    }

    private fun addTask(downloadTask: Downloader) {
        downloadTasksInternal[downloadTask.taskId] = downloadTask
    }

    private fun removeTask(taskId: String) {
        downloadTasksInternal.remove(taskId)
    }

    private fun startService() {
        val serverNotifIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val serverPendingIntent = PendingIntent.getActivity(this, 0, serverNotifIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val serverNotifBuilder = NotificationCompat.Builder(this, serviceChannelID).apply {
            setContentTitle(resources.getString(R.string.app_name))
            setTicker(resources.getString(R.string.app_name))
            setContentText(resources.getString(R.string.service_running))
            setSmallIcon(R.drawable.ic_service_notif)
            setContentIntent(serverPendingIntent)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setVisibility(NotificationCompat.VISIBILITY_SECRET)
            priority = NotificationCompat.PRIORITY_MIN
        }

        startForeground(serviceNotificationID, serverNotifBuilder.build())
    }

    private class IncomingHandler(val aidService: AIDService) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.data.get("task") as String) {
                "request_download" -> {
                    val thisData = msg.obj as TrackData
                    val quality = msg.data.get("quality") as Int
                    val albumDownload = msg.data.get("albumDownload") as Boolean

                    GlobalScope.launch {
                        aidService.downloadTrack(thisData, quality, albumDownload)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val thisNotificationManager = getSystemService(NotificationManager::class.java)!!

            val servicename = "Service Notification"
            val servicedescription = "The notification present when AIDS is running."
            val servicechannel = NotificationChannel(serviceChannelID, servicename, NotificationManager.IMPORTANCE_MIN)
            servicechannel.description = servicedescription
            servicechannel.setSound(null, null)
            thisNotificationManager.createNotificationChannel(servicechannel)

            val downloadname = "Download Notifications"
            val downloaddescription = "Notifications providing the status of downloads."
            val downloadchannel = NotificationChannel(downloadChannelID, downloadname, NotificationManager.IMPORTANCE_DEFAULT)
            downloadchannel.description = downloaddescription
            downloadchannel.setSound(null, null)
            thisNotificationManager.createNotificationChannel(downloadchannel)
        }
    }

    private fun downloadTrack(thisData: TrackData, quality: Int, albumDownload: Boolean = false) {
        val thisTaskId = "${thisData.trackId}_$quality"

        val extension = if (quality == 9) {"flac"} else {"mp3"}

        val artistSep = when (sharedPreferences!!.getInt("artist_separator", 0)) {
            0 -> ", "
            1 -> ","
            2 -> "; "
            3 -> ";"
            4 -> " / "
            5 -> "/"
            6 -> " & "
            7 -> "&"
            8 -> "\uE000"
            else -> ", "
        }

        thisData.linkedTrackTagData = backend.getTrackTagData(thisData, artistSep)

        var downloadPath = sharedPreferences!!.getString("download_path", "${Environment.getExternalStorageDirectory().absolutePath}/Music/Deezer")!!
        var friendlySaveName = "${thisData.primaryName} - ${thisData.tertiaryName}"

        if (sharedPreferences!!.getBoolean("create_album_folders", true) && albumDownload) {
            downloadPath += "/${thisData.secondaryName!!.scrub()} - ${thisData.tertiaryName!!.scrub()}"
            friendlySaveName = "${thisData.linkedTrackTagData!!.trackNumber} - ${thisData.primaryName}"
        }

        File(downloadPath).mkdirs()

        val thisFilePathEncrypted = "$cacheDir/$thisTaskId.$extension.encrypted"
        val thisFilePathComplete = "$downloadPath/${friendlySaveName.scrub()}.$extension"

        if (File(thisFilePathComplete).exists()) {
            requestSnackbar(getString(R.string.already_downloaded, thisData.primaryName))
            return
        }

        if (sessionToken == null) sessionToken = decryptor.getSID()

        if (sharedPreferences!!.getBoolean("get_lyrics", false)) {
            val thisTrackLyrics = backend.getTrackLyrics(thisData.trackId!!, sessionToken!!)

            if (thisTrackLyrics.first != null) {
                if (thisTrackLyrics.second) {
                    val lyricsFile = File("$downloadPath/${friendlySaveName.scrub()}.lrc")

                    if (!lyricsFile.exists()) {
                        lyricsFile.createNewFile()
                        lyricsFile.appendText(thisTrackLyrics.first!!)
                    }
                } else {
                    thisData.trackLyrics = thisTrackLyrics.first
                }
            }
        }

        val thisBlowfishKey = decryptor.createBlowfishKey(thisData.trackId!!)
        val thisTrackSecretData = decryptor.getTrackSecrets(thisData.trackId!!, sessionToken!!)!!

        if (quality == 1 && thisTrackSecretData["FILESIZE_MP3_128"] == "0") {
            requestSnackbar(getString(R.string.quality_not_available, thisData.primaryName, "MP3 128"))
            return
        } else if (quality == 3 && thisTrackSecretData["FILESIZE_MP3_320"] == "0") {
            requestSnackbar(getString(R.string.quality_not_available, thisData.primaryName, "MP3 320"))
            return
        } else if (quality == 9 && thisTrackSecretData["FILESIZE_FLAC"] == "0") {
            requestSnackbar(getString(R.string.quality_not_available, thisData.primaryName, "FLAC"))
            return
        }

        val thisPUID = thisTrackSecretData["PUID"] as String
        val thisMediaVersion = thisTrackSecretData["MEDIA_VERSION"] as String

        val downloadUrl = decryptor.getDownloadUrl(thisData.trackId!!, thisPUID, thisMediaVersion, quality)

        val thisDownloadTask = Downloader()
        thisDownloadTask.taskId = thisTaskId
        thisDownloadTask.outputPath = thisFilePathEncrypted

        newDownloadNotification(thisTaskId, thisData.primaryName!!, thisData.tertiaryName!!)

        thisDownloadTask.postExecute = { taskId ->
            GlobalScope.launch {
                decryptDownloadedTrack(
                    thisFilePathEncrypted,
                    thisBlowfishKey,
                    thisData,
                    thisFilePathComplete,
                    taskId
                )
            }
        }

        thisDownloadTask.onTaskCancel = { taskId ->
            Log.d(TAG, "Cancelling $taskId")
            downloadCancelNotification(taskId)
            File(thisFilePathEncrypted).delete()
            removeTask(taskId)
            killIfNeeded()
        }

        thisDownloadTask.onTaskFailure = { taskId ->
            Log.d(TAG, "Failing $taskId")
            downloadEndNotification(taskId, "Download failed")
            File(thisFilePathEncrypted).delete()
            removeTask(taskId)
            killIfNeeded()
        }

        thisDownloadTask.progressCallback = { taskId, progress, progressSize ->
            if (taskId in notifs.keys) {
                updateDownloadProgress(taskId, progress, progressSize)
            }
        }

        addTask(thisDownloadTask)

        thisDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL(downloadUrl))
    }

    private fun decryptDownloadedTrack(
        thisFilePathEncrypted: String,
        thisBlowfishKey: String,
        thisData: TrackData,
        thisFilePathComplete: String,
        downloadTaskId: String) {

        var chunkCounter = 0

        val thisDecryptedFile = File(thisFilePathComplete)
        thisDecryptedFile.createNewFile()

        for (chunk in ByteArrayChunkIterator(File(thisFilePathEncrypted).readBytes(), 2048)) {
            val decChunk: ByteArray = if ((chunkCounter % 3) > 0 || chunk.size < 2048) {
                chunk
            } else {
                decryptor.blowJob(chunk, thisBlowfishKey)
            }

            thisDecryptedFile.appendBytes(decChunk)
            chunkCounter++
        }

        File(thisFilePathEncrypted).delete()
        tagDownloadedSong(thisData, thisFilePathComplete, downloadTaskId)
    }

    private fun tagDownloadedSong(thisData: TrackData, thisFilePathComplete: String, downloadTaskId: String) {
        val thisFile = File(thisFilePathComplete)
        val thisAudioFile = AudioFileIO.read(thisFile)
        val thisAudioTag = thisAudioFile.setNewDefaultTag()

        thisAudioTag.apply {
            setField(FieldKey.TITLE, thisData.primaryName ?: "")
            setField(FieldKey.ALBUM, thisData.secondaryName ?: "")
            setField(FieldKey.ALBUM_ARTIST, thisData.tertiaryName ?: "")
            setField(FieldKey.TRACK, thisData.linkedTrackTagData!!.trackNumber?.toString() ?: "")
            setField(FieldKey.BPM, thisData.linkedTrackTagData!!.trackBPM?.toString() ?: "")
            setField(FieldKey.GENRE, thisData.linkedTrackTagData!!.trackGenre ?: "")
            setField(FieldKey.YEAR, thisData.linkedTrackTagData!!.trackDate ?: "")

            setField(FieldKey.ARTIST, thisData.linkedTrackTagData!!.trackContributors ?: thisData.tertiaryName ?: "")

            setField(FieldKey.LYRICS, thisData.trackLyrics ?: "")
        }

        if (!thisData.coverXL.isNullOrBlank()) {
            val thisTempFile = File("${cacheDir.absolutePath}/${thisData.linkedAlbumId}_xl.tmp")

            if (!thisTempFile.exists()) {
                val thisAlbumArtXL: ByteArray? = URL(thisData.coverXL).readBytes()

                if (thisAlbumArtXL != null && !thisTempFile.exists()) {
                    thisTempFile.createNewFile()
                    thisTempFile.writeBytes(thisAlbumArtXL)
                }
            }

            val thisArtwork = ArtworkFactory.createArtworkFromFile(thisTempFile)
            thisAudioTag.addArtwork(thisArtwork)
        }

        thisAudioFile.save()

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(thisFilePathComplete))))

        downloadEndNotification(downloadTaskId, "Download complete")
        requestSnackbar(getString(R.string.successfully_downloaded, thisData.primaryName))
        removeTask(downloadTaskId)
        killIfNeeded()
    }

    private fun requestSnackbar(snackbarText: String) {
        try {
            activityMessenger?.send(Message().apply {
                obj = "request_snackbar"
                data.putString("snackbarText", snackbarText)
            })
        } catch (e: Exception) {
            Log.w(TAG, e.message!!)
        }
    }

    private var notifId = 100
    private var notifs: ArrayMap<String, ArrayMap<Int, NotificationCompat.Builder>> = ArrayMap()

    private fun newDownloadNotification(downloadTaskId: String, name: String, artist: String) {
        val thisNotifId = notifId++

        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("taskId", downloadTaskId)
        cancelIntent.action = "com.nick80835.add.CANCEL_DOWNLOAD"

        val pendingCancelIntent = PendingIntent.getBroadcast(this, thisNotifId, cancelIntent, PendingIntent.FLAG_ONE_SHOT)

        val thisBuilder = NotificationCompat.Builder(this, downloadChannelID).apply {
            setSmallIcon(android.R.drawable.stat_sys_download)
            setContentTitle("$name - $artist")
            setProgress(0, 0, true)
            setOnlyAlertOnce(true)
            setTimeoutAfter(6000000)
            setOngoing(true)
            addAction(0, getString(R.string.cancel), pendingCancelIntent)
        }

        val thisBuilderMap: ArrayMap<Int, NotificationCompat.Builder> = ArrayMap()

        thisBuilderMap[thisNotifId] = thisBuilder

        notifs[downloadTaskId] = thisBuilderMap

        NotificationManagerCompat.from(this).notify(
            downloadTaskId,
            notifs.getValue(downloadTaskId).keyAt(0),
            thisBuilder.build()
        )
    }

    private fun updateDownloadProgress(downloadTaskId: String, progress: Int, progressSize: String) {
        val thisBuilder = notifs.getValue(downloadTaskId).getValue(notifs.getValue(downloadTaskId).keyAt(0))

        thisBuilder.apply {
            setProgress(100, progress, false)
            setContentText(progressSize)
        }

        NotificationManagerCompat.from(this).notify(
            downloadTaskId,
            notifs.getValue(downloadTaskId).keyAt(0),
            thisBuilder.build()
        )
    }

    private fun downloadEndNotification(downloadTaskId: String, reason: String) {
        val thisBuilder = notifs.getValue(downloadTaskId).getValue(notifs.getValue(downloadTaskId).keyAt(0))

        thisBuilder.apply {
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setProgress(0, 0, false)
            setContentText(reason)
            setOngoing(false)
            mActions.clear()
        }

        NotificationManagerCompat.from(this).notify(
            downloadTaskId,
            notifs.getValue(downloadTaskId).keyAt(0),
            thisBuilder.build()
        )

        notifs.remove(downloadTaskId)
    }

    private fun downloadCancelNotification(downloadTaskId: String) {
        NotificationManagerCompat.from(this).cancel(
            downloadTaskId,
            notifs.getValue(downloadTaskId).keyAt(0)
        )

        notifs.remove(downloadTaskId)
    }

    fun cancelDownload(downloadTaskId: String) {
        if (downloadTaskId in downloadTasksInternal) {
            val thisTask = downloadTasksInternal.getValue(downloadTaskId)
            thisTask.cancel(false)
        }
    }

    private fun String.scrub(): String {
        var thisScrubbedString = this.replace("/", "_")
        thisScrubbedString = thisScrubbedString.replace(".", "")

        if (thisScrubbedString == "") {
            thisScrubbedString = "_"
        }

        return thisScrubbedString
    }
}

class CancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.nick80835.add.CANCEL_DOWNLOAD") {
            val taskId = intent.extras!!.get("taskId") as String
            aidService?.cancelDownload(taskId)
        }
    }
}
