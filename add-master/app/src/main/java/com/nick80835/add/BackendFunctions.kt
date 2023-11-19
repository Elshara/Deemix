package com.nick80835.add

import android.util.Log
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class BackendFunctions {
    private fun getGenreFromId(genreId: Int): String? {
        if (genreId == -1) return null

        val thisGenreUrl = "$deezerApiGenre$genreId"
        val genreData = URL(thisGenreUrl).readText()

        val resultMap: LinkedTreeMap<*, *> = gson.fromJson(genreData, object : TypeToken<Map<String, Any>>() {}.type)

        return resultMap["name"] as String?
    }

    fun getAlbumTracklist(contentListUrl: String, limit: Int): ArrayList<*> {
        val tracklistHolder = URL("$contentListUrl?limit=$limit").readText()
        val resultMap: Map<String, Any> = gson.fromJson(tracklistHolder, object : TypeToken<Map<String, Any>>() {}.type)
        return resultMap["data"] as ArrayList<*>
    }

    fun getArtistAlbumlist(artistId: Long): ArrayList<*> {
        val thisTracklistUrl = "$deezerApiArtist$artistId/albums"
        val tracklistHolder = URL(thisTracklistUrl).readText()
        val resultMap: Map<String, Any> = gson.fromJson(tracklistHolder, object : TypeToken<Map<String, Any>>() {}.type)
        return resultMap["data"] as ArrayList<*>
    }

    fun getAlbumData(sourceAlbumId: Long): TrackData {
        val thisAlbumUrl = "$deezerApiAlbum$sourceAlbumId"
        val albumData = URL(thisAlbumUrl).readText()
        val resultMap: Map<String, Any> = gson.fromJson(albumData, object : TypeToken<Map<String, Any>>() {}.type)

        // album info
        val thisResult = resultMap as LinkedTreeMap<*, *>
        val primaryName = thisResult["title"] as String?
        val coverSmall = thisResult["cover_small"] as String?
        val coverBig = thisResult["cover_big"] as String?
        val coverXL = thisResult["cover_xl"] as String?
        val tertiaryName = "${(thisResult["nb_tracks"] as Double?)?.toInt()} track(s)"
        val explicit = thisResult["explicit_lyrics"] as Boolean?
        val contentListUrl = thisResult["tracklist"] as String?
        val trackCount = (thisResult["nb_tracks"] as Double?)?.toInt()
        val albumId = (thisResult["id"] as Double?)?.toLong()
        val genreId = (thisResult["genre_id"] as Double?)?.toInt()

        // linked artist info
        val thisArtist = thisResult["artist"] as LinkedTreeMap<*, *>?
        val secondaryName = thisArtist?.get("name") as String?

        return TrackData().apply {
            this.primaryName = primaryName
            this.secondaryName = secondaryName
            this.tertiaryName = tertiaryName
            this.coverSmall = coverSmall
            this.coverBig = coverBig
            this.coverXL = coverXL
            this.trackId = albumId
            this.genreId = genreId
            this.explicit = explicit
            this.contentListUrl = contentListUrl
            this.trackCount = trackCount
            this.resultRaw = thisResult
            this.cardType = "album"
        }
    }

    fun getTracklist(thisData: TrackData): ArrayList<TrackData> {
        val thisTracklist: ArrayList<*> = getAlbumTracklist(thisData.contentListUrl!!, thisData.trackCount!!)
        val thisTrackDataArrayList: ArrayList<TrackData> = ArrayList()

        for (i in thisTracklist) {
            val thisResult = i as LinkedTreeMap<*, *>

            // track info
            val primaryName = thisResult["title"] as String?
            val previewUrl = thisResult["preview"] as String?
            val explicit = thisResult["explicit_lyrics"] as Boolean?
            val trackId = (thisResult["id"] as Double?)?.toLong()
            val trackLength = (thisResult["duration"] as Double?)?.toInt()
            val readable = thisResult["readable"] as Boolean

            // linked album info
            val thisAlbum = thisData.resultRaw!!
            val linkedAlbumId = (thisAlbum["id"] as Double?)?.toLong()
            val secondaryName = thisAlbum["title"] as String?
            val coverSmall = thisAlbum["cover_small"] as String?
            val coverBig = thisAlbum["cover_big"] as String?
            val coverXL = thisAlbum["cover_xl"] as String?

            // linked artist info
            val thisArtist = thisResult["artist"] as LinkedTreeMap<*, *>
            val tertiaryName = thisArtist["name"] as String?

            val thisResultData = TrackData().apply {
                this.primaryName = primaryName
                this.secondaryName = secondaryName
                this.tertiaryName = tertiaryName
                this.coverSmall = coverSmall
                this.coverBig = coverBig
                this.coverXL = coverXL
                this.trackId = trackId
                this.trackPreviewUrl = previewUrl
                this.trackLength = trackLength
                this.readable = readable
                this.linkedAlbumId = linkedAlbumId
                this.explicit = explicit
                this.resultRaw = thisResult
                this.cardType = "track"
            }

            thisTrackDataArrayList.add(thisResultData)
        }

        return thisTrackDataArrayList
    }

    fun getTrackTagData(trackData: TrackData, artistSep: String): TrackTagData {
        val fullTrackDataUrl = "$deezerApiTrack${trackData.trackId}"
        val fullTrackData = URL(fullTrackDataUrl).readText()

        val resultMap: LinkedTreeMap<*, *> = gson.fromJson(fullTrackData, object : TypeToken<Map<String, Any>>() {}.type)

        val contributorArrayList = resultMap["contributors"] as ArrayList<LinkedTreeMap<*, *>>

        var contributorString: String? = null

        if (!contributorArrayList.isNullOrEmpty()) {
            val contributorNames: ArrayList<String> = ArrayList()

            for (contributor in contributorArrayList) {
                contributorNames.add(contributor["name"] as String)
            }

            contributorString = contributorNames.joinToString(artistSep)
        }

        val trackNumber = (resultMap["track_position"] as Double?)?.toInt()
        val trackBPM = (resultMap["bpm"] as Double?)?.toFloat()
        val trackDate = resultMap["release_date"] as String?

        trackData.genreId = getAlbumData(trackData.linkedAlbumId!!).genreId

        return TrackTagData().apply {
            this.trackNumber = trackNumber
            this.trackBPM = trackBPM
            this.trackContributors = contributorString
            this.trackDate = trackDate

            if (trackData.genreId != null) {
                this.trackGenre = getGenreFromId(trackData.genreId!!)
            }
        }
    }

    fun getTrackLyrics(trackId: Long, thisSID: String): Pair<String?, Boolean> {
        val gatewayUrl = URL("https://api.deezer.com/1.0/gateway.php?api_key=4VCYIJUCDLOUELGD1V8WBVYBNVDYOXEWSLLZDONGBBDFVXTZJRXPR29JRLQFO6ZE&$thisSID&input=3&output=3&method=song_getLyrics")

        var resultMap: Map<*, *>? = null

        with(gatewayUrl.openConnection() as HttpsURLConnection) {
            requestMethod = "POST"
            doInput = true
            doOutput = true

            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
            )
            setRequestProperty("Content-Language", "en-US")
            setRequestProperty("Cache-Control", "max-age=0")
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Accept-Charset", "utf-8,ISO-8859-1;q=0.7,*;q=0.3")
            setRequestProperty("Accept-Language", "en-US,en;q=0.9,en-US;q=0.8,en;q=0.7")
            setRequestProperty("Connection", "keep-alive")

            val writer = outputStream

            writer.write("{\"sng_id\":\"$trackId\"}".toByteArray())
            writer.flush()

            Log.d(TAG, "LRC URL: $url")
            Log.d(TAG, "LRC response code: $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val inputLine = it.readLine()

                val rawResultMap: Map<String, Any> = gson.fromJson(inputLine, object : TypeToken<Map<String, Any>>() {}.type)
                resultMap = rawResultMap["results"] as Map<*, *>

                Log.d(TAG, inputLine)
            }
        }

        if ((resultMap!!["LYRICS_SYNC_JSON"] as ArrayList<Map<*, *>?>?) != null) {
            var syncedLyrics = ""

            for (i in resultMap!!["LYRICS_SYNC_JSON"] as ArrayList<Map<*, *>?>) {
                if (i != null) {
                    if (!(i["line"] as String?).isNullOrBlank()) {
                        syncedLyrics += "${i["lrc_timestamp"]}${i["line"]}\r\n"
                    }
                }
            }

            return Pair(syncedLyrics, true)
        } else if (!(resultMap!!["LYRICS_TEXT"] as String?).isNullOrBlank()) {
            return Pair(resultMap!!["LYRICS_TEXT"].toString(), false)
        } else {
            return Pair(null, false)
        }
    }
}
