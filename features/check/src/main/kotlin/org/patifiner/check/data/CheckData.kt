package org.patifiner.check.data

interface CheckData {
    fun getCheckStatus(): CheckResponse
}

data class CheckResponse(
    val response: String,
    val versionName: String,
    val versionCode: String
)
