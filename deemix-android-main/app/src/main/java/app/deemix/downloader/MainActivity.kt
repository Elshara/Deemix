package app.deemix.downloader

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import app.deemix.downloader.SharedObjects.dz
import app.deemix.downloader.SharedObjects.fullQueue
import app.deemix.downloader.SharedObjects.queue
import app.deemix.downloader.Utils.getFinalURL
import app.deemix.downloader.types.*
import app.deemix.downloader.types.Collection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: DownloadItemAdapter
    private var constraints: Constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresStorageNotLow(true).build()
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var hasBeenInit: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate")
        // Set layout to main activity
        setContentView(R.layout.activity_main)

        // Get layout components
        val downloadInputBox = findViewById<EditText>(R.id.downloadInputBox)
        val downloadQueueView = findViewById<RecyclerView>(R.id.downloadQueue)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)

        // Create the download queue list with the Recycler View

        downloadQueueView.layoutManager = LinearLayoutManager(this)
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val uuid = fullQueue[position]
                GlobalScope.launch(Dispatchers.IO) {
                    removeFromQueue(uuid, position)
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(downloadQueueView)
        adapter = DownloadItemAdapter(queue, fullQueue)
        downloadQueueView.adapter = adapter
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Open settings when clicking the button
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Add deezer links to queue on downloadInputBox enter key press
        downloadInputBox.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                var link = downloadInputBox.text.toString()
                downloadInputBox.setText("") // Clear the text box

                if (URLUtil.isValidUrl(link) and link.contains("deezer")){
                    link = link.replace("http://", "https://")
                    GlobalScope.launch(Dispatchers.IO) {
                        addUrlToQueue(link)
                    }
                }

                // Hide the keyboard
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                // Remove focus
                downloadInputBox.clearFocus()
                return@OnKeyListener true
            }
            false
        })

        // Listener for the download queue
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("downloadQueue")
            .observe(this, Observer { listOfWorkInfo: List<WorkInfo>? ->
                if (listOfWorkInfo == null || listOfWorkInfo.isEmpty()) return@Observer
                var currentItem: WorkInfo? = null
                for (workInfo in listOfWorkInfo){
                    if (workInfo.state == WorkInfo.State.RUNNING){
                        currentItem = workInfo
                        break
                    }
                }
                if (currentItem == null) return@Observer

                val update = currentItem.progress
                val uuid = update.getString("uuid")
                val position = fullQueue.indexOf(uuid)
                val currentDownloadItem = queue[uuid] ?: return@Observer
                currentDownloadItem.progress = update.getInt("progress", 0)
                currentDownloadItem.failed = update.getInt("failed", 0)
                currentDownloadItem.downloaded = update.getInt("downloaded", 0)
                currentDownloadItem.status = update.getString("status").toString()
                if (currentDownloadItem.status != "inQueue" && currentDownloadItem.status != "downloading"){
                    currentDownloadItem.progress = 100
                }
                val viewHolder = downloadQueueView.findViewHolderForAdapterPosition(position)
                if (viewHolder != null){
                    (viewHolder as DownloadItemAdapter.DownloadViewHolder).updateDownloadItem(currentDownloadItem)
                }
            })

        setupPermissions()
    }

    private fun setupPermissions() {
        val shouldAskPermission: ArrayList<String> = ArrayList()
        for (permission in permissions){
            val hasPermission = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            println("$permission, $hasPermission")
            if (!hasPermission){
                shouldAskPermission.add(permission)
            }
        }

        if (shouldAskPermission.size > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission to write and read to storage is required by this app.")
                    .setTitle("Permission required")
            builder.setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(this, shouldAskPermission.toTypedArray(), 1)
            }
            val dialog = builder.create()
            dialog.show()
        } else {
            attemptAutoLogin()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            var accepted = true
            for (grant in grantResults){
                accepted = accepted && grant == PackageManager.PERMISSION_GRANTED
            }
            if (!accepted) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("The app can't work if it can't write to the storage")
                    .setTitle("Permissions not granted")
                builder.setPositiveButton("OK") { _, _ ->
                    finishAndRemoveTask()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                attemptAutoLogin()
            }
        }
    }

    private fun attemptAutoLogin(){
        GlobalScope.launch(Dispatchers.IO) {
            if (dz.isLoggedIn){
                init()
            } else {
                // Try retrieving the login data
                var loginText: String
                try {
                    openFileInput("login").use { stream ->
                        loginText = stream.bufferedReader().use {
                            it.readText()
                        }
                    }
                } catch (e: Exception){ loginText = "" }

                // No login file, show login fragment
                if (loginText == ""){
                    showLoginActivity()
                    return@launch
                }

                // Parse the login text file
                val splitLogin = loginText.split("\n")
                val accessToken: String = splitLogin[0]
                var arl: String = splitLogin[1]

                // Try to login
                var loggedIn = dz.login(arl)
                if (loggedIn) {
                    init(true)
                    return@launch
                }

                // Try to get a new arl from accessToken
                if (accessToken != ""){
                    val testArl = dz.getArlFromAccessToken(accessToken)
                    if (testArl == null){
                        showLoginActivity()
                        return@launch
                    }
                    arl = testArl
                    loggedIn = dz.login(arl)
                    if (loggedIn) {
                        saveLogin(accessToken, arl) // if the arl changed, save it
                        init(true)
                        return@launch
                    }
                }

                // Login failed, remove the file and show the login form
                forgetLogin()
                showLoginActivity()
            }
        }
    }

    private fun showLoginActivity(){
        val thisIntent = Intent(this, LoginActivity::class.java)
        thisIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain"){
            thisIntent.action = Intent.ACTION_SEND
            thisIntent.type = "text/plain"
            thisIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
        }
        startActivity(thisIntent)
        finish()
    }

    private fun saveLogin(accessToken: String, arl: String) {
        openFileOutput("login", Context.MODE_PRIVATE).use { output ->
            output.write("$accessToken\n$arl".toByteArray())
        }
    }
    private fun forgetLogin(){
        deleteFile("login")
    }

    private fun init(justLoggedIn: Boolean = false){
        if (!hasBeenInit){
            // Restore the queue
            if (justLoggedIn) {
                runOnUiThread {
                    Toast.makeText(
                        this, "Logged in as ${dz.currentUser!!.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            restoreQueue()
            initSettings()

            // Add deezer links to queue on intent action send text
            if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain"){
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val url = "https://${text?.substringAfterLast("https://")}"
                if (URLUtil.isValidUrl(url) and url.contains("deezer")){
                    addUrlToQueue(url)
                }
            }
            hasBeenInit = true
        }
        runOnUiThread { findViewById<ConstraintLayout>(R.id.loadingScreen).visibility = View.GONE }
    }

    private fun initSettings() {
        if (sharedPreferences.getString("download_quality", "0") == "0"){
            var quality = 1
            if (dz.currentUser!!.canStream.high) quality = 3
            if (dz.currentUser!!.canStream.lossless) quality = 9
            sharedPreferences.edit()
                .putString("download_quality", quality.toString())
                .apply()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume(){
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    private fun addUrlToQueue(url: String){
        // Extend and clean the URL if needed
        var link = url
        if (link.contains("deezer.page.link")) link = getFinalURL(link)
        if (link.contains("?")) link = link.substringBefore('?')
        if (link.contains("&")) link = link.substringBefore('&')
        if (link.endsWith("/")) link = link.substring(0, link.length-1)

        // Find the download type
        var downloadType: String? = null
        when {
            "/track/" in link -> downloadType = "track"
            "/album/" in link -> downloadType = "album"
            //"/artist/" in link -> downloadType = "artist"
            //"/playlist/" in link -> downloadType = "playlist"
        }
        if (downloadType == null){
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Type not recognized!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        // Find the download ID
        val downloadId: String? = """/$downloadType/(.+)""".toRegex().find(link)?.groups?.get(1)?.value
        if (downloadId == null) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Couldn't find the id!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        // Generate the download item
        val downloadItem: DownloadItem
        try {
            downloadItem = when (downloadType) {
                "track" -> generateTrackItem(downloadId)
                "album" -> generateAlbumItem(downloadId)
                //"artist" -> downloadItem = generateArtistItem(downloadId)
                //"playlist" -> downloadItem = generatePlaylistItem(downloadId)
                else -> throw Exception("Type not reconized")
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Error while trying to generate the download item!\n$e",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("deemix", e.message, e)
            return
        }
        downloadItem.bitrate = sharedPreferences.getString("download_quality", "1")!!
        downloadItem.generateUUID()

        // Check if item is already in queue
        if (queue.keys.contains(downloadItem.uuid)){
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Already in queue!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        // Add item to viewable queue
        downloadItem.status = "inQueue"
        fullQueue.add(downloadItem.uuid)
        queue[downloadItem.uuid] = downloadItem.minimalClone()
        runOnUiThread {
            adapter.notifyItemChanged(fullQueue.indexOf(downloadItem.uuid))
        }

        openFileOutput("queue", Context.MODE_PRIVATE).use { output ->
            output.write(TextUtils.join(";", fullQueue).toByteArray())
        }
        openFileOutput("${downloadItem.uuid}.json", Context.MODE_PRIVATE).use { output ->
            output.write(downloadItem.toString().toByteArray())
        }

        // Add to actual queue
        addToQueue(downloadItem.uuid)
    }

    private fun isAlreadyInQueue(uuid: String): Boolean{
        val jobs = WorkManager.getInstance(this).getWorkInfosByTag(uuid).get()
        return jobs.size > 0
    }

    private fun addToQueue(uuid: String) {
        val inputData: Data = Data.Builder()
            .putString("uuid", uuid)
            .build()
        val downloadWork: OneTimeWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(uuid)
            .build()
        WorkManager
            .getInstance(this)
            .enqueueUniqueWork(
                "downloadQueue",
                ExistingWorkPolicy.APPEND,
                downloadWork
            )
    }

    private fun removeFromQueue(uuid: String, position: Int){
        fullQueue.removeAt(position)
        runOnUiThread { adapter.notifyItemRemoved(position) }
        openFileOutput("queue", Context.MODE_PRIVATE).use { output ->
            output.write(TextUtils.join(";", fullQueue).toByteArray())
        }
        val deletedItem = queue[uuid] ?: return
        if (deletedItem.status == "downloading" || deletedItem.status == "inQueue"){
            WorkManager.getInstance(this).cancelAllWorkByTag(uuid)
            runOnUiThread { Toast.makeText(
                this@MainActivity,
                "Removed $uuid from queue",
                Toast.LENGTH_SHORT
            ).show() }
        }
        deleteFile("$uuid.json")
        queue.remove(uuid)
        WorkManager.getInstance(this).pruneWork()
    }

    private fun restoreQueue() {
        fullQueue.clear()
        var queueText: String
        try {
            openFileInput("queue").use { stream ->
                queueText = stream.bufferedReader().use {
                    it.readText()
                }
            }
        } catch (e: Exception){
            queueText = ""
        }
        val thisQueue = queueText.split(';')
        for (uuid in thisQueue){
            if (uuid.isBlank()) continue
            fullQueue.add(uuid)
            var downloadObject: JSONObject

            try {
                openFileInput("$uuid.json").use { stream ->
                    val text = stream.bufferedReader().use {
                        it.readText()
                    }
                    downloadObject = JSONObject(text)
                }
            } catch (e: Exception){
                fullQueue.remove(uuid)
                continue
            }

            var downloadItem: DownloadItem? = null
            if (downloadObject.getString("itemType") == "single") downloadItem = Single(downloadObject)
            else if (downloadObject.getString("itemType") == "collection") downloadItem = Collection(downloadObject)
            if (downloadItem != null){
                queue[uuid] = downloadItem.minimalClone()
                if (downloadItem.status == "inQueue" && !isAlreadyInQueue(downloadItem.uuid)){
                    addToQueue(downloadItem.uuid)
                }
                runOnUiThread {adapter.notifyItemChanged(fullQueue.indexOf(uuid))}
            }
        }
    }

    private fun generateTrackItem(downloadId: String, track: Track? = null, album: Album? = null): DownloadItem {
        val item = Single()
        val thisTrack: Track

        if (track == null) {
            thisTrack = Track()
            if (downloadId.toInt() < 0){
                val trackAPI = dz.apiCallGW("song.getData", "{\"SNG_ID\": $downloadId}")
                thisTrack.parseGW(trackAPI)
            } else {
                val trackAPI = dz.apiCall("track/$downloadId")
                thisTrack.parseAPI(trackAPI)
            }
        } else {
            thisTrack = track
        }

        item.id = thisTrack.id
        item.type = "track"
        item.title = thisTrack.title!!
        item.artist = thisTrack.artist.name
        item.explicit = thisTrack.explicit == true
        item.cover = thisTrack.album.pic.getURL(72, "jpg-80")

        val single = JSONObject()
        single.put("trackAPI", thisTrack.toJSON())
        if (album != null) single.put("albumAPI", album.toJSON())
        item.single = single
        return item
    }

    private fun generateAlbumItem(downloadId: String, rootArtist: Artist? = null): DownloadItem {
        val item = Collection()
        val thisAlbum = Album()
        var thisId = downloadId
        var albumAPI: JSONObject? = null

        if (thisId.startsWith("upc")){
            val upcs = ArrayList<String>()
            upcs.add(thisId.substring(4))
            upcs.add(upcs[0].toInt().toString()) // Try UPC without leading zeros as well
            var lastError = Exception()
            for (upc in upcs){
                try {
                    albumAPI = dz.apiCall("album/upc:$upc")
                    break
                } catch (e: Exception){
                    lastError = e
                    albumAPI = null
                }
            }
            if (albumAPI == null){
                throw Exception("Generation Error: $thisId, $lastError")
            }
            thisId = albumAPI.getString("id")
        } else {
            albumAPI = dz.apiCall("album/$thisId")
        }

        // Get extra info about album
        // This saves extra api calls when downloading
        val albumGW: JSONObject = dz.apiCallGW("album.getData", "{\"ALB_ID\": $thisId}")

        thisAlbum.parseAPI(albumAPI)
        thisAlbum.parseGW(albumGW)
        thisAlbum.rootArtist = rootArtist

        var songs = JSONArray()
        var songsFromGW = false
        val data = albumAPI.getJSONObject("tracks")
        if (data.length() == thisAlbum.trackTotal!!.toInt()){
            songs = data.getJSONArray("data")
        }
        if (songs.length() == 0){
            val body = dz.apiCallGW("song.getListByAlbum", "{\"ALB_ID\": $thisId, \"nb\": -1}")
            songs = body.getJSONArray("data")
            songsFromGW = true
        }

        thisAlbum.trackTotal = songs.length().toString()
        val tracks = JSONArray()
        for (i in 0 until songs.length()) {
            val currentTrackAPI = songs.getJSONObject(i)
            val track = Track()
            if (songsFromGW) track.parseGW(currentTrackAPI)
            else track.parseAPI(currentTrackAPI)
            track.position = i+1
            tracks.put(track.toJSON())
        }

        if (tracks.length() == 1){
            val singleTrack = Track().restoreObject(tracks.getJSONObject(0))
            return generateTrackItem(singleTrack.id, singleTrack, thisAlbum)
        }

        item.id = thisAlbum.id
        item.type = "album"
        item.title = thisAlbum.title
        item.artist = thisAlbum.artist.name
        item.explicit = thisAlbum.explicit == true
        item.size = songs.length()
        item.cover = thisAlbum.pic.getURL(72, "jpg-80")

        val collection = JSONObject()
        collection.put("tracks", tracks)
        collection.put("albumAPI", thisAlbum.toJSON())
        item.collection = collection
        return item
    }

    /*
    private fun generateArtistItem(downloadId: String): DownloadItem {

        val item = DownloadItem()
        return item
    }

    private fun generatePlaylistItem(downloadId: String): DownloadItem {
        val item = DownloadItem()
        return item
    }
    */

}