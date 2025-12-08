package org.patifiner.upload.api

import io.ktor.http.HttpStatusCode

sealed class UploadException(override val message: String, val statusCode: HttpStatusCode) : RuntimeException(message) {

    class InvalidFileTypeException(mimeType: String) :
        UploadException("Unsupported file type: $mimeType. Only images are allowed.", HttpStatusCode.BadRequest)

    class FileTooLargeException(maxSizeMb: Int) :
        UploadException("File size exceeds maximum limit of $maxSizeMb MB.", HttpStatusCode.PayloadTooLarge)

    class MissingFilePartException :
        UploadException("Missing required file part in request.", HttpStatusCode.BadRequest)
}
