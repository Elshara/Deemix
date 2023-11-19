package app.deemix.downloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.deemix.downloader.types.DownloadItem
import com.squareup.picasso.Picasso

class DownloadItemAdapter(private val dataSet: Map<String, DownloadItem>, private val order: ArrayList<String>) :
    RecyclerView.Adapter<DownloadItemAdapter.DownloadViewHolder>() {

    class DownloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.downloadTitle)
        val artist: TextView = view.findViewById(R.id.downloadArtist)
        val progress: TextView = view.findViewById(R.id.downloadProgress)
        val quality: TextView = view.findViewById(R.id.qualityLabel)
        val fails: TextView = view.findViewById(R.id.downloadFails)
        val bar: ProgressBar = view.findViewById(R.id.downloadBar)
        val cover: ImageView = view.findViewById(R.id.coverImage)
        val status: ImageView = view.findViewById(R.id.downloadStatus)

        private fun statusIcon(status: String): Int{
            return when (status){
                "inQueue" -> R.drawable.ic_baseline_list_24
                "downloading" -> R.drawable.ic_baseline_arrow_downward_24
                "downloaded" -> R.drawable.ic_baseline_done_24
                "downloadedWithErrors" -> R.drawable.ic_baseline_warning_24
                "failed" -> R.drawable.ic_baseline_error_24
                else -> R.drawable.ic_baseline_list_24
            }
        }

        fun updateDownloadItem(downloadItem: DownloadItem){
            progress.text = "${downloadItem.downloaded+downloadItem.failed}/${downloadItem.size}"
            status.setImageResource(statusIcon(downloadItem.status))
            if (downloadItem.failed > 0){
                fails.text = "${downloadItem.failed} (!)"
                fails.visibility = View.VISIBLE
            } else {
                fails.visibility = View.GONE
            }
            when {
                downloadItem.progress == -1 -> {
                    bar.isIndeterminate = true
                }
                downloadItem.progress >= 100 -> {
                    bar.isIndeterminate = false
                    bar.progress = 100
                }
                else -> {
                    bar.isIndeterminate = false
                    bar.progress = downloadItem.progress
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DownloadViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.download_item, viewGroup, false)

        return DownloadViewHolder(view)
    }

    private fun qualityText(quality: String): String{
        return when (quality) {
            "9" -> "FLAC"
            "3" -> "320"
            "1" -> "128"
            else -> "MISC"
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(downloadViewHolder: DownloadViewHolder, position: Int) {
        val downloadItem = dataSet[order[position]]!!

        downloadViewHolder.title.text = downloadItem.title
        downloadViewHolder.artist.text = downloadItem.artist
        downloadViewHolder.quality.text = qualityText(downloadItem.bitrate)
        Picasso.get()
            .load(downloadItem.cover)
            .placeholder(R.drawable.no_cover)
            .error(R.drawable.no_cover)
            .into(downloadViewHolder.cover)
        downloadViewHolder.updateDownloadItem(downloadItem)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = order.size

}