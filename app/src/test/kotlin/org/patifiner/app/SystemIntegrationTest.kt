package org.patifiner.app

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test
import org.patifiner.base.PagedRequest
import org.patifiner.relations.api.UpdateRelationRequest
import org.patifiner.relations.api.relationsRoutes
import org.patifiner.relations.relationsModule
import org.patifiner.search.api.searchRoutes
import org.patifiner.search.searchModule
import org.patifiner.testing.BaseIntegrationTest
import org.patifiner.topics.topicsModule
import org.patifiner.user.UserDto
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UserCreatedResponse
import org.patifiner.user.api.userRoutes
import org.patifiner.user.userModule
import kotlin.test.assertTrue

class SystemIntegrationTest : BaseIntegrationTest(
    listOf(userModule, relationsModule, searchModule, topicsModule)
) {

    @Test
    fun `blocked user should disappear from search results`() = ptfTest(
        routeBlock = {
            userRoutes()
            relationsRoutes()
            searchRoutes()
        }
    ) { client ->
        // 1. Регистрируем пользователя А (тот кто блокирует)
        val userAResponse = client.post("/user/create") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("User A", "a@ptf.ru", "pass123"))
        }
        assertResponseStatus(HttpStatusCode.OK, userAResponse)
        val userA = userAResponse.body<UserCreatedResponse>()
        val tokenA = userA.token.accessToken

        // 2. Регистрируем пользователя Б (тот кого заблокируют)
        val userBResponse = client.post("/user/create") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("User B", "b@ptf.ru", "pass123"))
        }
        assertResponseStatus(HttpStatusCode.OK, userBResponse)
        val userB = userBResponse.body<UserCreatedResponse>()
        val tokenB = userB.token.accessToken
        val userIdB = userB.userInfo.id

        // 3. Пользователь A блокирует Пользователя B
        val blockResponse = client.post("/user/relations/block") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody(UpdateRelationRequest(targetUserId = userIdB, status = org.patifiner.database.enums.RelationStatus.BLOCKED))
        }
        assertResponseStatus(HttpStatusCode.OK, blockResponse)

        // 4. Пользователь B пытается найти пользователей
        val searchResponse = client.post("/search/users") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
            contentType(ContentType.Application.Json)
            setBody(PagedRequest(page = 0, perPage = 10))
        }

        assertResponseStatus(HttpStatusCode.OK, searchResponse)
        val results = searchResponse.body<List<UserDto>>()

        // Проверяем, что Пользователь А исчез из выдачи для Пользователя Б
        assertTrue(results.none { it.name == "User A" }, "Blocked User A should not be visible to User B")
    }
}
