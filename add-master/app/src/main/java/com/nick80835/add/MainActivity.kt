package com.nick80835.add

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import ealvatag.tag.TagOptionSingleton
import kotlinx.android.synthetic.main.album_popup_layout.view.*
import kotlinx.android.synthetic.main.artist_popup_layout.view.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.settings_popup_layout.view.*
import kotlinx.android.synthetic.main.track_popup_layout.view.*
import kotlinx.android.synthetic.main.track_popup_layout.view.AlbumArtView
import kotlinx.android.synthetic.main.track_popup_layout.view.CloseButton
import kotlinx.android.synthetic.main.track_popup_layout.view.PrimaryTitleView
import kotlinx.android.synthetic.main.track_popup_layout.view.SecondaryTitleView
import kotlinx.android.synthetic.main.track_popup_layout.view.TertiaryTitleView
import kotlinx.android.synthetic.main.track_popup_layout.view.extraInfoView
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.net.URLEncoder
import kotlin.system.exitProcess

const val TAG = "ADD"

const val deezerApiSearch = "https://api.deezer.com/search"
const val deezerApiTrack = "https://api.deezer.com/track/"
const val deezerApiAlbum = "https://api.deezer.com/album/"
const val deezerApiArtist = "https://api.deezer.com/artist/"
const val deezerApiGenre = "https://api.deezer.com/genre/"
const val deezerApiInfos = "https://api.deezer.com/infos"

lateinit var defaultAlbumArt: Drawable
var cardIdCounter = 0

val gson = Gson()

