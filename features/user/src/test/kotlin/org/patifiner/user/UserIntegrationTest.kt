package org.patifiner.user

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test
import org.patifiner.testing.BaseIntegrationTest
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.TokenRequest
import org.patifiner.user.api.TokenResponse
import org.patifiner.user.api.UserCreatedResponse
import org.patifiner.user.api.userRoutes
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserIntegrationTest : BaseIntegrationTest(listOf(userModule)) {

    @Test
    fun `test full auth cycle`() = ptfTest(
        routeBlock = { userRoutes() }
    ) { client ->
        // 1. Регистрация
        val registerResponse = client.post("/user/create") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("Test User", "test@example.com", "password123"))
        }
        assertResponseStatus(HttpStatusCode.OK, registerResponse)
        val registerData = registerResponse.body<UserCreatedResponse>()
        assertNotNull(registerData.token.accessToken)

        // 2. Логин
        val loginResponse = client.post("/user/login") {
            contentType(ContentType.Application.Json)
            setBody(TokenRequest("test@example.com", "password123"))
        }
        assertResponseStatus(HttpStatusCode.OK, loginResponse)
        val loginData = loginResponse.body<TokenResponse>()
        val token = loginData.accessToken

        // 3. Получение профиля (Get Me)
        val meResponse = client.get("/user/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertResponseStatus(HttpStatusCode.OK, meResponse)
        val meData = meResponse.body<UserDto>()
        assertEquals("test@example.com", meData.email)
        assertEquals("Test User", meData.name)
    }
}
