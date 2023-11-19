package app.deemix.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.work.*
import app.deemix.downloader.SharedObjects.dz
import app.deemix.downloader.SharedObjects.queue
import app.deemix.downloader.Utils.createBlowfishKey
import app.deemix.downloader.Utils.decryptBlowfish
import app.deemix.downloader.types.Album
import app.deemix.downloader.types.DownloadItem
import app.deemix.downloader.types.Track
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt
import java.lang.Exception

class DownloadWorker(private val appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private var uuid = ""
        private lateinit var currentItem: DownloadItem

        private var totalSize = 0
        private var progressNext: Double = 0.00
        private var bitrate: String = "0"

        private var title = ""
        val NOTIFICATION_ID = 42
        lateinit var notification: Notification
    }

    private fun createNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId )
        return notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_get_app_24)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle(title)
            .setTicker(title)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override suspend fun doWork(): Result {
        // Get the UUID
        val thisUuid = inputData.getString("uuid")
        if (thisUuid == null){
            println("[DOWNLOAD ERROR] No UUID found")
            return Result.failure()
        }

        uuid = thisUuid
        currentItem = queue[uuid] ?: return Result.success()
        currentItem.progress = 0
        currentItem.downloaded = 0
        currentItem.failed = 0
        currentItem.errors = ArrayList()
        currentItem.status = "downloading"
        title = "${currentItem.artist} - ${currentItem.title}"

        // Get the data from disk
        val downloadObject: JSONObject
        try {
            appContext.openFileInput("$uuid.json").use { stream ->
                val text = stream.bufferedReader().use {
                    it.readText()
                }
                downloadObject = JSONObject(text)
            }
        } catch (e: Exception){
            println("[DOWNLOAD ERROR] Error: $e")
            return Result.failure()
        }
        downloadObject.put("status", currentItem.status)
        appContext.openFileOutput("$uuid.json", Context.MODE_PRIVATE).use { output ->
            output.write(downloadObject.toString().toByteArray())
        }
        notification = createNotification()
        sendUpdate()

        // Get the downloadData
        var downloadData: JSONObject? = null
        val downloadType: String = downloadObject.getString("itemType")
        if (downloadType == "single") downloadData = downloadObject.getJSONObject("single")
        else if (downloadType == "collection") downloadData = downloadObject.getJSONObject("collection")
        if (downloadData == null) {
            println("[DOWNLOAD ERROR] No download Data")
            return Result.failure()
        }
        totalSize = downloadObject.getInt("size")
        progressNext = 0.00
        bitrate = downloadObject.getString("bitrate")

        // Start Download
        if (downloadType == "single"){
            val track: Track = Track().restoreObject(downloadData.getJSONObject("trackAPI"))
            val album: Album? = if (downloadData.has("albumAPI")) Album().restoreObject(downloadData.getJSONObject("albumAPI")) else null
            downloadWrapper(track, album)
        } else if (downloadType == "collection"){
            val album: Album? = if (downloadData.has("albumAPI")) Album().restoreObject(downloadData.getJSONObject("albumAPI")) else null
            for (i in 0 until downloadData.getJSONArray("tracks").length()){
                val track: Track = Track().restoreObject(downloadData.getJSONArray("tracks").getJSONObject(i))
                downloadWrapper(track, album)
            }
        }

        currentItem.progress = 100
        downloadObject.put("progress", currentItem.progress)
        downloadObject.put("downloaded", currentItem.downloaded)
        downloadObject.put("failed", currentItem.failed)
        downloadObject.put("errors", JSONArray(currentItem.errors))

        currentItem.status = "downloaded"
        if (currentItem.failed == currentItem.downloaded + currentItem.failed){
            currentItem.status = "failed"
        } else if (currentItem.failed > 0){
            currentItem.status = "downloadedWithErrors"
        }
        downloadObject.put("status", currentItem.status)
        sendUpdate()

        appContext.openFileOutput("$uuid.json", Context.MODE_PRIVATE).use { output ->
            output.write(downloadObject.toString().toByteArray())
        }
        return Result.success()
    }

    private suspend fun sendUpdate(){
        if (isStopped) return
        setProgress( Data.Builder()
            .putString("uuid", uuid)
            .putInt("progress", currentItem.progress)
            .putInt("downloaded", currentItem.downloaded)
            .putInt("failed", currentItem.failed)
            .putStringArray("errors", currentItem.errors.toTypedArray())
            .putString("status", currentItem.status)
            .build()
        )
        setForeground(ForegroundInfo(NOTIFICATION_ID, notification))
    }

    private suspend fun download(track: Track, album: Album? = null){
        // Get Tags
        val sngId = track.id
        if (album != null) track.album = album

        if (track.isLocal){
            // Only to get refreshed track tokens
            val trackGW = dz.apiCallGW("song.getData", "{\"SNG_ID\": $sngId}")
            track.parseGW(trackGW)
        } else {
            val trackPageGW = dz.apiCallGW("deezer.pageTrack", "{\"SNG_ID\": $sngId}")
            track.parseGWPage(trackPageGW)

            // Only standard API has track bpm
            if (track.bpm == null){
                val trackAPI = dz.apiCall("track/$sngId")
                if (trackAPI.has("bpm")) {
                    track.bpm = trackAPI.getString("bpm").toFloat()
                }
            }
            // Only standard API has album genres
            if (track.album.genres == null){
                try {
                    val albumAPI = dz.apiCall("album/${track.album.id}")
                    track.album.parseAPI(albumAPI)
                } catch (_: Exception){}
            }
            // Only gw api has album discTotal
            if (track.album.discTotal == null){
                try {
                    val albumAPI = dz.apiCallGW("album.getData", "{\"ALB_ID\": ${track.album.id}}")
                    track.album.parseGW(albumAPI)
                } catch (_: Exception){}
            }

            if (track.album.date != null && track.date == null) track.date = track.album.date
        }
        track.cleanUp()

        // Get the correct bitrate
        val thisBitrate = bitrate
        val extension = when (thisBitrate){
            "9" -> "flac"
            else -> "mp3"
        }
        val format = when (thisBitrate){
            "9" -> "FLAC"
            "3" -> "MP3_320"
            "1" -> "MP3_128"
            else -> "MP3_128"
        }
        // Apply settings
        // Generate filename and filepath from metadata
        val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/Music/Deezer"
        val fileName = "${track.artist.name} - ${track.title}"
        val finalFilePath = "$filePath/$fileName.$extension"
        // Create the download folder if it doesn't exists
        File(filePath).mkdirs()

        // Generate cover URLs
        val coverSize = 800
        track.album.embeddedCoverURL = track.album.pic.getURL(coverSize, "jpg-80")
        // Download and cache the coverart
        val coverTempFile = File("${appContext.cacheDir}/alb${track.album.id}_$coverSize.jpg")
        if (!coverTempFile.exists()) {
            val thisAlbumArt: ByteArray? = URL(track.album.embeddedCoverURL).readBytes()
            if (thisAlbumArt != null && !coverTempFile.exists()) {
                coverTempFile.createNewFile()
                coverTempFile.writeBytes(thisAlbumArt)
            }
        }
        track.album.embeddedCoverPath = coverTempFile.absolutePath

        // Download the track
        val trackURL: String = dz.getTrackURL(track.trackToken!!, format)
            ?: throw Exception("No URL FOUND")
        val blowfishKey = createBlowfishKey(sngId)

        val request = URL(trackURL)
        with(request.openConnection() as HttpsURLConnection) {
            connect()
            val outputFile = File(finalFilePath)
            outputFile.createNewFile()

            var place = 0
            val chunk = ByteArray(16384)

            var modifiedStream = ByteArray(0)
            while (place < contentLength){
                val size = inputStream.read(chunk) // Returns the actual size of the chunk
                var cutChunk = chunk.copyOfRange(0, size) // Cuts down the chunk to the actual size

                modifiedStream += cutChunk
                while (modifiedStream.size >= 2048 * 3){
                    var decryptedChunk = ByteArray(0)
                    val decryptingChunk = modifiedStream.copyOfRange(0, 2048 * 3)
                    modifiedStream = modifiedStream.copyOfRange(2048 * 3, modifiedStream.size)
                    if (decryptingChunk.size > 2048){
                        decryptedChunk = decryptBlowfish(decryptingChunk.copyOfRange(0, 2048), blowfishKey)
                        decryptedChunk += decryptingChunk.copyOfRange(2048, decryptingChunk.size)
                    }
                    outputFile.appendBytes(decryptedChunk)
                }

                place += size
                updateProgress(size, contentLength)
            }
            if (modifiedStream.size >= 2048) {
                var decryptedChunk = decryptBlowfish(modifiedStream.copyOfRange(0, 2048), blowfishKey)
                decryptedChunk += modifiedStream.copyOfRange(2048, modifiedStream.size)
                outputFile.appendBytes(decryptedChunk)
            } else {
                outputFile.appendBytes(modifiedStream)
            }
        }
        // Add tags to the track
        if (!track.isLocal)
            tagFile(finalFilePath, track)
    }

    private fun tagFile(finalFilePath: String, track: Track) {
        val f = AudioFileIO.read(File(finalFilePath))

        val tag = f.createDefaultTag()
        tag.setField(FieldKey.TITLE, track.title)
        for (artist in track.artists){
            tag.addField(FieldKey.ARTIST, artist)
        }
        tag.setField(FieldKey.ALBUM, track.album.title)
        for (artist in track.album.artists){
            tag.addField(FieldKey.ALBUM_ARTIST, artist)
        }

        tag.setField(FieldKey.TRACK, track.trackNumber)
        tag.setField(FieldKey.TRACK_TOTAL, track.album.trackTotal)
        tag.setField(FieldKey.DISC_NO, track.discNumber)
        tag.setField(FieldKey.TRACK_TOTAL, track.album.discTotal)


        if (track.album.genres != null){
            for (genre in track.album.genres!!){
                tag.addField(FieldKey.GENRE, genre)
            }
        }
        if (track.album.date != null){
            tag.setField(FieldKey.YEAR, track.album.date!!.toString())
        }
        // TODO: Add TLEN
        if (track.bpm != null && track.bpm != 0f) tag.setField(FieldKey.BPM, track.bpm.toString())
        if (!track.album.label.isNullOrEmpty()) tag.setField(FieldKey.RECORD_LABEL, track.album.label)
        tag.setField(FieldKey.ISRC, track.isrc)
        if (!track.album.barcode.isNullOrEmpty()) tag.setField(FieldKey.BARCODE, track.album.barcode)

        // TODO: Add explicit
        // TODO: Add replaygain
        // TODO: Add unsync lyrics USLT
        // TODO: Add sync lyrics SYLT
        // TODO: Add involved people and composer

        tag.setField(FieldKey.COPYRIGHT, track.album.copyright)
        // TODO: Add compilation check IS_COMPILATION

        // TODO: Add source and sourceid
        // TODO: Add rating
        val artwork = ArtworkFactory.createArtworkFromFile(File(track.album.embeddedCoverPath))
        tag.setField(artwork)

        f.tag = tag
        f.commit()
    }

    private suspend fun updateProgress(chunkSize: Int, fileSize: Int){
        progressNext += ((chunkSize.toDouble() / fileSize.toDouble()) / totalSize.toDouble()) * 100.00

        if (progressNext.roundToInt() != currentItem.progress && progressNext.roundToInt() % 2 == 0){
            currentItem.progress = progressNext.roundToInt()
            sendUpdate()
        }
    }

    private suspend fun downloadWrapper(track: Track, album: Album? = null){
        try {
            download(track, album)
            currentItem.downloaded += 1
        } catch (e: Exception){
            currentItem.failed += 1
            currentItem.errors.add(e.toString())
            println(e)
            Log.e("deemix", "DownloadeError: ", e)
        }
    }
}