class MainActivity : AppCompatActivity() {
    private lateinit var resultRecycler: RecyclerView
    private lateinit var resultRecyclerLayoutManager: RecyclerView.LayoutManager
    private lateinit var itemAdapter: ItemAdapter<AbstractItem<ResultItem.ViewHolder>>
    private lateinit var imm: InputMethodManager
    private var searchType = "track"
    private var searchThread: DisposableHandle? = null
    private var previewThread: DisposableHandle? = null
    private var isInContentList = false
    private var sharedPreferences: SharedPreferences? = null
    private var serviceIntent: Intent? = null
    private var serviceMessenger: Messenger? = null
    private var activityMessenger: Messenger = Messenger(IncomingHandler(this))
    private var serviceConnection: AIDServiceConnection = AIDServiceConnection()
    private val backend = BackendFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)

        defaultAlbumArt = ContextCompat.getDrawable(applicationContext, R.drawable.placeholder_disc)!!

        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setupSharedPreferences()
        setupSearchBar()
        setupResultRecycler()
        setupBottomNav()
        checkCountryAvailability()

        BackButton.setOnClickListener {
            hideContentList()
        }

        serviceIntent = Intent(applicationContext, AIDService::class.java)
        serviceIntent!!.putExtra("Messenger", activityMessenger)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, 0)

        TagOptionSingleton.getInstance().isAndroid = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Storage access denied, show a popup for that
                showStoragePopup()
            }
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isInContentList) {
            hideContentList()
            return
        } else if (SearchBar.hasFocus()) {
            SearchBar.clearFocus()
        }

        super.onBackPressed()
    }

    private fun checkCountryAvailability() {
        lateinit var infosHolder: String
        var countryCheckThread: DisposableHandle? = null

        countryCheckThread = GlobalScope.launch {
            try {
                infosHolder = URL(deezerApiInfos).readText()
            } catch (e: Exception) {
                countryCheckThread?.dispose()
                showConnectErrorPopup()
            }
        }.invokeOnCompletion {
            val resultMap: Map<String, Any> = gson.fromJson(infosHolder, object : TypeToken<Map<String, Any>>() {}.type)
            if (BuildConfig.DEBUG) Log.d(TAG, resultMap.toString())

            if (resultMap["open"] == false) {
                showCountryWarningPopup(resultMap["country"] as String)
            }
        }
    }

    private fun setupSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private fun setIntPref(name: String, value: Int) {
        sharedPreferences!!.edit().apply {
            putInt(name, value)
            apply()
        }
    }

    private fun setBoolPref(name: String, value: Boolean) {
        sharedPreferences!!.edit().apply {
            putBoolean(name, value)
            apply()
        }
    }

    private fun setStringPref(name: String, value: String) {
        sharedPreferences!!.edit().apply {
            putString(name, value)
            apply()
        }
    }

    private fun setupSearchBar() {
        updateSearchHint(searchType)

        SearchButton.setOnClickListener {
            triggerSearch()
        }

        SearchBar.setOnEditorActionListener { _, actionId, _ ->
            var handled = false

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch()
                handled = true
            }
            handled
        }
    }

    private fun setupResultRecycler() {
        itemAdapter = ItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)

        resultRecycler = ResultRecycler
        resultRecyclerLayoutManager = LinearLayoutManager(this)

        fastAdapter.onClickListener = { view, adapter, item, position ->
            // Handle click here
            handleCardClick(view!!, adapter, item, position)

            true
        }

        runOnUiThread {
            resultRecycler.layoutManager = resultRecyclerLayoutManager
            resultRecycler.adapter = fastAdapter
        }
    }

    private fun setupBottomNav() {
        MainBottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.track_nav -> {
                    searchType = "track"
                    updateSearchHint(searchType)
                    triggerSearch()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.album_nav -> {
                    searchType = "album"
                    updateSearchHint(searchType)
                    triggerSearch()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.artist_nav -> {
                    searchType = "artist"
                    updateSearchHint(searchType)
                    triggerSearch()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.playlist_nav -> {
                    searchType = "playlist"
                    updateSearchHint(searchType)
                    triggerSearch()

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.settings_nav -> {
                    showSettingsPopup()
                    return@setOnNavigationItemSelectedListener false
                }
            }

            false
        }
    }

    private fun triggerSearch() {
        SearchBar.hideKeyboard()
        SearchBar.clearFocus()
        itemAdapter.clear()

        if (SearchBar.editableText.toString().isNotBlank()) {
            resultsLoadingProgressBar.unhide()
            searchDeezer(SearchBar.editableText.toString())
        }
    }

    private fun searchDeezer(userQuery: String) {
        var thisQueryUrl: String?
        var tempSearchType: String? = null
        var urlSearch = false

        if (URLUtil.isValidUrl(userQuery)) {
            if (userQuery.contains("www.deezer.com")) {
                urlSearch = true

                thisQueryUrl = userQuery.replace("http://", "https://")
                thisQueryUrl = thisQueryUrl.replace("www", "api")

                when {
                    "/track/" in thisQueryUrl -> tempSearchType = "track"
                    "/album/" in thisQueryUrl -> tempSearchType = "album"
                    "/artist/" in thisQueryUrl -> tempSearchType = "artist"
                    "/playlist/" in thisQueryUrl -> tempSearchType = "playlist"
                }

                thisQueryUrl = thisQueryUrl.replace(thisQueryUrl.substringAfter(".com").substringBefore("/$tempSearchType"), "")
            } else {
                resultsLoadingProgressBar.hide()
                return
            }
        } else {
            val queryString = URLEncoder.encode(userQuery, "UTF-8")

            val selectedSearchLimit = when (sharedPreferences!!.getInt("result_limit", 1)) {
                0 -> 25
                1 -> 50
                2 -> 75
                3 -> 100
                else -> 50
            }

            thisQueryUrl = "$deezerApiSearch/$searchType?limit=$selectedSearchLimit&q=$queryString"
        }

        searchThread?.dispose()

        var resultHolder: String? = null

        searchThread = GlobalScope.launch {
            try {
                resultHolder = URL(thisQueryUrl).readText()
                resultsLoadingProgressBar.hide()
            } catch (e: Exception) {
                searchThread?.dispose()
                showConnectErrorPopup()
                resultsLoadingProgressBar.hide()
            }
        }.invokeOnCompletion {
            if (resultHolder.isNullOrBlank()) return@invokeOnCompletion

            val normalResultMap: ArrayList<*>

            normalResultMap = if (!urlSearch) {
                val resultMap: Map<String, Any> = gson.fromJson(resultHolder, object : TypeToken<Map<String, Any>>() {}.type)
                resultMap["data"] as ArrayList<*>
            } else {
                val thisResultMap: Map<String, Any> = gson.fromJson(resultHolder, object : TypeToken<Map<String, Any>>() {}.type)
                arrayListOf(thisResultMap)
            }

            cardIdCounter = 0

            when (tempSearchType ?: searchType) {
                "track" -> searchTrack(normalResultMap)
                "album" -> searchAlbum(normalResultMap)
                "artist" -> searchArtist(normalResultMap)
                "playlist" -> searchPlaylist(normalResultMap)
            }
        }
    }

    private fun addResultItem(thisItemData: TrackData) {
        runOnUiThread {
            itemAdapter.add(ResultItem(thisItemData, this))
        }
    }

    private fun searchTrack(normalResultList: ArrayList<*>, albumOverride: LinkedTreeMap<*, *>? = null) {
        if (BuildConfig.DEBUG) Log.d(TAG, normalResultList.toString())

        for (i in normalResultList) {
            val thisResult = i as LinkedTreeMap<*, *>

            // track info
            val primaryName = thisResult["title"] as String?
            val previewUrl = thisResult["preview"] as String?
            val explicit = thisResult["explicit_lyrics"] as Boolean?
            val trackId = (thisResult["id"] as Double?)?.toLong()
            val trackLength = (thisResult["duration"] as Double?)?.toInt()
            val readable = thisResult["readable"] as Boolean

            // linked album info
            val thisAlbum = albumOverride ?: thisResult["album"] as LinkedTreeMap<*, *>
            val linkedAlbumId = (thisAlbum["id"] as Double?)?.toLong()
            val secondaryName = thisAlbum["title"] as String?
            val coverSmall = thisAlbum["cover_small"] as String?
            val coverBig = thisAlbum["cover_big"] as String?
            val coverXL = thisAlbum["cover_xl"] as String?

            // linked artist info
            val thisArtist = thisResult["artist"] as LinkedTreeMap<*, *>
            val tertiaryName = thisArtist["name"] as String?

            val thisResultData = TrackData().apply {
                this.primaryName = primaryName
                this.secondaryName = secondaryName
                this.tertiaryName = tertiaryName
                this.coverSmall = coverSmall
                this.coverBig = coverBig
                this.coverXL = coverXL
                this.trackId = trackId
                this.trackPreviewUrl = previewUrl
                this.trackLength = trackLength
                this.readable = readable
                this.linkedAlbumId = linkedAlbumId
                this.explicit = explicit
                this.resultRaw = thisResult
                this.cardType = "track"
            }

            addResultItem(thisResultData)
        }
    }

    private fun searchAlbum(normalResultList: ArrayList<*>) {
        if (BuildConfig.DEBUG) Log.d(TAG, normalResultList.toString())

        for (i in normalResultList) {
            val thisResult = i as LinkedTreeMap<*, *>

            // album info
            val primaryName = thisResult["title"] as String?
            val coverSmall = thisResult["cover_small"] as String?
            val coverBig = thisResult["cover_big"] as String?
            val coverXL = thisResult["cover_xl"] as String?
            val tertiaryName = "${(thisResult["nb_tracks"] as Double?)?.toInt()} track(s)"
            val explicit = thisResult["explicit_lyrics"] as Boolean?
            val contentListUrl = thisResult["tracklist"] as String?
            val trackCount = (thisResult["nb_tracks"] as Double?)?.toInt()
            val albumId = (thisResult["id"] as Double?)?.toLong()
            val genreId = (thisResult["genre_id"] as Double?)?.toInt()

            // linked artist info
            val thisArtist = thisResult["artist"] as LinkedTreeMap<*, *>
            val secondaryName = thisArtist["name"] as String?

            val thisResultData = TrackData().apply {
                this.primaryName = primaryName
                this.secondaryName = secondaryName
                this.tertiaryName = tertiaryName
                this.coverSmall = coverSmall
                this.coverBig = coverBig
                this.coverXL = coverXL
                this.trackId = albumId
                this.genreId = genreId
                this.explicit = explicit
                this.contentListUrl = contentListUrl
                this.trackCount = trackCount
                this.resultRaw = thisResult
                this.cardType = "album"
            }

            addResultItem(thisResultData)
        }
    }

    private fun searchArtist(normalResultList: ArrayList<*>) {
        if (BuildConfig.DEBUG) Log.d(TAG, normalResultList.toString())

        for (i in normalResultList) {
            val thisResult = i as LinkedTreeMap<*, *>

            // artist info
            val primaryName = thisResult["name"] as String?
            val coverSmall = thisResult["picture_small"] as String?
            val coverBig = thisResult["picture_big"] as String?
            val secondaryName = "${(thisResult["nb_album"] as Double?)?.toInt()} album(s)"
            val tertiaryName = "${(thisResult["nb_fan"] as Double?)?.toInt()} fan(s)"
            val artistId = (thisResult["id"] as Double?)?.toLong()

            val thisResultData = TrackData().apply {
                this.primaryName = primaryName
                this.secondaryName = secondaryName
                this.tertiaryName = tertiaryName
                this.coverSmall = coverSmall
                this.coverBig = coverBig
                this.trackId = artistId
                this.resultRaw = thisResult
                this.cardType = "artist"
            }

            addResultItem(thisResultData)
        }
    }

    private fun searchPlaylist(normalResultList: ArrayList<*>) {
        if (BuildConfig.DEBUG) Log.d(TAG, normalResultList.toString())

        for (i in normalResultList) {
            val thisResult = i as LinkedTreeMap<*, *>

            // playlist info
            val primaryName = thisResult["title"] as String?
            val coverSmall = thisResult["picture_small"] as String?
            val coverBig = thisResult["picture_big"] as String?
            val tertiaryName = "${(thisResult["nb_tracks"] as Double?)?.toInt()} track(s)"
            val contentListUrl = thisResult["tracklist"] as String?
            val trackCount = (thisResult["nb_tracks"] as Double?)?.toInt()
            val playlistId = (thisResult["id"] as Double?)?.toLong()

            // linked user info
            val thisUser = thisResult["user"] as LinkedTreeMap<*, *>?
            val secondaryName = thisUser?.get("name") as String?

            val thisResultData = TrackData().apply {
                this.primaryName = primaryName
                this.secondaryName = secondaryName
                this.tertiaryName = tertiaryName
                this.coverSmall = coverSmall
                this.coverBig = coverBig
                this.trackId = playlistId
                this.contentListUrl = contentListUrl
                this.trackCount = trackCount
                this.resultRaw = thisResult
                this.cardType = "playlist"
            }

            addResultItem(thisResultData)
        }
    }

    private fun handleCardClick(view: View, adapter: IAdapter<AbstractItem<ResultItem.ViewHolder>>, item: AbstractItem<ResultItem.ViewHolder>, position: Int) {
        val thisCard = adapter.getAdapterItem(position).getViewHolder(view)
        val thisData = thisCard.getCardData()

        when (thisData.cardType) {
            "track" -> showTrackSheet(thisData)
            "album" -> showAlbumSheet(thisData)
            "artist" -> showArtistSheet(thisData)
            "playlist" -> showPlaylistSheet(thisData)
        }
    }

    private fun showSingleDownloadPrompt(thisData: TrackData) {
        val qualityList = listOf("FLAC", "MP3 320", "MP3 128")
        val lastBitrate = sharedPreferences!!.getInt("download_quality", 1)

        runOnUiThread {
            MaterialDialog(this).show {
                cornerRadius(res = R.dimen.md_corner_radius)
                title(text = thisData.primaryName)

                listItemsSingleChoice(items = qualityList, initialSelection = lastBitrate) { _, index, _ ->
                    when (index) {
                        0 -> GlobalScope.launch {
                            setIntPref("download_quality", 0)
                            requestDownload(thisData, 9)
                        }
                        1 -> GlobalScope.launch {
                            setIntPref("download_quality", 1)
                            requestDownload(thisData, 3)
                        }
                        2 -> GlobalScope.launch {
                            setIntPref("download_quality", 2)
                            requestDownload(thisData, 1)
                        }
                    }
                }

                positiveButton(R.string.download)

                negativeButton(R.string.cancel)
            }
        }
    }

    private fun showMultipleDownloadPrompt(thisDataArrayList: ArrayList<TrackData>, batchName: String) {
        val qualityList = listOf("FLAC", "MP3 320", "MP3 128")
        val lastBitrate = sharedPreferences!!.getInt("download_quality", 1)

        runOnUiThread {
            MaterialDialog(this).show {
                cornerRadius(res = R.dimen.md_corner_radius)
                title(text = batchName)

                listItemsSingleChoice(items = qualityList, initialSelection = lastBitrate) { _, index, _ ->
                    when (index) {
                        0 -> {
                            setIntPref("download_quality", 0)
                            for (i in thisDataArrayList) {
                                GlobalScope.launch {
                                    requestDownload(i, 9, true)
                                }
                            }
                        }
                        1 -> {
                            setIntPref("download_quality", 1)
                            for (i in thisDataArrayList) {
                                GlobalScope.launch {
                                    requestDownload(i, 3, true)
                                }
                            }
                        }
                        2 -> {
                            setIntPref("download_quality", 2)
                            for (i in thisDataArrayList) {
                                GlobalScope.launch {
                                    requestDownload(i, 1, true)
                                }
                            }
                        }
                    }
                }

                positiveButton(R.string.download)

                negativeButton(R.string.cancel)
            }
        }
    }

    private fun showTrackSheet(thisData: TrackData) {
        val thisDialog = MaterialDialog(this).apply {
            cornerRadius(res = R.dimen.md_corner_radius)
            customView(R.layout.track_popup_layout)
        }

        val thisCustomView = thisDialog.getCustomView()

        thisCustomView.PrimaryTitleView.text = thisData.primaryName
        thisCustomView.SecondaryTitleView.text = thisData.secondaryName
        thisCustomView.TertiaryTitleView.text = thisData.tertiaryName
        thisCustomView.extraInfoView2.text = thisData.trackId.toString()

        var trackLengthMinutes = 0
        var trackLengthSeconds = thisData.trackLength!!

        while (trackLengthSeconds >= 60) {
            trackLengthMinutes++
            trackLengthSeconds -= 60
        }

        val trackLengthFormatted = if (trackLengthMinutes > 0 && trackLengthSeconds > 0) {
            "$trackLengthMinutes min, $trackLengthSeconds sec"
        } else if (trackLengthMinutes > 0 && trackLengthSeconds == 0) {
            "$trackLengthMinutes min"
        } else {
            "$trackLengthSeconds sec"
        }

        thisCustomView.extraInfoView.text = trackLengthFormatted

        val mediaPlayer = MediaPlayer()
        var earlyPlay = false

        thisCustomView.PreviewButton.setOnClickListener {
            earlyPlay = true
        }

        if (!thisData.trackPreviewUrl.isNullOrBlank()) {
            val thisTempFile = File("${cacheDir.absolutePath}/${thisData.trackId}_preview.tmp")

            if (!thisTempFile.exists()) {
                var thisPreview: ByteArray? = null

                previewThread = GlobalScope.launch {
                    try {
                        thisPreview = URL(thisData.trackPreviewUrl).readBytes()
                    } catch (e: Exception) {
                        previewThread?.dispose()
                    }
                }.invokeOnCompletion {
                    if (thisPreview != null) {
                        if (!thisTempFile.exists()) {
                            thisTempFile.createNewFile()
                            thisTempFile.writeBytes(thisPreview!!)
                        }

                        mediaPlayer.setDataSource(thisTempFile.absolutePath)
                        mediaPlayer.prepare()

                        if (earlyPlay) mediaPlayer.start()

                        runOnUiThread {
                            thisCustomView.PreviewButton.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.colorTextLight
                                )
                            )
                        }

                        thisCustomView.PreviewButton.setOnClickListener {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                                mediaPlayer.seekTo(0)
                            } else {
                                mediaPlayer.start()
                            }
                        }
                    }
                }
            } else {
                mediaPlayer.setDataSource(thisTempFile.absolutePath)
                mediaPlayer.prepare()

                if (earlyPlay) mediaPlayer.start()

                runOnUiThread {
                    thisCustomView.PreviewButton.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorTextLight
                        )
                    )
                }

                thisCustomView.PreviewButton.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        mediaPlayer.seekTo(0)
                    } else {
                        mediaPlayer.start()
                    }
                }
            }
        }

        if (!thisData.coverBig.isNullOrBlank()) {
            var thisArtThread: DisposableHandle? = null
            val thisTempFile = File("${cacheDir.absolutePath}/${thisData.linkedAlbumId}_big.tmp")

            if (!thisTempFile.exists()) {
                var thisAlbumArtBig: ByteArray? = null

                thisArtThread = GlobalScope.launch {
                    try {
                        thisAlbumArtBig = URL(thisData.coverBig).readBytes()
                    } catch (e: Exception) {
                        thisArtThread?.dispose()
                    }
                }.invokeOnCompletion {
                    if (thisAlbumArtBig != null) {
                        if (!thisTempFile.exists()) {
                            thisTempFile.createNewFile()
                            thisTempFile.writeBytes(thisAlbumArtBig!!)
                        }
                        val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig!!.size)
                        runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                    }
                }
            } else {
                val thisAlbumArtBig = thisTempFile.readBytes()
                val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig.size)
                runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
            }
        }

        thisCustomView.CloseButton.setOnClickListener {
            thisDialog.dismiss()
        }

        thisCustomView.DownloadTrack.setOnClickListener {
            thisDialog.dismiss()

            if (sharedPreferences!!.getBoolean("quality_always_ask", true)) {
                showSingleDownloadPrompt(thisData)
            } else {
                when (sharedPreferences!!.getInt("download_quality", 1)) {
                    0 -> GlobalScope.launch { requestDownload(thisData, 9) }
                    1 -> GlobalScope.launch { requestDownload(thisData, 3) }
                    2 -> GlobalScope.launch { requestDownload(thisData, 1) }
                }
            }
        }

        thisCustomView.ViewAlbum.setOnClickListener {
            thisDialog.dismiss()
            var thisAlbumData: TrackData? = null

            GlobalScope.launch {
                thisAlbumData = backend.getAlbumData(thisData.linkedAlbumId!!)
            }.invokeOnCompletion {
                showAlbumSheet(thisAlbumData!!)
            }
        }

        thisDialog.show {
            onDismiss {
                previewThread?.dispose()
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    private fun showAlbumSheet(thisData: TrackData) {
        runOnUiThread {
            val thisDialog = MaterialDialog(this).apply {
                cornerRadius(res = R.dimen.md_corner_radius)
                customView(R.layout.album_popup_layout)
            }

            val thisCustomView = thisDialog.getCustomView()

            thisCustomView.PrimaryTitleView.text = thisData.primaryName
            thisCustomView.SecondaryTitleView.text = thisData.secondaryName
            thisCustomView.TertiaryTitleView.text = thisData.tertiaryName
            thisCustomView.extraInfoView.text = thisData.trackId.toString()

            if (!thisData.coverBig.isNullOrBlank()) {
                var thisArtThread: DisposableHandle? = null
                val thisTempFile = File("${cacheDir.absolutePath}/${thisData.trackId}_big.tmp")

                if (!thisTempFile.exists()) {
                    var thisAlbumArtBig: ByteArray? = null

                    thisArtThread = GlobalScope.launch {
                        try {
                            thisAlbumArtBig = URL(thisData.coverBig).readBytes()
                        } catch (e: Exception) {
                            thisArtThread?.dispose()
                        }
                    }.invokeOnCompletion {
                        if (thisAlbumArtBig != null) {
                            if (!thisTempFile.exists()) {
                                thisTempFile.createNewFile()
                                thisTempFile.writeBytes(thisAlbumArtBig!!)
                            }
                            val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig!!.size)
                            runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                        }
                    }
                } else {
                    val thisAlbumArtBig = thisTempFile.readBytes()
                    val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig.size)
                    runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                }
            }

            thisCustomView.TracklistButton.setOnClickListener {
                thisDialog.dismiss()
                showTracklist(thisData)
            }

            thisCustomView.DownloadAllTracksButton.setOnClickListener {
                GlobalScope.launch {
                    thisDialog.dismiss()
                    val thisDataArrayList = backend.getTracklist(thisData)

                    if (sharedPreferences!!.getBoolean("quality_always_ask", true)) {
                        showMultipleDownloadPrompt(thisDataArrayList, thisData.primaryName!!)
                    } else {
                        when (sharedPreferences!!.getInt("download_quality", 1)) {
                            0 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 9, true)
                                    }
                                }
                            }
                            1 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 3, true)
                                    }
                                }
                            }
                            2 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 1, true)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            thisCustomView.CloseButton.setOnClickListener {
                thisDialog.dismiss()
            }

            thisDialog.show()
        }
    }

    private fun showArtistSheet(thisData: TrackData) {
        val thisDialog = MaterialDialog(this).apply {
            cornerRadius(res = R.dimen.md_corner_radius)
            customView(R.layout.artist_popup_layout)
        }

        val thisCustomView = thisDialog.getCustomView()

        thisCustomView.PrimaryTitleView.text = thisData.primaryName
        thisCustomView.SecondaryTitleView.text = thisData.secondaryName
        thisCustomView.TertiaryTitleView.text = thisData.tertiaryName
        thisCustomView.extraInfoView.text = thisData.trackId.toString()

        if (!thisData.coverBig.isNullOrBlank()) {
            var thisArtThread: DisposableHandle? = null
            val thisTempFile = File("${cacheDir.absolutePath}/${thisData.trackId}_big.tmp")

            if (!thisTempFile.exists()) {
                var thisAlbumArtBig: ByteArray? = null

                thisArtThread = GlobalScope.launch {
                    try {
                        thisAlbumArtBig = URL(thisData.coverBig).readBytes()
                    } catch (e: Exception) {
                        thisArtThread?.dispose()
                    }
                }.invokeOnCompletion {
                    if (thisAlbumArtBig != null) {
                        if (!thisTempFile.exists()) {
                            thisTempFile.createNewFile()
                            thisTempFile.writeBytes(thisAlbumArtBig!!)
                        }
                        val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig!!.size)
                        runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                    }
                }
            } else {
                val thisAlbumArtBig = thisTempFile.readBytes()
                val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig.size)
                runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
            }
        }

        thisCustomView.AlbumlistButton.setOnClickListener {
            thisDialog.dismiss()
            showAlbumlist(thisData)
        }

        thisCustomView.CloseButton.setOnClickListener {
            thisDialog.dismiss()
        }

        thisDialog.show()
    }

    private fun showPlaylistSheet(thisData: TrackData) {
        runOnUiThread {
            val thisDialog = MaterialDialog(this).apply {
                cornerRadius(res = R.dimen.md_corner_radius)
                customView(R.layout.album_popup_layout)
            }

            val thisCustomView = thisDialog.getCustomView()

            thisCustomView.PrimaryTitleView.text = thisData.primaryName
            thisCustomView.SecondaryTitleView.text = thisData.secondaryName
            thisCustomView.TertiaryTitleView.text = thisData.tertiaryName
            thisCustomView.extraInfoView.text = thisData.trackId.toString()

            if (!thisData.coverBig.isNullOrBlank()) {
                var thisArtThread: DisposableHandle? = null
                val thisTempFile = File("${cacheDir.absolutePath}/${thisData.trackId}_big.tmp")

                if (!thisTempFile.exists()) {
                    var thisAlbumArtBig: ByteArray? = null

                    thisArtThread = GlobalScope.launch {
                        try {
                            thisAlbumArtBig = URL(thisData.coverBig).readBytes()
                        } catch (e: Exception) {
                            thisArtThread?.dispose()
                        }
                    }.invokeOnCompletion {
                        if (thisAlbumArtBig != null) {
                            if (!thisTempFile.exists()) {
                                thisTempFile.createNewFile()
                                thisTempFile.writeBytes(thisAlbumArtBig!!)
                            }
                            val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig!!.size)
                            runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                        }
                    }
                } else {
                    val thisAlbumArtBig = thisTempFile.readBytes()
                    val imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig.size)
                    runOnUiThread { thisCustomView.AlbumArtView.setImageBitmap(imageBitmap) }
                }
            }

            thisCustomView.TracklistButton.setOnClickListener {
                thisDialog.dismiss()
                showTracklist(thisData)
            }

            thisCustomView.DownloadAllTracksButton.setOnClickListener {
                GlobalScope.launch {
                    thisDialog.dismiss()
                    val thisDataArrayList = backend.getTracklist(thisData)

                    if (sharedPreferences!!.getBoolean("quality_always_ask", true)) {
                        showMultipleDownloadPrompt(thisDataArrayList, thisData.primaryName!!)
                    } else {
                        when (sharedPreferences!!.getInt("download_quality", 1)) {
                            0 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 9, true)
                                    }
                                }
                            }
                            1 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 3, true)
                                    }
                                }
                            }
                            2 -> {
                                for (i in thisDataArrayList) {
                                    GlobalScope.launch {
                                        requestDownload(i, 1, true)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            thisCustomView.CloseButton.setOnClickListener {
                thisDialog.dismiss()
            }

            thisDialog.show()
        }
    }

    private fun showTracklist(thisData: TrackData) {
        resultsLoadingProgressBar.unhide()
        showContentList(thisData.primaryName)

        var thisTracklist: ArrayList<*>? = null

        searchThread?.dispose()

        searchThread = GlobalScope.launch {
            thisTracklist = backend.getAlbumTracklist(thisData.contentListUrl!!, thisData.trackCount!!)
        }.invokeOnCompletion {
            resultsLoadingProgressBar.hide()

            if (thisData.cardType == "album") {
                searchTrack(thisTracklist!!, thisData.resultRaw)
            } else {
                searchTrack(thisTracklist!!)
            }
        }
    }

    private fun showAlbumlist(thisData: TrackData) {
        resultsLoadingProgressBar.unhide()
        showContentList(thisData.primaryName)

        val thisAlbumDataList: ArrayList<TrackData> = ArrayList()

        searchThread?.dispose()

        searchThread = GlobalScope.launch {
            val thisAlbumList: ArrayList<*> = backend.getArtistAlbumlist(thisData.trackId!!)

            for (i in thisAlbumList) {
                val thisResult = i as LinkedTreeMap<*, *>
                val thisAlbumId = (thisResult["id"] as Double).toLong()
                val thisResultData = backend.getAlbumData(thisAlbumId)
                thisAlbumDataList += thisResultData
            }
        }.invokeOnCompletion {
            resultsLoadingProgressBar.hide()
            cardIdCounter = 0

            for (a in thisAlbumDataList) {
                addResultItem(a)
            }
        }
    }

    private fun showContentList(listName: String? = null) {
        isInContentList = true

        runOnUiThread {
            itemAdapter.clear()
            TracklistName.text = listName
            MainBottomNav.hide()
            SearchBarConstraint.hide()
            TracklistHeaderConstraint.unhide()
        }
    }

    private fun hideContentList() {
        isInContentList = false

        runOnUiThread {
            MainBottomNav.unhide()
            SearchBarConstraint.unhide()
            TracklistHeaderConstraint.hide()
        }

        triggerSearch()
    }

    private fun requestDownload(thisData: TrackData, quality: Int, albumDownload: Boolean = false) {
        serviceMessenger!!.send(Message().apply {
            obj = thisData
            data.putString("task", "request_download")
            data.putInt("quality", quality)
            data.putBoolean("albumDownload", albumDownload)
        })
    }

    private fun showSettingsPopup() {
        val thisDialog = MaterialDialog(this).apply {
            cornerRadius(res = R.dimen.md_corner_radius)
            customView(R.layout.settings_popup_layout)
            title(R.string.settings)
        }

        val thisCustomView = thisDialog.getCustomView()

        thisCustomView.LyricsSwitch.isChecked = sharedPreferences!!.getBoolean("get_lyrics", false)

        thisCustomView.LyricsSwitch.setOnCheckedChangeListener { _, isChecked ->
            setBoolPref("get_lyrics", isChecked)
        }

        thisCustomView.AlbumFolderSwitch.isChecked = sharedPreferences!!.getBoolean("create_album_folders", true)

        thisCustomView.AlbumFolderSwitch.setOnCheckedChangeListener { _, isChecked ->
            setBoolPref("create_album_folders", isChecked)
        }

        thisCustomView.DownloadPathView.text = sharedPreferences!!.getString("download_path", "${Environment.getExternalStorageDirectory().absolutePath}/Music/Deezer")

        thisCustomView.DownloadPathView.setOnClickListener {
            thisDialog.dismiss()
            showDownloadPathChooser()
        }

        thisCustomView.QualityAskSwitch.isChecked = sharedPreferences!!.getBoolean("quality_always_ask", true)

        thisCustomView.QualityAskSwitch.setOnCheckedChangeListener { _, isChecked ->
            setBoolPref("quality_always_ask", isChecked)
        }

        thisCustomView.QualitySpinner.setSelection(sharedPreferences!!.getInt("download_quality", 1))

        thisCustomView.QualitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setIntPref("download_quality", position)
            }
        }

        thisCustomView.ArtistSepSpinner.setSelection(sharedPreferences!!.getInt("artist_separator", 0))

        thisCustomView.ArtistSepSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setIntPref("artist_separator", position)
            }
        }

        thisCustomView.ResultLimitSpinner.setSelection(sharedPreferences!!.getInt("result_limit", 1))

        thisCustomView.ResultLimitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setIntPref("result_limit", position)
            }
        }

        thisCustomView.SourceButton.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW)
            urlIntent.data = Uri.parse("https://gitlab.com/Nick80835/add")
            startActivity(urlIntent)
        }

        thisCustomView.CloseButton.setOnClickListener {
            thisDialog.dismiss()
        }

        thisDialog.show()
    }

    private fun showDownloadPathChooser() {
        MaterialDialog(this).show {
            folderChooser(allowFolderCreation = true) { _, file ->
                setStringPref("download_path", file.absolutePath)
            }

            onDismiss {
                showSettingsPopup()
            }
        }
    }

    private class IncomingHandler(val mainActivity: MainActivity) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.obj as String) {
                "request_snackbar" -> {
                    mainActivity.showSnackbar(msg.data.get("snackbarText") as String)
                }
            }
        }
    }

    private inner class AIDServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            unbindService(serviceConnection)
            Log.d(TAG, "Service disconnected")
        }
    }

    private fun updateSearchHint(searchType: String) {
        val searchPlaceholder = when (searchType) {
            "track" -> getString(R.string.search_tracks)
            "album" -> getString(R.string.search_albums)
            "artist" -> getString(R.string.search_artists)
            "playlist" -> getString(R.string.search_playlists)
            else -> ""
        }

        SearchBar.hint = getString(R.string.search_deezer, searchPlaceholder)
    }

    private fun showStoragePopup() {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.no_storage_perm)
                setCancelable(false)
                setPositiveButton(R.string.okay) { _, _ ->
                    finish()
                    exitProcess(0)
                }
                create().show()
            }
        }
    }

    private fun showCountryWarningPopup(country: String) {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.country_not_supported, country))
                setPositiveButton(R.string.okay) { DialogInterface, _ ->
                    DialogInterface.dismiss()
                }
                create().show()
            }
        }
    }

    private fun showConnectErrorPopup() {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.connection_error)
                setPositiveButton(R.string.okay) { DialogInterface, _ ->
                    DialogInterface.dismiss()
                }
                create().show()
            }
        }
    }

    fun showSnackbar(snackText: String) {
        runOnUiThread {
            Snackbar.make(ResultConstraint, snackText, Snackbar.LENGTH_LONG).apply {
                if (MainBottomNav.isVisible) {
                    anchorView = MainBottomNav
                }
            }.show()
        }
    }

    private fun View.hideKeyboard() {
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun View.hide() {
        runOnUiThread {
            this.visibility = View.GONE
        }
    }

    private fun View.unhide() {
        runOnUiThread {
            this.visibility = View.VISIBLE
        }
    }
}
