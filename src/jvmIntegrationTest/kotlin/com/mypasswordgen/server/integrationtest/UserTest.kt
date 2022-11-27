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

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserTest : TestParent() {

    @Nested
    inner class Login {

        @Nested
        inner class Challenged {

            @Test
            fun `login without cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `login with bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }

        @Nested
        inner class Validated {

            @Test
            fun `login with good cookie and non-existing username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2 = "UserXYZ"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto(initUsername2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val session = Session.findById(initSessionId)
                    assertNotNull(session)
                    assertNull(session.lastUser)
                }
            }

            @Test
            fun `login with good cookie and username from other session`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("87302283-27a9-4823-bca2-2e418fa55081")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUserId2 = UUID.fromString("ae83b738-3bfc-4156-9eec-777a53f2f662")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2 = "UserXYZ"
                val initUsername2Encoded = initUsername2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId2');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto(initUsername2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val session = Session.findById(initSessionId)
                    assertNotNull(session)
                    assertNull(session.lastUser)
                }
            }

            @Test
            fun `login with good cookie and existing username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailId2 = UUID.fromString("cc5ead72-7345-4897-aed7-aad45b3eb2d3")
                val initEmailAddress = "Email001"
                val initEmailAddress2 = "Email002"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto(initUsername))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals(initUserId.toString(), responseBody.id)
                assertEquals(
                    setOf(initEmailId.toString(), initEmailId2.toString()),
                    responseBody.emailIdSet
                )
                testTransaction {
                    val session = Session.findById(initSessionId)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals(initUserId, session.lastUser!!.id.value)
                }
            }
        }

    }

    @Nested
    inner class Register {

        @Nested
        inner class Challenged {

            @Test
            fun `register without cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                }
            }

            @Test
            fun `register with bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                }
            }
        }

        @Nested
        inner class Validated {

            @Test
            fun `register with good cookie and non-existing username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2 = "UserXYZ"
                val initUsername2Encoded = initUsername2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto(initUsername2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals(setOf(), responseBody.emailIdSet)
                testTransaction {
                    assertEquals(usersBefore + 1, Users.selectAll().count())
                    val user =
                        User.find { Users.sessionId eq initSessionId and (Users.username eq initUsername2Encoded) }
                    assertTrue(user.count() == 1L)
                    assertNotNull(user.first())
                    assertNull(user.first().lastEmail)
                    val session = Session.findById(initSessionId)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals(initUsername2Encoded, session.lastUser!!.username)
                    assertEquals(initSessionId, session.lastUser!!.session.id.value)
                }
            }

            @Test
            fun `register with good cookie and username from other session`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("2a81c217-52e8-4ee3-aace-95e8462ccf70")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId2');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto(initUsername))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals(setOf(), responseBody.emailIdSet)
                testTransaction {
                    assertEquals(usersBefore + 1, Users.selectAll().count())
                    val user =
                        User.find { Users.sessionId eq initSessionId2 and (Users.username eq initUsernameEncoded) }
                    assertTrue(user.count() == 1L)
                    assertNotNull(user.first())
                    assertNull(user.first().lastEmail)
                    val session = Session.findById(initSessionId2)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals(initUsernameEncoded, session.lastUser!!.username)
                    assertEquals(initSessionId2, session.lastUser!!.session.id.value)
                }
            }

            @Test
            fun `register with good cookie and existing username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto(initUsername))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Conflict, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    val session = Session.findById(initSessionId)
                    assertNotNull(session)
                    assertNull(session.lastUser)
                }
            }
        }
    }

    @Nested
    inner class Logout {

        @Nested
        inner class Challenged {

            @Test
            fun `logout without cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `logout with bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `logout with good cookie and last username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("d962f6d5-4758-44f4-80dc-fcf5edf31d33")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto(initUsername))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNull(Session.findById(initSessionId)!!.lastUser)
                    assertNull(User.findById(initUserId)!!.lastEmail)
                }
            }

            @Test
            fun `logout with good cookie and username not last`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initUserId2 = UUID.fromString("9c1dc390-30c4-4b3a-b1fd-5b99bac62c53")
                val initUsername2 = "UserXYZ"
                val initUsername2Encoded = initUsername2.encode()
                val initEmailId = UUID.fromString("d962f6d5-4758-44f4-80dc-fcf5edf31d33")
                val initEmailAddress = "Email001"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto(initUsername2Encoded))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNotNull(Session.findById(initSessionId)!!.lastUser)
                }
            }

            @Test
            fun `logout with good cookie and no last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("d962f6d5-4758-44f4-80dc-fcf5edf31d33")
                val initEmailAddress = "Email001"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto(initUsernameEncoded))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNull(Session.findById(initSessionId)!!.lastUser)
                }
            }

            @Test
            fun `logout with good cookie and non-existing username`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2 = "UserABC"
                val initUsername2Encoded = initUsername2.encode()
                val initEmailId = UUID.fromString("d962f6d5-4758-44f4-80dc-fcf5edf31d33")
                val initEmailAddress = "Email001"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto(initUsername2Encoded))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNotNull(Session.findById(initSessionId)!!.lastUser)
                }
            }
        }

    }

    @Nested
    inner class Export {

        @Test
        fun `export user with emails`() = testApplication {
            val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
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
            val fullUserClientDto = FullUserClientDto {
                +FullEmailClientDto(initEmailId.toString()) {
                    +FullSiteClientDto(initSiteId.toString())
                    +FullSiteClientDto(initSiteId2.toString())
                }
                +FullEmailClientDto(initEmailId2.toString())
            }

            testTransaction {
                exec(
                    """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val response = client.get(UserRoute.Export(initUsername))
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(response.body())
            val responseBody = response.body<FullUserClientDto>()
            assertEquals(fullUserClientDto, responseBody)
        }

        @Test
        fun `export user without emails`() = testApplication {
            val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            val fullUserClientDto = FullUserClientDto()
            testTransaction {
                exec(
                    """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                    """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.get(UserRoute.Export(initUsername))
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(response.body())
            val responseBody = response.body<FullUserClientDto>()
            assertEquals(fullUserClientDto, responseBody)
        }

        @Test
        fun `export user with bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initSessionId2 = UUID.fromString("f7e628f1-9afe-475a-9c57-9426bd45596d")
            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val response = client.get(UserRoute.Export(initUsername))
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `export user with no cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(UserRoute.Export(initUsername))
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    }

    @Nested
    inner class Import {

        @Test
        fun `import user with no cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithoutCookie()
            val response = client.post(UserRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(FullUserServerDto(username))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `import user with bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initSessionId2 = UUID.fromString("2d77d5f2-a15f-47dc-beda-4a3151688c7e")
            val username = "User123"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val response = client.post(UserRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(FullUserServerDto(username))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        fun `import user with conflicting users`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val initUserId = UUID.fromString("d2a25f84-0564-4fc3-b6cd-d1380b37b4fb")
            val initUsername = "User123"
            val initUsernameEncoded = initUsername.encode()
            val emailAddress = "email1"
            val emailAddress2 = "email2"
            val siteName = "site1"
            val siteName2 = "site2"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                """.trimIndent()
                )
            }
            val fullUserServerDto = FullUserServerDto(initUsername) {
                +FullEmailServerDto(emailAddress) {
                    +FullSiteServerDto(siteName)
                }
                +FullEmailServerDto(emailAddress2) {
                    +FullSiteServerDto(siteName)
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(UserRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullUserServerDto)
            }
            assertEquals(HttpStatusCode.Conflict, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEquals(1, Users.select { Users.sessionId eq initSessionId }.count())
                assertNotNull(User.findById(initUserId))
            }
        }

        @Test
        fun `import user with blank existing session`() = testApplication {
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
            val fullUserServerDto = FullUserServerDto(username) {
                +FullEmailServerDto(emailAddress) {
                    +FullSiteServerDto(siteName)
                    +FullSiteServerDto(siteName2)
                }
                +FullEmailServerDto(emailAddress2)
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(UserRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullUserServerDto)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEquals(1, Users.select { Users.sessionId eq initSessionId }.count())
                assertEquals(2, Emails.selectAll().count())
                assertEquals(2, Sites.selectAll().count())
            }
        }

        @Test
        fun `import user with non-blank existing session`() = testApplication {
            val initSessionId = UUID.fromString("4f272978-493c-4e4e-a39f-71629c065e4e")
            val username = "User123"
            val emailAddress = "Email001"
            val siteName = "Site001"

            val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
            val initUsername = "UserABC"
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
            val fullUserServerDto = FullUserServerDto(username) {
                +FullEmailServerDto(emailAddress) {
                    +FullSiteServerDto(siteName)
                }
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.post(UserRoute.Import()) {
                contentType(ContentType.Application.Json)
                setBody(fullUserServerDto)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            testTransaction {
                assertNotNull(Session.findById(initSessionId))
                assertEquals(3, Users.select { Users.sessionId eq initSessionId }.count())
                assertEquals(3, Emails.selectAll().count())
                assertEquals(3, Sites.selectAll().count())
            }
        }

    }

}
