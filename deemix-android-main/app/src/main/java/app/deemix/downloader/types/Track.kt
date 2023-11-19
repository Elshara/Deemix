package app.deemix.downloader.types

import app.deemix.downloader.Utils.concatTitleVersion
import app.deemix.downloader.Utils.isExplicit
import app.deemix.downloader.toArrayList
import app.deemix.downloader.toHashMap
import app.deemix.downloader.toJSON
import app.deemix.downloader.toJSONArray
import org.json.JSONObject

class Track {
    lateinit var id: String

    // Full title
    var title: String = ""
    // The title without the version
    var titleShort: String? = null
    // Just the version
    var titleVersion: String? = null

    var trackToken: String? = null
    var trackTokenExpiration: Int = 0
    var fallbackID: String? = null

    // Just the main artist of the track
    var artist: Artist = Artist()
    // List of all roles with their respective artists
    var artistRoles: MutableMap<String, ArrayList<String>> = HashMap()
    // Just all the artists as strings
    var artists: ArrayList<String> = ArrayList()
    // All the contributors of the track
    var contributors: MutableMap<String, ArrayList<String>> = HashMap()

    var album: Album = Album()
    var date: DeezerDate? = null
    var lyrics: Lyrics? = null

    var duration: Int = 0 // in milliseconds
    var trackNumber: String? = null
    var discNumber: String? = null
    var bpm: Float? = null
    var explicit: Boolean? = null
    var isrc: String? = null
    var replayGain: Float? = null
    var isAvailableSomewhere: Boolean? = null
    var position: Int = 0
    var searched: Boolean = false

    // Computed
    var isLocal: Boolean = false

    init {
        artistRoles["Main"] = ArrayList()
    }

    fun parseAPI(trackAPI: JSONObject): Track {
        id = trackAPI.getString("id")
        title = trackAPI.getString("title")
        titleShort = trackAPI.getString("title_short")
        titleVersion = if (trackAPI.has("titleVersion"))
            trackAPI.getString("title_version")
        else ""
        artist.parseAPI(trackAPI.getJSONObject("artist"))
        duration = trackAPI.getInt("duration") * 1000
        explicit = trackAPI.getBoolean("explicit_lyrics")

        if (trackAPI.has("isrc")) isrc = trackAPI.getString("isrc")
        if (trackAPI.has("track_position")) trackNumber = trackAPI.getString("track_position")
        if (trackAPI.has("disk_number")) discNumber = trackAPI.getString("disk_number")
        if (trackAPI.has("bpm")) bpm = trackAPI.getString("bpm").toFloat() // Only on public API
        if (trackAPI.has("gain")) replayGain = trackAPI.getString("gain").toFloat()
        if (trackAPI.has("available_countries")) isAvailableSomewhere = trackAPI.getJSONArray("available_countries").length() > 0
        if (trackAPI.has("contributors")) {
            val data = trackAPI.getJSONArray("contributors")
            for (i in 0 until data.length()) {
                val artist = data.getJSONObject(i)
                val isVariousArtist = artist.getString("id") == "5080"
                val isMainArtist = artist.getString("role") == "Main"

                if (data.length() > 1 && isVariousArtist) continue

                val artistName = artist.getString("name")
                val artistRole = artist.getString("role")

                if (!artists.contains(artistName))
                    artists.add(artistName)

                if (isMainArtist || !artistRoles["Main"]!!.contains(artistName) && !isMainArtist){
                    if (!artistRoles.containsKey(artistRole))
                        artistRoles[artistRole] = ArrayList()
                    artistRoles[artistRole]!!.add(artistName)
                }
            }
        }
        if (trackAPI.has("release_date")) date = DeezerDate(trackAPI.getString("release_date"))
        if (trackAPI.has("md5_image")) album.pic.md5 = trackAPI.getString("md5_image")
        if (trackAPI.has("album")) {
            val thisAlbum = trackAPI.getJSONObject("album")
            album.id = thisAlbum.getString("id")
            album.title = thisAlbum.getString("title")
            album.pic.md5 = thisAlbum.getString("md5_image")
            album.date = DeezerDate(thisAlbum.getString("release_date"))
        }
        return this
    }

