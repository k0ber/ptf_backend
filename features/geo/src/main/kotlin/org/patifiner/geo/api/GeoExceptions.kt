package org.patifiner.geo.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PatifinerException

sealed class UploadException(message: String, statusCode: HttpStatusCode) : PatifinerException(message, "UPLOAD_ERROR", statusCode) {

}