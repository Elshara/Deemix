package com.nick80835.add

import android.util.Log
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.*
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

class Decryptor {
    // Misc crypto junk
    private val secret = "g4el5" + "8wc0z" + "vf9na1"
    private val urlCryptKeeper = ("jo6ae" + "y6hai" + "d2Teih").toByteArray()
    private val urlSecretKeySpec = SecretKeySpec(urlCryptKeeper, "AES")
    private val secretIvSpec = IvParameterSpec(byteArrayOf(0,1,2,3,4,5,6,7))

    // Mmm cookies
    private val cookieManager = CookieManager()

    // Blowjob
    fun blowJob(chunk: ByteArray, blowfishKey: String): ByteArray {
        val secretKeySpec = SecretKeySpec(blowfishKey.toByteArray(), "Blowfish")
        val thisTrackCipher = Cipher.getInstance("BLOWFISH/CBC/NoPadding")
        thisTrackCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, secretIvSpec)
        return thisTrackCipher.update(chunk)
    }

    // Hex and MD5 utils
    private fun bytesToHex(bytes: ByteArray): String {
        var hexString = ""

        for (byte in bytes) {
            hexString += String.format("%02X", byte)
        }

        return hexString
    }

    private fun String.toMd5Hex(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray(Charsets.ISO_8859_1))
        return bytesToHex(bytes).toLowerCase(Locale.ROOT)
    }

    // Download URL generation
    fun getDownloadUrl(trackId: Long, thisPUID: String, thisMediaVersion: String, format: Int): String {
        val assholeChar = "¤"
        val urlEndingPlain = "$thisPUID$assholeChar$format$assholeChar$trackId$assholeChar$thisMediaVersion"
        var urlEnding = urlEndingPlain.toMd5Hex()
        urlEnding += assholeChar + urlEndingPlain + assholeChar

        while ((urlEnding.length % 16) > 0) {
            urlEnding += "."
        }

        val inputArray = urlEnding.toByteArray(Charsets.ISO_8859_1)

        val thisUrlCipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        thisUrlCipher.init(Cipher.ENCRYPT_MODE, urlSecretKeySpec)

        val outputArray = ByteArray(thisUrlCipher.getOutputSize(inputArray.size))

        val ctLength = thisUrlCipher.update(inputArray, 0, inputArray.size, outputArray, 0)

        thisUrlCipher.doFinal(outputArray, ctLength)

        val outputArrayHex = bytesToHex(outputArray).toLowerCase(Locale.ROOT)

        return "https://e-cdns-proxy-" + thisPUID[0] + ".dzcdn.net/mobile/1/" + outputArrayHex
    }

    // SID fetching
    fun getSID(): String {
        val deezerUrl = URL("https://www.deezer.com")

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)

        with(deezerUrl.openConnection() as HttpsURLConnection) {
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

            connect()

            Log.d(TAG, "URL: $url")
            Log.d(TAG, "Response code: $responseCode")
        }

        return cookieManager.cookieStore.get(URI.create("https://www.deezer.com"))[0].toString()
    }

    // Track MD5 fetching
    fun getTrackSecrets(trackId: Long, thisSID: String): Map<*, *>? {
        val gatewayUrl = URL("https://api.deezer.com/1.0/gateway.php?api_key=4VCYIJUCDLOUELGD1V8WBVYBNVDYOXEWSLLZDONGBBDFVXTZJRXPR29JRLQFO6ZE&$thisSID&input=3&output=3&method=song_getData")

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

            Log.d(TAG, "URL: $url")
            Log.d(TAG, "Response code: $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val inputLine = it.readLine()

                val rawResultMap: Map<String, Any> = gson.fromJson(inputLine, object : TypeToken<Map<String, Any>>() {}.type)
                resultMap = rawResultMap["results"] as Map<*, *>

                Log.d(TAG, inputLine)
            }
        }

        return resultMap
    }

    // Blowfish key generation
    private fun bitwiseXor(firstVal: Char, secondVal: Char, thirdVal: Char): Char {
        return (BigInteger(byteArrayOf(firstVal.toByte())) xor
                BigInteger(byteArrayOf(secondVal.toByte())) xor
                BigInteger(byteArrayOf(thirdVal.toByte()))).toByte().toChar()
    }

    fun createBlowfishKey(trackId: Long): String {
        val trackMd5Hex = trackId.toString().toMd5Hex()
        var blowfishKey = ""

        Log.d(TAG, trackMd5Hex)

        for (i in 0..15) {
            val nextChar = bitwiseXor(trackMd5Hex[i], trackMd5Hex[i + 16], secret[i])
            blowfishKey += nextChar
        }

        Log.d(TAG, blowfishKey)

        return blowfishKey
    }
}
