package org.patifiner.upload.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PtfException

sealed class UploadException(message: String, statusCode: HttpStatusCode) : PtfException(message, "UPLOAD_ERROR", statusCode) {

    class InvalidFileTypeException(mimeType: String) :
        UploadException("Unsupported file type: $mimeType. Only images are allowed.", HttpStatusCode.BadRequest)

    class FileTooLargeException(maxSizeMb: Int) :
        UploadException("File size exceeds maximum limit of $maxSizeMb MB.", HttpStatusCode.PayloadTooLarge)

    class MissingFilePartException :
        UploadException("Missing required file part in request.", HttpStatusCode.BadRequest)
}