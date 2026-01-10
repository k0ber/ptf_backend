package org.patifiner.upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.patifiner.upload.api.UploadConfig
import org.patifiner.upload.api.UploadException
import org.patifiner.upload.api.UploadResponse
import org.slf4j.Logger
import java.io.File
import java.util.UUID

class UploadService(val config: UploadConfig, val logger: Logger) {
    private val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun saveImage(fileBytes: ByteArray, originalFileName: String?, fileContentType: String?, fileSize: Long): UploadResponse =
        withContext(Dispatchers.IO) {
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
            UploadResponse(url = publicUrl, filename = uniqueFileName, sizeBytes = fileSize)
        }

    fun deleteImage(filename: String) {
        try {
            val file = File(config.uploadPath, filename)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            logger.error("Failed to delete file: $filename, error: ${e.message}")
        }
    }
}
