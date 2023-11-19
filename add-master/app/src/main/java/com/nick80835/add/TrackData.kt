package com.nick80835.add

import com.google.gson.internal.LinkedTreeMap

data class TrackData(
    // shared vars
    var primaryName: String? = null,
    var secondaryName: String? = null,
    var tertiaryName: String? = null,
    var coverSmall: String? = null,
    var coverBig: String? = null,
    var coverXL: String? = null,
    var explicit: Boolean? = false,
    var resultRaw: LinkedTreeMap<*, *>? = null,

    // album-only vars
    var genreId: Int? = null,
    var contentListUrl: String? = null,
    var trackCount: Int? = null,

    // track-only vars
    var trackId: Long? = null,
    var trackLength: Int? = null,
    var trackPreviewUrl: String? = null,
    var linkedAlbumId: Long? = null,
    var trackLyrics: String? = null,
    var linkedTrackTagData: TrackTagData? = null,
    var readable: Boolean? = true,

    // app vars
    var cardId: Int = cardIdCounter++,
    var cardType: String? = null
)

data class TrackTagData(
    var trackNumber: Int? = null,
    var trackBPM: Float? = null,
    var trackGenre: String? = null,
    var trackContributors: String? = null,
    var trackDate: String? = null
)
