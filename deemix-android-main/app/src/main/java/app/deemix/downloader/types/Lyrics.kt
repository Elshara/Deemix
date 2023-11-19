package app.deemix.downloader.types

import app.deemix.downloader.toArrayList
import org.json.JSONObject

class Lyrics {
    var id: String = "0"
    var sync: String = ""
    var unsync: String = ""
    var syncID3: ArrayList< Pair<String, Int>> = ArrayList()

    fun restoreObject(lyricsAPI: JSONObject): Lyrics {
        id = lyricsAPI.getString("id")
        sync = lyricsAPI.getString("sync")
        unsync = lyricsAPI.getString("unsync")
        //syncID3 = lyricsAPI.getJSONArray("syncID3").toArrayList()
        return this
    }

    fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("sync", sync)
        obj.put("unsync", unsync)
        //obj.put("syncID3", syncID3) // TODO: toJSON
        return obj
    }
}