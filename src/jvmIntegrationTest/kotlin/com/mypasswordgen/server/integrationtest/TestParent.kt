package com.mypasswordgen.server.integrationtest

import com.mypasswordgen.server.plugins.*
import com.mypasswordgen.server.plugins.routing.installRoutes
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestParent {

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14.5-alpine")
            .withDatabaseName("postgresTestDatabase")
            .withUsername("postgres")
            .withPassword("postgres")

        private lateinit var database: Database

    }

    @BeforeAll
    fun installTestDatabase() {
        database =
            Database.connect(postgres.jdbcUrl, postgres.driverClassName, postgres.username, postgres.password)
    }

    @BeforeEach
    fun reinitializeDatabase() {
        transaction(database) {
            SchemaUtils.drop(Sessions, Users, Emails, Sites)
            SchemaUtils.create(Sessions, Users, Emails, Sites)
        }
    }

    fun ApplicationTestBuilder.createAndConfigureClientWithoutCookie(): HttpClient =
        createClient {
            install(DefaultRequest) {
                host = "localhost"
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
            install(Resources)
            install(HttpCookies)
            install(ContentNegotiation) {
                json()
            }
        }

    fun ApplicationTestBuilder.createAndConfigureClientWithCookie(sessionId: UUID): HttpClient {
        val encodedSessionId = sessionId.toString().replace("-", "%2D")
        return createClient {
            install(DefaultRequest) {
                host = "localhost"
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
            install(Resources)
            install(HttpCookies) {
                runBlocking {
                    storage.addCookie(
                        "https://localhost:443", Cookie(
                            "session",
                            "sessionId%3D%2523s$encodedSessionId",
                            CookieEncoding.RAW
                        )
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }

    fun <T> testTransaction(block: Transaction.() -> T) = transaction(database) {
        block()
    }

    fun HttpResponse.getSessionIdFromCookie(): UUID? = runBlocking {
        val stringUUID =
            this@getSessionIdFromCookie.headers["Set-Cookie"]
                ?.substringAfter("sessionId%3D%2523s")
                ?.substringBefore(';')
                ?.replace("%2D", "-")
        if (stringUUID == null) null
        else UUID.fromString(stringUUID)
    }

}


fun Application.module() {
    installKoin()
    installContentNegotiation()
    installCompression()
    installSessions()
    installAuthentication()
    installResources()
    installStatusPages()
    installRoutes()
    installCallLogging()
}