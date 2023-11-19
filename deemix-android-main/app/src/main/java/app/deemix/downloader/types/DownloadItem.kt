package app.deemix.downloader.types

import org.json.JSONArray
import org.json.JSONObject

open class DownloadItem {
    var type: String = ""
    var id: String = ""
    var bitrate: String = "3"
    var title: String = ""
    var artist: String = ""
    var cover: String = ""
    var explicit: Boolean = false
    open var size: Int = 0
    var downloaded: Int = 0
    var failed: Int = 0
    var progress: Int = -1
    var errors: ArrayList<String> = ArrayList()
    var uuid: String = ""
    var isCanceled: Boolean = false
    open var itemType: String = ""
    var status: String = ""

    constructor()

    constructor(obj: JSONObject){
        type = obj.getString("type")
        id = obj.getString("id")
        bitrate = obj.getString("bitrate")
        title = obj.getString("title")
        artist = obj.getString("artist")
        cover = obj.getString("cover")
        explicit = obj.getBoolean("explicit")
        size = obj.getInt("size")
        downloaded = obj.getInt("downloaded")
        failed = obj.getInt("failed")
        progress = obj.getInt("progress")
        for (i in 0 until obj.getJSONArray("errors").length()){
            errors.add(obj.getJSONArray("errors").getString(i))
        }
        uuid = obj.getString("uuid")
        isCanceled = obj.getBoolean("isCanceled")
        itemType = obj.getString("itemType")
        status = obj.getString("status")
    }

    constructor(obj: DownloadItem){
        type = obj.type
        id = obj.id
        bitrate = obj.bitrate
        title = obj.title
        artist = obj.artist
        cover = obj.cover
        explicit = obj.explicit
        size = obj.size
        downloaded = obj.downloaded
        failed = obj.failed
        progress = obj.progress
        errors = obj.errors
        uuid = obj.uuid
        isCanceled = obj.isCanceled
        itemType = obj.itemType
        status = obj.status
    }

    fun generateUUID(){
        uuid = "${type}_${id}_${bitrate}"
    }

    open fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("type", type)
        obj.put("id", id)
        obj.put("bitrate", bitrate)
        obj.put("title", title)
        obj.put("artist", artist)
        obj.put("cover", cover)
        obj.put("explicit", explicit)
        obj.put("size", size)
        obj.put("downloaded", downloaded)
        obj.put("failed", failed)
        obj.put("progress", progress)
        obj.put("errors", JSONArray(errors))
        obj.put("uuid", uuid)
        obj.put("isCanceled", isCanceled)
        obj.put("itemType", itemType)
        obj.put("status", status)
        return obj
    }

    override fun toString(): String {
        return toJSON().toString(2)
    }

    fun minimalClone(): DownloadItem {
        return DownloadItem(this)
    }
}

class Single : DownloadItem {
    override var size = 1
    override var itemType = "single"
    var single: JSONObject = JSONObject()

    constructor()

    constructor(obj: JSONObject): super(obj){
        single = obj.getJSONObject("single")
    }

    constructor(obj: Single): super(obj){
        single = obj.single
    }

    override fun toJSON(): JSONObject {
        val obj = super.toJSON()
        obj.put("single", single)
        return obj
    }
}

class Collection : DownloadItem {
    override var itemType = "collection"
    var collection: JSONObject = JSONObject()

    constructor()

    constructor(obj: JSONObject): super(obj){
        collection = obj.getJSONObject("collection")
    }

    constructor(obj: Collection): super(obj){
        collection = obj.collection
    }

    override fun toJSON(): JSONObject {
        val obj = super.toJSON()
        obj.put("collection", collection)
        return obj
    }
}