package org.patifiner.app

import io.ktor.server.config.MapApplicationConfig
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import org.patifiner.auth.authModule
import org.patifiner.base.coreModule
import org.patifiner.check.di.checkModule
import org.patifiner.database.databaseModule
import org.patifiner.events.eventsModule
import org.patifiner.geo.geoModule
import org.patifiner.relations.relationsModule
import org.patifiner.search.searchModule
import org.patifiner.topics.topicsModule
import org.patifiner.upload.uploadModule
import org.patifiner.user.userModule

@OptIn(KoinExperimentalAPI::class)
class KoinModuleTest : KoinTest {

    @Test
    fun `verify full application koin graph`() {
        val fakeConfig = MapApplicationConfig().apply {
            put("jwt.secret", "test-secret-at-least-32-chars-long")
            put("jwt.audience", "test")
            put("jwt.realm", "test")
            put("jwt.issuer", "test")
            put("jwt.expiration", "3600")
            put("database.url", "jdbc:h2:mem:test")
            put("database.driver", "org.h2.Driver")
            put("database.user", "sa")
            put("database.password", "")
            put("upload.path", "test")
            put("upload.baseUrl", "test")
            put("upload.maxSize", "5")
        }

        module {
            includes(
                coreModule,
                authModule(fakeConfig),
                checkModule(fakeConfig),
                databaseModule(fakeConfig),
                uploadModule(fakeConfig),
                userModule,
                topicsModule,
                searchModule,
                geoModule,
                eventsModule,
                relationsModule
            )
        }.verify()
    }
}
