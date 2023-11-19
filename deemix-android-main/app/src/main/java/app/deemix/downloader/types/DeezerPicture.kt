package app.deemix.downloader.types

import org.json.JSONObject

class DeezerPicture {
    var md5: String = ""
    var type: String = ""

    fun getURL(size: Int, format: String): String{
        val url = "https://e-cdns-images.dzcdn.net/images/$type/$md5/${size}x${size}"

        if (format.startsWith("jpg")){
            var quality = 80
            if (format.contains("-")) quality = format.substring(4).toInt()
            return "$url-000000-$quality-0-0.jpg"
        }
        if (format == "png"){
            return "$url-none-100-0-0.png"
        }
        return "$url.jpg"
    }

    fun restoreObject(picture: JSONObject): DeezerPicture{
        md5 = picture.getString("md5")
        type = picture.getString("type")
        return this
    }

    fun toJSON(): JSONObject{
        val obj = JSONObject()
        obj.put("md5", md5)
        obj.put("type", type)
        return obj
    }
}