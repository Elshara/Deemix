package app.deemix.downloader

import app.deemix.downloader.types.DownloadItem

object SharedObjects {
    var dz = Deezer()

    var fullQueue: ArrayList<String> = ArrayList()
    var queue: MutableMap<String, DownloadItem> = HashMap()
}