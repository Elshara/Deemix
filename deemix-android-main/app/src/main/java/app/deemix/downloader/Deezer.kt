package app.deemix.downloader

import android.os.Build
import app.deemix.downloader.types.DeezerUser
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.*
import javax.net.ssl.HttpsURLConnection

class Deezer {
    private var cookieJar: CookieManager = CookieManager()
    private var httpHeaders: MutableMap<String, String> = HashMap()

    var isLoggedIn = false
    var currentUser: DeezerUser? = null
    private var childs: ArrayList<DeezerUser> = ArrayList()
    private var selectedAccount: Int = 0

    private var apiToken: String? = null

    init {
        httpHeaders["User-Agent"] = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
    }

    fun apiCall(method: String): JSONObject {
        var result: JSONObject
        val request = URL("https://api.deezer.com/$method")
        with(request.openConnection() as HttpsURLConnection) {
            requestMethod = "GET"
            doInput = true

            // Set Headers
            for (key in httpHeaders.keys){
                setRequestProperty(key, httpHeaders[key])
            }

            // Set Cookies
            if (cookieJar.cookieStore.cookies.size > 0) {
                var cookieText = ""
                for (cookie in cookieJar.cookieStore.cookies) {
                    cookieText += "${cookie.name}=${cookie.value}; "
                }
                setRequestProperty("Cookie", cookieText)
            }

            // Save set cookies
            val thisHeaderFields: Map<String, List<String>> = headerFields
            val cookiesHeader = thisHeaderFields["Set-Cookie"]
            if (cookiesHeader != null) {
                for (cookie in cookiesHeader) {
                    val thisCookie = HttpCookie.parse(cookie)
                    cookieJar.cookieStore.add(URI("https://www.deezer.com"), thisCookie[0])
                }
            }

            BufferedReader(InputStreamReader(inputStream)).use {
                result = JSONTokener(it.readText()).nextValue() as JSONObject
            }
        }
        if (result.has("error")){
            val error = result.getJSONObject("error")
            if (error.has("code")){
                val errorCode = error.getInt("code")
                if (arrayOf(4, 700).contains(errorCode)){
                    return apiCall(method)
                }
            }
            throw Exception(error.toString(0).replace("\n", ""))
        }
        return result
    }

    fun apiCallGW(method: String, args:String? = null): JSONObject{
        if (apiToken == null && method != "deezer.getUserData") apiToken = getToken()
        var result: JSONObject

        val request = URL("https://www.deezer.com/ajax/gw-light.php?api_version=1.0&api_token=${ if (method == "deezer.getUserData") "null" else apiToken }&input=3&method=$method")
        with(request.openConnection() as HttpsURLConnection) {
            requestMethod = "POST"
            doInput = true
            doOutput = args != null

            // Set Headers
            for (key in httpHeaders.keys){
                setRequestProperty(key, httpHeaders[key])
            }

            // Set Cookies
            if (cookieJar.cookieStore.cookies.size > 0) {
                var cookieText = ""
                for (cookie in cookieJar.cookieStore.cookies) {
                    cookieText += "${cookie.name}=${cookie.value}; "
                }
                setRequestProperty("Cookie", cookieText)
            }

            // Set post body
            if (args != null){
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                outputStream.write(args.toByteArray())
                outputStream.flush()
            }

            // Save set cookies
            val thisHeaderFields: Map<String, List<String>> = headerFields
            val cookiesHeader = thisHeaderFields["Set-Cookie"]
            if (cookiesHeader != null) {
                for (cookie in cookiesHeader) {
                    val thisCookie = HttpCookie.parse(cookie)
                    cookieJar.cookieStore.add(URI("https://www.deezer.com"), thisCookie[0])
                }
            }

            BufferedReader(InputStreamReader(inputStream)).use {
                result = JSONTokener(it.readText()).nextValue() as JSONObject
            }
        }
        if (result.has("error") && result.get("error").toString() != "[]"){
            val error = result.getJSONObject("error").toString()
            if (
                !(error != "{\"GATEWAY_ERROR\":\"invalid api token\"}" && error != "{\"VALID_TOKEN_REQUIRED\":\"Invalid CSRF token\"}")
            ){
                apiToken = getToken()
                return apiCallGW(method, args)
            }
            if (result.has("payload") && result.getJSONObject("payload").has("FALLBACK")){
                val thisArgs = JSONObject(args)
                val fallback = result.getJSONObject("payload").getJSONObject("FALLBACK")
                for (key in fallback.keys()){
                    thisArgs.put(key, fallback.get(key))
                }
                return apiCallGW(method, thisArgs.toString(0).replace("\n", ""))
            }
            throw Exception(result.getJSONObject("error").toString(0).replace("\n", ""))
        }
        val unknownResult = result.get("results")
        if (unknownResult is JSONObject) result = unknownResult
        else {
            result = JSONObject()
            result.put("data", unknownResult)
        }
        if (apiToken == null && method == "deezer.getUserData") apiToken = result.getString("checkForm")
        return result
    }

