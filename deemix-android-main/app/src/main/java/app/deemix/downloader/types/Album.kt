package app.deemix.downloader.types

import app.deemix.downloader.Utils.concatTitleVersion
import app.deemix.downloader.Utils.isExplicit
import app.deemix.downloader.toArrayList
import app.deemix.downloader.toHashMap
import app.deemix.downloader.toJSON
import app.deemix.downloader.toJSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Album {
    var id = "0"
    var title = ""
    var pic = DeezerPicture()
    var artist = Artist()
    var artistRoles: MutableMap<String, ArrayList<String>> = HashMap()
    var artists: ArrayList<String> = ArrayList()
    var date: DeezerDate? = null
    var trackTotal: String? = null
    var discTotal: String? = null
    var embeddedCoverPath: String? = null
    var embeddedCoverURL: String? = null
    var explicit: Boolean? = null
    var genres: ArrayList<String>? = null
    var barcode: String? = null
    var label: String? = null
    var copyright: String? = null
    var recordType: String = "album"
    var bitrate: String? = null
    var rootArtist: Artist? = null
    var variousArtists: Artist? = null


    init {
        pic.type = "cover"
    }

    fun parseAPI(albumAPI: JSONObject): Album {
        id = albumAPI.getString("id")
        title = albumAPI.getString("title")
        pic.md5 = albumAPI.getString("md5_image")
        artist.parseAPI(albumAPI.getJSONObject("artist"))
        val data = albumAPI.getJSONArray("contributors")
        for (i in 0 until data.length()) {
            val artist = data.getJSONObject(i)
            val isVariousArtist = artist.getString("id") == "5080"
            val isMainArtist = artist.getString("role") == "Main"

            if (isVariousArtist) {
                variousArtists = Artist().parseAPI(artist)
                continue
            }

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
        date = DeezerDate(albumAPI.getString("release_date"))
        trackTotal = albumAPI.getString("nb_tracks")
        explicit = albumAPI.getBoolean("explicit_lyrics")
        val thisGenres = albumAPI.getJSONObject("genres").getJSONArray("data")
        genres = ArrayList()
        for (i in 0 until thisGenres.length()) {
            val genre = thisGenres.getJSONObject(i).getString("name")
            if (!genres!!.contains(genre)) genres!!.add(genre)
        }
        barcode = albumAPI.getString("upc")
        label = albumAPI.getString("label")
        recordType = albumAPI.getString("record_type")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return this
    }

    fun parseGW(albumAPI: JSONObject): Album {
        id = albumAPI.getString("ALB_ID")
        title = albumAPI.getString("ALB_TITLE").trim()
        pic.md5 = albumAPI.getString("ALB_PICTURE")
        artist.id = albumAPI.getString("ART_ID")
        artist.name = albumAPI.getString("ART_NAME")
        date = DeezerDate(albumAPI.getString("PHYSICAL_RELEASE_DATE"))
        trackTotal = albumAPI.getString("NUMBER_TRACK")
        discTotal = albumAPI.getString("NUMBER_DISK")
        explicit = isExplicit(albumAPI.getJSONObject("EXPLICIT_ALBUM_CONTENT").getInt("EXPLICIT_LYRICS_STATUS"))
        label = albumAPI.getString("LABEL_NAME")
        copyright = albumAPI.getString("COPYRIGHT")
        return this
    }

    fun parseGWPage(albumAPI: JSONObject): Album{
        id = albumAPI.getString("ALB_ID")
        title = albumAPI.getString("ALB_TITLE").trim()
        val version = albumAPI.getString("VERSION").trim()
        title = concatTitleVersion(title, version)
        pic.md5 = albumAPI.getString("ALB_PICTURE")
        artist.id = albumAPI.getString("ART_ID")
        artist.name = albumAPI.getString("ART_NAME")
        val data = albumAPI.getJSONArray("ARTISTS")
        for (i in 0 until data.length()) {
            val artist = data.getJSONObject(i)
            val isVariousArtist = artist.getString("ART_ID") == "5080"
            val isMainArtist = artist.getString("ROLE") == "0"

            if (isVariousArtist) {
                variousArtists = Artist().parseGW(artist)
                continue
            }

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
        date = DeezerDate(albumAPI.getString("PHYSICAL_RELEASE_DATE"))
        explicit = isExplicit(albumAPI.getJSONObject("EXPLICIT_ALBUM_CONTENT").getInt("EXPLICIT_LYRICS_STATUS"))
        barcode = albumAPI.getString("UPC")
        label = albumAPI.getString("LABEL_NAME")
        return this
    }

    fun restoreObject(albumAPI: JSONObject): Album{
        id = albumAPI.getString("id")
        title = albumAPI.getString("title")
        pic.restoreObject(albumAPI.getJSONObject("pic"))
        artist.restoreObject(albumAPI.getJSONObject("artist"))
        artistRoles = albumAPI.getJSONObject("artistRoles").toHashMap()
        artists = albumAPI.getJSONArray("artists").toArrayList()
        if (albumAPI.has("date")) date = DeezerDate(albumAPI.getString("date"))
        if (albumAPI.has("trackTotal")) trackTotal = albumAPI.getString("trackTotal")
        if (albumAPI.has("discTotal")) discTotal = albumAPI.getString("discTotal")
        if (albumAPI.has("embeddedCoverPath")) embeddedCoverPath = albumAPI.getString("embeddedCoverPath")
        if (albumAPI.has("embeddedCoverURL")) embeddedCoverURL = albumAPI.getString("embeddedCoverURL")
        if (albumAPI.has("explicit")) explicit = albumAPI.getBoolean("explicit")
        if (albumAPI.has("genres")) genres = albumAPI.getJSONArray("genres").toArrayList()
        if (albumAPI.has("barcode")) barcode = albumAPI.getString("barcode")
        if (albumAPI.has("label")) label = albumAPI.getString("label")
        if (albumAPI.has("copyright")) copyright = albumAPI.getString("copyright")
        if (albumAPI.has("bitrate")) bitrate = albumAPI.getString("bitrate")
        if (albumAPI.has("rootArtist")) rootArtist = Artist().restoreObject(albumAPI.getJSONObject("rootArtist"))
        recordType = albumAPI.getString("recordType")
        return this
    }

    fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("title", title)
        obj.put("pic", pic.toJSON())
        obj.put("artist", artist.toJSON())
        obj.put("artistRoles", artistRoles.toJSON())
        obj.put("artists", artists.toJSONArray())
        if (date != null) obj.put("date", date)
        if (trackTotal != null) obj.put("trackTotal", trackTotal)
        if (discTotal != null) obj.put("discTotal", discTotal)
        if (embeddedCoverPath != null) obj.put("embeddedCoverPath", embeddedCoverPath)
        if (embeddedCoverURL != null) obj.put("embeddedCoverURL", embeddedCoverURL)
        if (explicit != null) obj.put("explicit", explicit)
        if (genres != null) obj.put("genres", genres!!.toJSONArray())
        if (barcode != null) obj.put("barcode", barcode)
        if (label != null) obj.put("label", label)
        if (copyright != null) obj.put("copyright", copyright)
        if (bitrate != null) obj.put("bitrate", bitrate)
        if (rootArtist != null) obj.put("rootArtist", rootArtist)
        obj.put("recordType", recordType)
        return obj
    }
}