package org.patifiner.upload.api

data class UploadConfig(
    val uploadPath: String,
    val baseUrl: String,
    val maxFileSizeMB: Int
)

data class UploadResponse(
    val url: String,
    val filename: String,
    val sizeBytes: Long
)
