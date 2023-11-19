package com.nick80835.add

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

class ResultItem(val data: TrackData, val mainActivity: MainActivity) : AbstractItem<ResultItem.ViewHolder>() {
    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = data.cardId

    /** defines the layout which will be used for this item in the list  */
    override val layoutRes: Int
        get() = R.layout.track_result_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    inner class ViewHolder(view: View) : FastAdapter.ViewHolder<ResultItem>(view) {
        private var resultMainTitle: TextView = view.findViewById(R.id.resultMainTitle)
        private var resultSecondaryTitle: TextView = view.findViewById(R.id.resultSecondaryTitle)
        private var resultTertiaryTitle: TextView = view.findViewById(R.id.resultTertiaryTitle)
        private var albumArtView: ImageView = view.findViewById(R.id.albumArtView)
        private var resultExplicitIndicator: ImageView = view.findViewById(R.id.resultExplicitIndicator)
        private var resultUnreadableIndicator: ImageView = view.findViewById(R.id.resultUnreadableIndicator)
        private var imageBitmap: Bitmap? = null
        private var thisArtThread: DisposableHandle? = null

        fun getCardData(): TrackData {
            return data
        }

        override fun bindView(item: ResultItem, payloads: MutableList<Any>) {
            resultMainTitle.text = item.data.primaryName
            resultSecondaryTitle.text = item.data.secondaryName
            resultTertiaryTitle.text = item.data.tertiaryName

            if (item.data.explicit != null) {
                if (item.data.explicit!!) {
                    resultExplicitIndicator.visibility = View.VISIBLE
                }
            }

            if (item.data.readable != null) {
                if (!item.data.readable!!) {
                    resultUnreadableIndicator.visibility = View.VISIBLE
                }
            }

            if (item.data.cardType == "artist") {
                albumArtView.contentDescription = mainActivity.getString(R.string.artist_art_description)
            }

            if (imageBitmap == null && !item.data.coverBig.isNullOrBlank()) {
                val thisTempId = when (item.data.cardType) {
                    "track" -> item.data.linkedAlbumId
                    else -> item.data.trackId
                }

                val thisTempFile = File("${mainActivity.cacheDir.absolutePath}/${thisTempId}_small.tmp")

                if (!thisTempFile.exists()) {
                    var thisAlbumArtSmall: ByteArray? = null

                    thisArtThread = GlobalScope.launch {
                        try {
                            thisAlbumArtSmall = URL(item.data.coverBig).readBytes()

                            if (thisAlbumArtSmall != null) {
                                if (!thisTempFile.exists()) {
                                    thisTempFile.createNewFile()
                                    thisTempFile.writeBytes(thisAlbumArtSmall!!)
                                }
                            } else {
                                thisArtThread?.dispose()
                            }
                        } catch (e: Exception) {
                            thisArtThread?.dispose()
                        }
                    }.invokeOnCompletion {
                        if (thisAlbumArtSmall != null) {
                            imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtSmall, 0, thisAlbumArtSmall!!.size)
                            addImageBitmap()
                        }
                    }
                } else {
                    thisArtThread = GlobalScope.launch {
                        val thisAlbumArtBig = thisTempFile.readBytes()
                        imageBitmap = BitmapFactory.decodeByteArray(thisAlbumArtBig, 0, thisAlbumArtBig.size)
                    }.invokeOnCompletion {
                        addImageBitmap()
                    }
                }
            } else if (imageBitmap != null) {
                addImageBitmap()
            }
        }

        private fun addImageBitmap() {
            mainActivity.runOnUiThread { albumArtView.setImageBitmap(imageBitmap) }
        }

        override fun unbindView(item: ResultItem) {
            resultMainTitle.text = mainActivity.getString(R.string.song_name_placeholder)
            resultSecondaryTitle.text = mainActivity.getString(R.string.album_name_placeholder)
            resultTertiaryTitle.text = mainActivity.getString(R.string.artist_name_placeholder)
            resultExplicitIndicator.visibility = View.GONE
            resultUnreadableIndicator.visibility = View.GONE
            imageBitmap = null
            thisArtThread?.dispose()
            mainActivity.runOnUiThread { albumArtView.setImageDrawable(mainActivity.getDrawable(R.drawable.placeholder_disc)) }
        }
    }
}