    private fun getToken(): String{
        val tokenData = apiCallGW("deezer.getUserData")
        return tokenData.getString("checkForm")
    }

    fun getAccessToken(email: String, password: String): String?{
        val clientId = "172365"
        val clientSecret = "fb0bec7ccc063dab0417eb7b0d847f34"

        var accessToken: String? = null
        val hashedPassword = password.toMD5()
        val hash = "$clientId$email$hashedPassword$clientSecret".toMD5()
        var response: JSONObject
        val request = URL("https://api.deezer.com/auth/token?app_id=$clientId&login=$email&password=$hashedPassword&hash=$hash")
        with(request.openConnection() as HttpsURLConnection) {
            setRequestProperty("User-Agent", httpHeaders["User-Agent"])

            BufferedReader(InputStreamReader(inputStream)).use {
                response = JSONTokener(it.readText()).nextValue() as JSONObject
            }
        }

        if (response.has("access_token")) accessToken = response.getString("access_token")
        if (accessToken == null) logout()
        return accessToken
    }

    fun getArlFromAccessToken(accessToken: String): String? {
        val request = URL("https://api.deezer.com/platform/generic/track/3135556")
        with(request.openConnection() as HttpsURLConnection) {
            setRequestProperty("User-Agent", httpHeaders["User-Agent"])
            setRequestProperty("Authorization", "Bearer $accessToken")

            // Save set Cookies
            val thisHeaderFields: Map<String, List<String>> = headerFields
            val cookiesHeader = thisHeaderFields["Set-Cookie"]
            if (cookiesHeader != null) {
                for (cookie in cookiesHeader) {
                    val thisCookie = HttpCookie.parse(cookie)
                    cookieJar.cookieStore.add(URI("https://www.deezer.com"), thisCookie[0])
                }
            }

        }
        return apiCallGW("user.getArl").getString("data")
    }

    fun login(arl: String): Boolean{
        val thisArl = arl.trim()
        val arlCookie = HttpCookie("arl", thisArl)
        arlCookie.domain = ".deezer.com"
        arlCookie.path = "/"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            arlCookie.isHttpOnly = true
        }
        cookieJar.cookieStore.add(URI("https://www.deezer.com"), arlCookie)

        var userData = apiCallGW("deezer.getUserData")
        if (userData.length() == 0) {
            isLoggedIn = false
            return isLoggedIn
        }
        userData = userData.getJSONObject("USER")
        if (userData.getInt("USER_ID") == 0){
            println("USER_ID = 0")
            println(cookieJar.cookieStore.cookies)
            isLoggedIn = false
            return isLoggedIn
        }

