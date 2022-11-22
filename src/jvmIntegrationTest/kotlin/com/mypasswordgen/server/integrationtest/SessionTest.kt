/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.integrationtest

import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionTest : TestParent() {

    @Nested
    inner class PutSession {

        @Test
        fun `create new session when bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initSessionId2 = UUID.fromString("f7e628f1-9afe-475a-9c57-9426bd45596d")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val sessionNumber = testTransaction {
                Sessions.selectAll().count()
            }
            val response = client.put(SessionRoute.Update())
            val sessionId = response.getSessionIdFromCookie()
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(sessionId)
            assertNotEquals(initSessionId2, sessionId)
            testTransaction {
                assertEquals(sessionNumber + 1, Sessions.selectAll().count())
                val session = Session.findById(sessionId)
                assertNotNull(session)
                assertNull(session.lastUser)
                assertEmpty(User.find { Users.sessionId eq sessionId })
            }
        }

        @Test
        fun `create new session when no cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithoutCookie()
            val sessionNumber = testTransaction {
                Sessions.selectAll().count()
            }
            val response = client.put(SessionRoute.Update())
            val sessionId = response.getSessionIdFromCookie()
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(sessionId)
            testTransaction {
                assertEquals(sessionNumber + 1, Sessions.selectAll().count())
                val session = Session.findById(sessionId)
                assertNotNull(session)
                assertNull(session.lastUser)
                assertEmpty(User.find { Users.sessionId eq sessionId })
            }
        }

        @Test
        fun `update session when good cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            var sessionNumber = 0L
            var oldUsers = emptyList<UUID>()
            testTransaction {
                sessionNumber = Sessions.selectAll().count()
                oldUsers = User.find { Users.sessionId eq initSessionId }.map { it.id.value }
            }
            val response = client.put(SessionRoute.Update())
            val sessionId = response.getSessionIdFromCookie()
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(sessionId)
            assertNotEquals(initSessionId, sessionId)
            testTransaction {
                assertEquals(sessionNumber, Sessions.selectAll().count())
                assertNull(Session.findById(initSessionId))
                val session = Session.findById(sessionId)
                assertNotNull(session)
                assertNull(session.lastUser)
                User.forIds(oldUsers).forEach { user ->
                    assertEquals(sessionId, user.session.id.value)
                }
            }
        }

    }

    @Nested
    inner class Export {

        @Test
        fun `export session with users`() = testApplication {
            val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            val initUserId2 = UUID.fromString("7f73c61e-0b3a-4a01-9099-6978ba73b72c")
            val initUsername2 = "User234"
            val initUsernameEncoded2 = initUsername2.encode()
            val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
            val initEmailAddress = "Email001"
            val initEmailAddressEncoded = initEmailAddress.encode()
            val initEmailId2 = UUID.fromString("85581b81-96ee-4086-b0b1-81da47e10422")
            val initEmailAddress2 = "Email002"
            val initEmailAddressEncoded2 = initEmailAddress2.encode()
            val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
            val initSiteName = "Site001"
            val initSiteNameEncoded = initSiteName.encode()
            val initSiteId2 = UUID.fromString("a42836d0-a6ca-4973-a6f1-c90fc9c6a352")
            val initSiteName2 = "SiteXYZ"
            val initSiteNameEncoded2 = initSiteName2.encode()
            val fullSessionClientDto = FullSessionClientDto {
                +FullUserClientDto {
                    +FullEmailClientDto(initEmailId.toString()) {
                        +FullSiteClientDto(initSiteId.toString())
                        +FullSiteClientDto(initSiteId2.toString())
                    }
                    +FullEmailClientDto(initEmailId2.toString())
                }
                +FullUserClientDto()
            }
            testTransaction {
                exec(
                    """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsernameEncoded2', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId2', '$initEmailAddressEncoded2', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteNameEncoded', '$initEmailId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId2', '$initSiteNameEncoded2', '$initEmailId');
                    """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.get(SessionRoute.Export())
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(response.body())
            val responseBody = response.body<FullSessionClientDto>()
            assertEquals(fullSessionClientDto, responseBody)
        }

        @Test
        fun `export session without users`() = testApplication {
            val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
            val fullSessionClientDto = FullSessionClientDto()
            testTransaction {
                exec(
                    """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.get(SessionRoute.Export())
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(response.body())
            val responseBody = response.body<FullSessionClientDto>()
            assertEquals(fullSessionClientDto, responseBody)
        }

        @Test
        fun `export session with bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initSessionId2 = UUID.fromString("f7e628f1-9afe-475a-9c57-9426bd45596d")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val response = client.get(SessionRoute.Export())
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `export session with no cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(SessionRoute.Export())
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    }

    @Nested
    inner class Import {

        @Test
        fun `import session with no cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithoutCookie()
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(FullSessionServerDto())
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `import session with bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initSessionId2 = UUID.fromString("2d77d5f2-a15f-47dc-beda-4a3151688c7e")
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(FullSessionServerDto())
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `import session with conflicting sites`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val emailAddress = "email1"
            val siteName = "site1"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val fullSessionServerDto = FullSessionServerDto {
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName)
                        +FullSiteServerDto(siteName)
                    }
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullSessionServerDto)
            }
            assertEquals(HttpStatusCode.Conflict, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEmpty(Users.select( Users.sessionId eq initSessionId))
            }
        }

        @Test
        fun `import session with conflicting emails`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val emailAddress = "email1"
            val siteName = "site1"
            val siteName2 = "site2"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val fullSessionServerDto = FullSessionServerDto {
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName)
                    }
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName2)
                    }
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullSessionServerDto)
            }
            assertEquals(HttpStatusCode.Conflict, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEmpty(Users.select( Users.sessionId eq initSessionId))
            }
        }

        @Test
        fun `import session with conflicting users`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val emailAddress = "email1"
            val emailAddress2 = "email2"
            val siteName = "site1"
            val siteName2 = "site2"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val fullSessionServerDto = FullSessionServerDto {
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName)
                    }
                }
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress2) {
                        +FullSiteServerDto(siteName2)
                    }
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullSessionServerDto)
            }
            assertEquals(HttpStatusCode.Conflict, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEmpty(Users.select( Users.sessionId eq initSessionId))
            }
        }

        @Test
        fun `import session with blank existing session`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val username2 = "User321"
            val emailAddress = "email1"
            val emailAddress2 = "email2"
            val siteName = "site1"
            val siteName2 = "site2"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val fullSessionServerDto = FullSessionServerDto {
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName)
                        +FullSiteServerDto(siteName2)
                    }
                    +FullEmailServerDto(emailAddress2)
                }
                +FullUserServerDto(username2)
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullSessionServerDto)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val sessionId = response.getSessionIdFromCookie()
            assertNotNull(sessionId)
            testTransaction {
                assertNull(Session.findById(initSessionId))
                assertNotNull(Session.findById(sessionId))
                assertEquals(2, Users.select { Users.sessionId eq sessionId }.count())
                assertEquals(2, Emails.selectAll().count())
                assertEquals(2, Sites.selectAll().count())
            }
        }

        @Test
        fun `import session with non-blank existing session`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val emailAddress = "email1"
            val siteName = "site1"

            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            val initUserId2 = UUID.fromString("7f73c61e-0b3a-4a01-9099-6978ba73b72c")
            val initUsername2 = "User234"
            val initUsernameEncoded2 = initUsername2.encode()
            val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
            val initEmailAddress = "Email001"
            val initEmailAddressEncoded = initEmailAddress.encode()
            val initEmailId2 = UUID.fromString("85581b81-96ee-4086-b0b1-81da47e10422")
            val initEmailAddress2 = "Email002"
            val initEmailAddressEncoded2 = initEmailAddress2.encode()
            val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
            val initSiteName = "Site001"
            val initSiteNameEncoded = initSiteName.encode()
            val initSiteId2 = UUID.fromString("a42836d0-a6ca-4973-a6f1-c90fc9c6a352")
            val initSiteName2 = "SiteXYZ"
            val initSiteNameEncoded2 = initSiteName2.encode()
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId2', '$initUsernameEncoded2', '$initSessionId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId2', '$initEmailAddressEncoded2', '$initUserId');
                    INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId', '$initSiteNameEncoded', '$initEmailId');
                    INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId2', '$initSiteNameEncoded2', '$initEmailId');
                """.trimIndent()
                )
            }
            val fullSessionServerDto = FullSessionServerDto {
                +FullUserServerDto(username) {
                    +FullEmailServerDto(emailAddress) {
                        +FullSiteServerDto(siteName)
                    }
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(SessionRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullSessionServerDto)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val sessionId = response.getSessionIdFromCookie()
            assertNotNull(sessionId)
            testTransaction {
                assertNull(Session.findById(initSessionId))
                assertNotNull(Session.findById(sessionId))
                assertEquals(1, Users.select { Users.sessionId eq sessionId }.count())
                assertEquals(1, Emails.selectAll().count())
                assertEquals(1, Sites.selectAll().count())
            }
        }

    }

}
