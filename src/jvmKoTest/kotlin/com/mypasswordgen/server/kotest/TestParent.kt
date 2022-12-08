/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.kotest

import com.mypasswordgen.server.plugins.*
import com.mypasswordgen.server.plugins.routing.installRoutes
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.test.TestScope
import io.kotest.extensions.testcontainers.JdbcTestContainerExtension
import io.kotest.extensions.testcontainers.LifecycleMode
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.property.PropertyTesting
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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.error.KoinAppAlreadyStartedException
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

const val maxSize = 5

object KotestConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        PropertyTesting.defaultIterationCount = System.getProperty("kotestCount")?.toIntOrNull() ?: 10
    }
}

infix fun String.and(other: String) = "$this and $other"

class TestTransaction(private val database: Database) {
    operator fun <T> invoke(statement: Transaction.() -> T) = transaction(database, statement)
}

val postgres = PostgreSQLContainer<Nothing>("postgres:14.5-alpine").apply {
    withDatabaseName("postgresTestDatabase")
    withPassword("postgres")
    withUsername("postgres")
}

fun FunSpec.initDatabase(): TestTransaction {
    val dataSource = install(JdbcTestContainerExtension(postgres, LifecycleMode.EveryTest))
    val databaseConfig = DatabaseConfig {
        sqlLogger = object : SqlLogger {
            override fun log(context: StatementContext, transaction: Transaction) {
                //Do Nothing
            }
        }
    }
    val database = Database.connect(dataSource, databaseConfig = databaseConfig)
    val testTransaction = TestTransaction(database)
    beforeTest {
        testTransaction {
            SchemaUtils.create(Sessions, Users, Emails, Sites)
        }
    }
    return testTransaction
}

fun Transaction.cleanUp() = this.exec("TRUNCATE TABLE SESSIONS CASCADE;")

suspend fun FunSpecContainerScope.should(name: String, test: suspend TestScope.() -> Unit) = test("should $name", test)

fun ApplicationTestBuilder.createAndConfigureClientWithCookie(sessionId: UUID?): HttpClient {
    val encodedSessionId = sessionId?.toString()?.replace("-", "%2D")
    return createClient {
        install(DefaultRequest) {
            host = "localhost"
            port = 443
            url { protocol = URLProtocol.HTTPS }
        }
        install(Resources)
        install(HttpCookies) {
            if (encodedSessionId != null) runBlocking {
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

fun HttpResponse.getSessionIdFromCookie(): UUID? = runBlocking {
    val stringUUID =
        this@getSessionIdFromCookie.headers["Set-Cookie"]
            ?.substringAfter("sessionId%3D%2523s")
            ?.substringBefore(';')
            ?.replace("%2D", "-")
    if (stringUUID == null) null
    else UUID.fromString(stringUUID)
}

infix fun HttpResponse.shouldHaveContentType(contentType: ContentType) = this should haveContentType(contentType)
fun haveContentType(contentType: ContentType) = object : Matcher<HttpResponse> {
    override fun test(value: HttpResponse): MatcherResult {
        return MatcherResult(
            value.contentType()?.withoutParameters() == contentType,
            { "Response should have ContentType $contentType= but was ${value.contentType()?.withoutParameters()}" },
            {
                "Response should not have ContentType $contentType"
            }
        )
    }
}

fun Application.module() {
    try {
        installKoin()
    } catch (e: KoinAppAlreadyStartedException) {
        // Do nothing
    }
    installContentNegotiation()
    installCompression()
    installSessions()
    installAuthentication()
    installResources()
    installStatusPages()
    installRoutes()
    installCallLogging()
}
