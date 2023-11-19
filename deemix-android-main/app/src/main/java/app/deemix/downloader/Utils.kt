package app.deemix.downloader

import android.webkit.URLUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Utils {
    private const val secret = "g4el58wc0zvf9na1"
    private val secretIvSpec = IvParameterSpec(byteArrayOf(0,1,2,3,4,5,6,7))

    @Throws(IOException::class)
    fun getFinalURL(url: String): String {
        val con: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        con.instanceFollowRedirects = false
        con.connect()
        con.inputStream
        if (con.responseCode == HttpURLConnection.HTTP_MOVED_PERM || con.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            val redirectUrl: String = con.getHeaderField("Location")
            if (URLUtil.isValidUrl(redirectUrl)) return getFinalURL(redirectUrl)
        }
        return url
    }

    private fun bitwiseXor(firstVal: Char, secondVal: Char, thirdVal: Char): Char {
        return (BigInteger(byteArrayOf(firstVal.code.toByte())) xor
                BigInteger(byteArrayOf(secondVal.code.toByte())) xor
                BigInteger(byteArrayOf(thirdVal.code.toByte()))).toByte().toInt().toChar()
    }

    fun createBlowfishKey(trackId: String): String {
        val trackMd5Hex = trackId.toMD5()
        var blowfishKey = ""

        for (i in 0..15) {
            val nextChar = bitwiseXor(trackMd5Hex[i], trackMd5Hex[i + 16], secret[i])
            blowfishKey += nextChar
        }

        return blowfishKey
    }

    fun decryptBlowfish(chunk: ByteArray, blowfishKey: String): ByteArray {
        val secretKeySpec = SecretKeySpec(blowfishKey.toByteArray(), "Blowfish")
        val thisTrackCipher = Cipher.getInstance("BLOWFISH/CBC/NoPadding")
        thisTrackCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, secretIvSpec)
        return thisTrackCipher.update(chunk)
    }

    fun concatTitleVersion(titleShort: String, titleVersion: String): String {
        var thisTitle = titleShort.trim()
        val thisVersion = titleVersion.trim()
        if (!thisTitle.contains(thisVersion)) thisTitle += " $thisVersion"
        return thisTitle
    }

    fun isExplicit(explicitLyrics: Int): Boolean{
        return arrayOf(1, 4).contains(explicitLyrics)
    }
}

private fun bytesToHex(bytes: ByteArray): String {
    var hexString = ""
    for (byte in bytes) {
        hexString += String.format("%02X", byte)
    }
    return hexString
}

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray(Charsets.ISO_8859_1))
    return bytesToHex(bytes).lowercase()
}

fun <T> ArrayList<T>.toJSONArray(): JSONArray {
    val result = JSONArray()
    for (value in this){
        result.put(value)
    }
    return result
}

fun <T> JSONArray.toArrayList(): ArrayList<T>{
    val result = ArrayList<T>()
    for (i in 0 until this.length()) {
        result.add(this.get(i) as T)
    }
    return result
}

fun <T> MutableMap<String, ArrayList<T>>.toJSON(): JSONObject{
    val result = JSONObject()
    for (key in this.keys){
        result.put(key, this[key]!!.toJSONArray())
    }
    return result
}

fun <T> JSONObject.toHashMap(): MutableMap<String, ArrayList<T>>{
    val result: MutableMap<String, ArrayList<T>> = HashMap()
    for (key in this.keys()){
        result[key] = this.getJSONArray(key).toArrayList()
    }
    return result
}