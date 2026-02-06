package org.patifiner.upload.api

data class UploadResponse(
    val url: String,
    val filename: String,
    val sizeBytes: Long
)
