package com.nick80835.add

import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.net.URL
import java.text.DecimalFormat

class Downloader : AsyncTask<URL, Int, Long>() {
    private val twoDF = DecimalFormat("0.00")
    private var currentProgress = 0
    var outputPath: String? = null
    var taskId = ""
    var postExecute = { _: String -> }
    var onTaskCancel = { _: String -> }
    var onTaskFailure = { _: String -> }
    var progressCallback = { _: String, _: Int, _: String -> }

    override fun doInBackground(vararg params: URL?): Long {
        try {
            val connection = params[0]!!.openConnection()
            connection.connect()

            val lengthOfFile = connection.contentLength

            val inputStream = connection.getInputStream()

            val outputFile = File(outputPath!!)
            outputFile.createNewFile()

            var place = 0
            val chunk = ByteArray(16384)

            while (place < lengthOfFile) {
                val size = inputStream.read(chunk)
                val cutChunk = chunk.copyOfRange(0, size)

                place += size

                if (isCancelled) return 1
                publishProgress(place, lengthOfFile)

                outputFile.appendBytes(cutChunk)
            }

            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            return 2 // failed
        }

        Log.d(TAG, "Download succeeded: $taskId")
        return 0 // succeeded
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val progressPercent = (values[0]!! * 100) / values[1]!!

        if (progressPercent > currentProgress + 1 || progressPercent == 100) {
            currentProgress = progressPercent

            val progressSize = "${twoDF.format(values[0]!!.toFloat() / 1000000)}MB / ${twoDF.format(values[1]!!.toFloat() / 1000000)}MB"

            progressCallback(taskId, currentProgress, progressSize)
        }
    }

    override fun onPostExecute(result: Long?) {
        if (result == 0.toLong()) {
            postExecute(taskId)
        } else if (result == 2.toLong()) {
            onTaskFailure(taskId)
        }
    }

    override fun onCancelled() {
        super.onCancelled()
        onTaskCancel(taskId)
    }
}
