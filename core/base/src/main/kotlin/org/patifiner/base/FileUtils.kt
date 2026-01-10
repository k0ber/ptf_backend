package org.patifiner.base

import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider

suspend fun MultiPartData.readRawFile(): RawFile? {
    var bytes: ByteArray? = null
    var fileName: String? = null
    var contentType: String? = null

    forEachPart { part ->
        if (part is PartData.FileItem) {
            bytes = part.streamProvider().readBytes()
            fileName = part.originalFileName
            contentType = part.contentType?.toString()
        }
        part.dispose()
    }

    val finalBytes = bytes ?: return null
    return RawFile(finalBytes, fileName, contentType)
}

data class RawFile(val bytes: ByteArray, val name: String?, val contentType: String?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawFile

        if (!bytes.contentEquals(other.bytes)) return false
        if (name != other.name) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        return result
    }
}