    fun parseGW(trackAPI: JSONObject): Track{
        id = trackAPI.getString("SNG_ID")
        album.id = trackAPI.getString("ALB_ID")
        album.pic.md5 = trackAPI.getString("ALB_PICTURE")
        album.title = trackAPI.getString("ALB_TITLE")
        artist.id = trackAPI.getString("ART_ID")
        artist.name = trackAPI.getString("ART_NAME")
        duration = trackAPI.getInt("DURATION") * 1000
        isrc = trackAPI.getString("ISRC")
        titleShort = trackAPI.getString("SNG_TITLE")
        title = titleShort!!
        trackToken = trackAPI.getString("TRACK_TOKEN")
        trackTokenExpiration = trackAPI.getInt("TRACK_TOKEN_EXPIRE")
        isLocal = id.toInt() < 0
        if (isLocal) return this

        val data = trackAPI.getJSONArray("ARTISTS")
        for (i in 0 until data.length()) {
            val artist = data.getJSONObject(i)
            val isVariousArtist = artist.getString("ART_ID") == "5080"
            val isMainArtist = artist.getString("ROLE_ID") == "0"

            if (data.length() > 1 && isVariousArtist) continue

            val artistName = artist.getString("ART_NAME")
            val artistRole = when (artist.getString("ROLE_ID")){
                "0" -> "Main"
                "5" -> "Featured"
                else -> "Other"
            }

            if (!artists.contains(artistName))
                artists.add(artistName)

            if (isMainArtist || !artistRoles["Main"]!!.contains(artistName) && !isMainArtist){
                if (!artistRoles.containsKey(artistRole))
                    artistRoles[artistRole] = ArrayList()
                artistRoles[artistRole]!!.add(artistName)
            }
        }
        discNumber = trackAPI.getString("DISK_NUMBER")
        explicit = isExplicit(trackAPI.getInt("EXPLICIT_LYRICS"))
        // GENRE_ID
        // LYRICS_ID
        date = DeezerDate(trackAPI.getString("PHYSICAL_RELEASE_DATE"))
        // contributors = SNG_CONTRIBUTORS
        trackNumber = trackAPI.getString("TRACK_NUMBER")
        titleVersion = if (trackAPI.has("VERSION"))
            trackAPI.getString("VERSION")
        else ""
        replayGain = trackAPI.getString("GAIN").toFloat()
        title = concatTitleVersion(titleShort?:"", titleVersion?:"")
        return this
    }

    fun parseGWPage(trackAPI: JSONObject): Track{
        val data = trackAPI.getJSONObject("DATA")
        parseGW(data)
        artist.pic.md5 = data.getString("ART_PICTURE")
        album.copyright = data.getString("COPYRIGHT")
        // isAvailableSomewhere = AVAILABLE_COUNTRIES

        if (trackAPI.has("LYRICS")){
            val lyricsData = trackAPI.getJSONObject("LYRICS")
        }
        if (trackAPI.has("ISRC")){
            val isrcData = trackAPI.getJSONObject("ISRC")
        }
        return this
    }

