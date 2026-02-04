package org.patifiner.upload

import io.ktor.client.request.forms.*
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.server.config.*
import org.junit.jupiter.api.Test
import org.patifiner.testing.BaseIntegrationTest
import org.patifiner.upload.api.uploadRoutes
import kotlin.test.assertEquals

class UploadIntegrationTest : BaseIntegrationTest(listOf(
    uploadModule(MapApplicationConfig().apply {
        put("upload.path", "build/test-uploads")
        put("upload.baseUrl", "http://localhost:8080/files")
        put("upload.maxSize", "5")
    })
)) {

    @Test
    fun `should upload image successfully`() = ptfTest(
        routeBlock = { uploadRoutes() }
    ) { client ->
        val response = client.post("/upload/image") {
            header(HttpHeaders.Authorization, "Bearer ${generateTestToken(userId = 1L)}")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("image", byteArrayOf(1, 2, 3), Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"test.png\"; name=\"image\"")
                        })
                    }
                ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}
