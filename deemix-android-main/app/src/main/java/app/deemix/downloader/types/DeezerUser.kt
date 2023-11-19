package app.deemix.downloader.types

import org.json.JSONObject

class DeezerQuality(soundQuality: JSONObject) {
    var low: Boolean = false
    var standard: Boolean = false
    var high: Boolean = false
    var lossless: Boolean = false
    var reality: Boolean = false

    init {
        low = soundQuality.getBoolean("low")
        standard = soundQuality.getBoolean("standard")
        high = soundQuality.getBoolean("high")
        lossless = soundQuality.getBoolean("lossless")
        reality = soundQuality.getBoolean("reality")
    }

}

class DeezerUser(userData: JSONObject, child: JSONObject? = null) {
    var id: String
    var name: String
    var picture: String = ""
    var licenseToken: String
    var canStream: DeezerQuality
    var country: String
    var language: String
    var lovedTracksID: String

    init {
        if (child != null){
            id = child.getString("USER_ID")
            name = child.getString("BLOG_NAME")
            picture = child.getString("USER_PICTURE")
            lovedTracksID = child.getString("LOVEDTRACKS_ID")
        } else {
            id = userData.getString("USER_ID")
            name = userData.getString("BLOG_NAME")
            picture = userData.getString("USER_PICTURE")
            lovedTracksID = userData.getString("LOVEDTRACKS_ID")
        }
        licenseToken = userData.getJSONObject("OPTIONS").getString("license_token")
        canStream = DeezerQuality(userData.getJSONObject("OPTIONS").getJSONObject("web_sound_quality"))
        country = userData.getJSONObject("OPTIONS").getString("license_country")
        var lang = userData.getJSONObject("SETTING").getJSONObject("global").getString("language")
            .replace("[^0-9A-Za-z *,-.;=]".toRegex(), "")
        lang = if (lang.length > 2 && lang[2] == '-'){
            lang.substring(0, 5)
        } else {
            lang.substring(0, 2)
        }
        language = lang
    }
}