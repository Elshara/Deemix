package app.deemix.downloader.types

import org.json.JSONObject

class Artist {
    var id: String = "0"
    var name: String = ""
    var pic = DeezerPicture()
    var role: String = "Main"
    var shouldSave = true

    init {
        pic.type = "artist"
    }

    fun parseAPI(artistAPI: JSONObject): Artist{
        id = artistAPI.getString("id")
        name = artistAPI.getString("name")
        if (artistAPI.has("md5_image"))
            pic.md5 =artistAPI.getString("md5_image")
        else if (artistAPI.has("picture_small"))
            pic.md5 = artistAPI.getString("picture_small").substringAfter("/artist/").substringBefore("/")
        if (artistAPI.has("role")) role = artistAPI.getString("role")
        return this
    }

    fun parseGW(artistAPI: JSONObject): Artist{
        id = artistAPI.getString("ART_ID")
        name = artistAPI.getString("ART_NAME")
        pic.md5 = artistAPI.getString("ART_PICTURE")
        role = when (artistAPI.getString("ROLE_ID")){
            "0" -> "Main"
            "5" -> "Featured"
            else -> "Other"
        }
        return this
    }

    fun restoreObject(artistAPI: JSONObject): Artist {
        id = artistAPI.getString("id")
        name = artistAPI.getString("name")
        pic.restoreObject(artistAPI.getJSONObject("pic"))
        role = artistAPI.getString("role")
        return this
    }

    fun toJSON(): JSONObject{
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("pic", pic.toJSON())
        obj.put("role", role)
        return obj
    }
}