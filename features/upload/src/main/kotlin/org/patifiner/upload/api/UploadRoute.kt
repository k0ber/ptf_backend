package org.patifiner.upload.api

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.upload.UploadService
import java.io.File


fun Route.uploadRoutes() {

    val uploadService: UploadService by inject()

    route("/upload") {
        authenticate(JWT_AUTH) {
            post("/image") {
                val multipart = call.receiveMultipart()
                var fileContent: ByteArray? = null
                var originalFileName: String? = null
                var fileContentType: String? = null
                var fileSize: Long = 0

                // Итерация по частям запроса
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val bytes = withContext(Dispatchers.IO) {
                            part.streamProvider().readBytes() // todo: эффективно для небольших файлов. Для очень больших файлов (более нескольких МБ), лучше использовать channel.copyTo(fileOutput)
                        }

                        fileContent = bytes
                        originalFileName = part.originalFileName
                        fileContentType = part.contentType?.toString()
                        fileSize = bytes.size.toLong()

                        part.dispose()
                        return@forEachPart
                    }
                    part.dispose()
                }

                // 4. Валидация наличия файла
                if (fileContent == null || originalFileName == null) {
                    throw UploadException.MissingFilePartException()
                }

                val response = uploadService.saveImage(
                    fileBytes = fileContent,
                    originalFileName = originalFileName,
                    fileContentType = fileContentType,
                    fileSize = fileSize
                )
                call.respond(HttpStatusCode.Created, response)
            }
        }
    }

    // ДОБАВЛЕНИЕ СЕРВИНГА СТАТИЧЕСКИХ ФАЙЛОВ (для доступа по URL)
    route("/files") {
        staticFiles("/", File(uploadService.config.uploadPath))
    }
}
