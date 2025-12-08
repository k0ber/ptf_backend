package org.patifiner.upload

import org.patifiner.upload.api.UploadConfig
import org.patifiner.upload.api.UploadException
import org.patifiner.upload.api.UploadResponse
import java.io.File
import java.util.UUID

class UploadService(val config: UploadConfig) {
    private val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun saveImage(fileBytes: ByteArray, originalFileName: String?, fileContentType: String?, fileSize: Long): UploadResponse {
        val maxSizeBytes = config.maxFileSizeMB * 1024 * 1024L
        if (fileSize > maxSizeBytes) {
            throw UploadException.FileTooLargeException(config.maxFileSizeMB)
        }

        val mimeType = fileContentType ?: throw UploadException.InvalidFileTypeException("unknown")
        if (mimeType !in supportedMimeTypes) {
            throw UploadException.InvalidFileTypeException(mimeType)
        }

        val extension = originalFileName?.substringAfterLast('.', "") ?: ""
        val uniqueFileName = "${UUID.randomUUID()}.${extension}"

        val targetDir = File(config.uploadPath)
        targetDir.mkdirs()

        val file = File(targetDir, uniqueFileName)
        file.writeBytes(fileBytes)

        val publicUrl = "${config.baseUrl}/$uniqueFileName"
        return UploadResponse(url = publicUrl, filename = uniqueFileName, sizeBytes = fileSize)
    }
}