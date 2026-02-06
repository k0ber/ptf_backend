package org.patifiner.base

import io.ktor.http.HttpStatusCode

abstract class PtfException(override val message: String, val code: String, val statusCode: HttpStatusCode) : Exception(message)