        postLogin(userData)
        changeAccount(0)
        isLoggedIn = true
        return isLoggedIn
    }

    fun loginViaEmail(email: String, password: String){
        val accessToken = SharedObjects.dz.getAccessToken(email, password) ?: return
        val arl = SharedObjects.dz.getArlFromAccessToken(accessToken) ?: return
        login(arl)
    }

    private fun changeAccount(childPos: Int): Pair<DeezerUser, Int> {
        var thisChildPos = childPos
        if (childs.size-1 < thisChildPos) thisChildPos = 0
        currentUser = childs[thisChildPos]
        selectedAccount = thisChildPos
        httpHeaders["Accept-Language"] = currentUser!!.language

        return Pair(currentUser!!, thisChildPos)
    }

    private fun postLogin(userData: JSONObject) {
        childs.clear()
        val isFamily = userData.getJSONObject("MULTI_ACCOUNT").getBoolean("ENABLED") && ! userData.getJSONObject("MULTI_ACCOUNT").getBoolean("IS_SUB_ACCOUNT")
        if (isFamily){
            val deezerChilds = apiCallGW("deezer.getChildAccounts")
            for (i in 0 until deezerChilds.length()){
                val child = deezerChilds.getJSONObject(i.toString())
                childs.add(DeezerUser(userData, child))
            }
        } else {
            childs.add(DeezerUser(userData))
        }
    }

    fun logout() {
        cookieJar = CookieManager()
        httpHeaders["Accept-Language"] = ""
        childs.clear()
        currentUser = null
        isLoggedIn = false
    }

    fun getTrackURL(trackToken: String, format: String): String?{
        if (!isLoggedIn) return null
        if (format == "MP4_RA" && !currentUser!!.canStream.reality) return null
        if (format == "FLAC" && !currentUser!!.canStream.lossless) return null
        if (format == "MP3_320" && !currentUser!!.canStream.high) return null
        if (format == "MP3_128" && !currentUser!!.canStream.standard) return null

        var result: JSONObject
        val request = URL("https://media.deezer.com/v1/get_url")
        with(request.openConnection() as HttpsURLConnection) {
            requestMethod = "POST"
            doInput = true
            doOutput = true

            // Set Headers
            for (key in httpHeaders.keys){
                setRequestProperty(key, httpHeaders[key])
            }

            // Set Cookies
            if (cookieJar.cookieStore.cookies.size > 0) {
                var cookieText = ""
                for (cookie in cookieJar.cookieStore.cookies) {
                    cookieText += "${cookie.name}=${cookie.value}; "
                }
                setRequestProperty("Cookie", cookieText)
            }

            // Set post body
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            outputStream.write("""
                {
                    "license_token": "${currentUser!!.licenseToken}",
                    "media": [{"type": "FULL", "formats": [{ "cipher": "BF_CBC_STRIPE", "format": "$format"}]}],
                    "track_tokens": ["$trackToken"]
                }
            """.trimIndent().toByteArray())
            outputStream.flush()

            // Save set cookies
            val thisHeaderFields: Map<String, List<String>> = headerFields
            val cookiesHeader = thisHeaderFields["Set-Cookie"]
            if (cookiesHeader != null) {
                for (cookie in cookiesHeader) {
                    val thisCookie = HttpCookie.parse(cookie)
                    cookieJar.cookieStore.add(URI("https://www.deezer.com"), thisCookie[0])
                }
            }

            BufferedReader(InputStreamReader(inputStream)).use {
                result = JSONTokener(it.readText()).nextValue() as JSONObject
            }
        }

        if (result.has("data")){
            for (i in 0 until result.getJSONArray("data").length()){
                val data: JSONObject = result.getJSONArray("data").getJSONObject(i)
                if (data.has("errors")){
                    println(data.get("errors").toString())
                    return null
                }
                return if (data.has("media") && data.getJSONArray("media").length() > 0){
                    data.getJSONArray("media").getJSONObject(0).getJSONArray("sources").getJSONObject(0).getString("url")
                } else {
                    null
                }
            }
        }
        return null
    }
}