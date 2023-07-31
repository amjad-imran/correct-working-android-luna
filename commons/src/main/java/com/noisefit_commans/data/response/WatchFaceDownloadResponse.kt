package com.noisefit_commans.data.response

data class WatchFaceDownloadResponse(
    val image: String,
    val imageName: String,
    val zip: WatchFaceZip
)

data class WatchFaceZip(
    val url: String,
    val originalFileName: String
)