package org.patifiner.testing

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.test.KoinTest
import org.koin.test.get
import org.patifiner.auth.generateToken
import org.patifiner.auth.installPtfAuth
import org.patifiner.base.ErrorResponse
import org.patifiner.base.PtfDbConfig
import org.patifiner.base.PtfJwtConfig
import org.patifiner.base.configurePtfDefaults
import org.patifiner.base.installPtfSerialization
import org.patifiner.database.tables.CitiesTable
import org.patifiner.database.tables.EventParticipantsTable
import org.patifiner.database.tables.EventTopicsTable
import org.patifiner.database.tables.EventsTable
import org.patifiner.database.tables.TopicsTable
import org.patifiner.database.tables.UserRelationsTable
import org.patifiner.database.tables.UserTable
import org.patifiner.database.tables.UserTopicsTable
import kotlin.test.assertEquals


abstract class BaseIntegrationTest(private val extraModules: List<Module> = emptyList()) : KoinTest {

    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension { extraModules }

    @BeforeEach
    fun setup() {
        val dbConfig = get<PtfDbConfig>()
        setupTestDatabase(dbConfig)
    }

    fun ptfTest(routeBlock: Route.() -> Unit, testBlock: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
        testApplication {
            application {
                install(Koin) {
                    modules(KoinTestExtension.testModules() + extraModules)
                }

                installPtfSerialization()

                install(StatusPages) {
                    exception<Throwable> { call, cause ->
                        println("--- TEST SERVER ERROR ---")
                        cause.printStackTrace()
                        println("-------------------------")
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("INTERNAL_ERROR", cause.message ?: "Unknown")
                        )
                    }
                }

                installPtfAuth(getKoin().get<PtfJwtConfig>())

                routing { routeBlock() }
            }
            testBlock(createTestClient())
        }

    fun ApplicationTestBuilder.createTestClient(): HttpClient {
        return createClient {
            install(ContentNegotiation) {
                jackson { configurePtfDefaults() }
            }
        }
    }

    suspend fun assertResponseStatus(expected: HttpStatusCode, response: HttpResponse) {
        assertEquals(expected, response.status, "Expected status $expected but got ${response.status}. Body: ${response.bodyAsText()}")
    }

    fun generateTestToken(userId: Long): String {
        val jwtConfig = get<PtfJwtConfig>()
        return generateToken(jwtConfig, userId)
    }

    private fun setupTestDatabase(config: PtfDbConfig) {
        Database.connect(
            url = config.url,
            driver = config.driver,
            user = config.user,
            password = config.password
        )

        transaction {
            SchemaUtils.create(
                UserTable,
                CitiesTable,
                TopicsTable,
                UserTopicsTable,
                UserRelationsTable,
                EventsTable,
                EventTopicsTable,
                EventParticipantsTable,
            )
        }
    }
}
