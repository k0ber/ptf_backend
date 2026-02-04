package org.patifiner.geo.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PtfException

sealed class UploadException(message: String, statusCode: HttpStatusCode) : PtfException(message, "UPLOAD_ERROR", statusCode) {

}