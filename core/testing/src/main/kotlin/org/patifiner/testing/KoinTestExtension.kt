package org.patifiner.testing

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.patifiner.base.PtfDbConfig
import org.patifiner.base.PtfJwtConfig
import org.patifiner.base.PtfUploadConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class KoinTestExtension(
    private val extraModules: () -> List<Module> = { emptyList() }
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        stopKoin()
        startKoin {
            modules(testModules() + extraModules())
        }
    }

    override fun afterEach(context: ExtensionContext?) {
        stopKoin()
    }

    companion object {
        fun testModules() = listOf(module {
            val testUuid = UUID.randomUUID().toString()

            single<Logger> { LoggerFactory.getLogger("Test") }

            single {
                PtfDbConfig(
                    url = "jdbc:h2:mem:test_$testUuid;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                    driver = "org.h2.Driver",
                    user = "sa",
                    password = ""
                )
            }

            single {
                PtfJwtConfig(
                    secret = "test-secret-key-at-least-32-chars-long",
                    audience = "test-audience",
                    realm = "test-realm",
                    issuer = "test-issuer",
                    accessTokenExpirationMs = 3600000,
                    refreshTokenExpirationMs = 2592000000
                )
            }

            single {
                PtfUploadConfig(
                    uploadPath = "build/test-uploads",
                    baseUrl = "http://localhost:8080/files",
                    maxFileSizeMB = 5
                )
            }
        })
    }
}