    fun cleanUp(){
        // Remove unwanted charaters in track name
        // Example: track/127793
        title = title.replace(Regex("\\s\\s+"), " ")

        // Make sure there is at least one artist
        // and that the first artist is the main one
        if (artists.contains(artist.name)){
            if (artists.indexOf(artist.name) != 0){
                artists.remove(artist.name)
            }
        }
        if (!artists.contains(artist.name)) artists.add(0, artist.name)
        if (artistRoles["Main"]!!.contains(artist.name)){
            if (artistRoles["Main"]!!.indexOf(artist.name) != 0){
                artistRoles["Main"]!!.remove(artist.name)
            }
        }
        if (!artistRoles["Main"]!!.contains(artist.name)) artistRoles["Main"]!!.add(0, artist.name)
        if (album.artists.contains(album.artist.name)){
            if (album.artists.indexOf(album.artist.name) != 0){
                album.artists.remove(album.artist.name)
            }
        }
        if (!album.artists.contains(album.artist.name)) album.artists.add(0, album.artist.name)
        if (album.artistRoles["Main"]!!.contains(album.artist.name)){
            if (album.artistRoles["Main"]!!.indexOf(album.artist.name) != 0){
                album.artistRoles["Main"]!!.remove(album.artist.name)
            }
        }
        if (!album.artistRoles["Main"]!!.contains(album.artist.name)) album.artistRoles["Main"]!!.add(0, album.artist.name)
    }

    fun restoreObject(trackAPI: JSONObject): Track {
        id = trackAPI.getString("id")
        if (trackAPI.has("title")) title = trackAPI.getString("title")
        if (trackAPI.has("titleShort")) titleShort = trackAPI.getString("titleShort")
        if (trackAPI.has("titleVersion")) titleVersion = trackAPI.getString("titleVersion")
        if (trackAPI.has("fallbackID")) fallbackID = trackAPI.getString("fallbackID")
        artist.restoreObject(trackAPI.getJSONObject("artist"))
        artistRoles = trackAPI.getJSONObject("artistRoles").toHashMap()
        contributors = trackAPI.getJSONObject("contributors").toHashMap()
        artists = trackAPI.getJSONArray("artists").toArrayList()
        album.restoreObject(trackAPI.getJSONObject("album"))
        if (trackAPI.has("date")) date = DeezerDate(trackAPI.getString("date"))
        if (trackAPI.has("lyrics")) lyrics = Lyrics().restoreObject(trackAPI.getJSONObject("lyrics"))
        duration = trackAPI.getInt("duration")
        if (trackAPI.has("trackNumber")) trackNumber = trackAPI.getString("trackNumber")
        if (trackAPI.has("discNumber")) discNumber = trackAPI.getString("discNumber")
        if (trackAPI.has("bpm")) bpm = trackAPI.getString("bpm").toFloat()
        if (trackAPI.has("explicit")) explicit = trackAPI.getBoolean("explicit")
        if (trackAPI.has("isrc")) isrc = trackAPI.getString("isrc")
        if (trackAPI.has("replayGain")) replayGain = trackAPI.getString("replayGain").toFloat()
        if (trackAPI.has("isAvailableSomewhere")) isAvailableSomewhere = trackAPI.getBoolean("isAvailableSomewhere")
        position = trackAPI.getInt("position")
        searched = trackAPI.getBoolean("searched")
        return this
    }

    fun toJSON(): JSONObject{
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("title", title)
        if (titleShort != null) obj.put("titleShort", titleShort)
        if (titleVersion != null) obj.put("titleVersion", titleVersion)
        if (fallbackID != null) obj.put("fallbackID", fallbackID)
        obj.put("artist", artist.toJSON())
        obj.put("artistRoles", artistRoles.toJSON())
        obj.put("contributors", contributors.toJSON())
        obj.put("artists", artists.toJSONArray())
        obj.put("album", album.toJSON())
        if (date != null) obj.put("date", date.toString())
        if (lyrics != null) obj.put("lyrics", lyrics!!.toJSON())
        obj.put("duration", duration)
        if (trackNumber != null) obj.put("trackNumber", trackNumber)
        if (discNumber != null) obj.put("discNumber", discNumber)
        if (bpm != null) obj.put("bpm", bpm)
        if (explicit != null) obj.put("explicit", explicit)
        if (isrc != null) obj.put("isrc", isrc)
        if (replayGain != null) obj.put("replayGain", replayGain)
        if (isAvailableSomewhere != null) obj.put("isAvailableSomewhere", isAvailableSomewhere)
        obj.put("position", position)
        obj.put("searched", searched)
        return obj
    }
}