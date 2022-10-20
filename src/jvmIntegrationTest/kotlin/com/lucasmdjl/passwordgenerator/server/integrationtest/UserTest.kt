package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and
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
                assertEquals(initUsernameEncoded, responseBody.username)
                assertEquals(mutableListOf(initEmailAddress, initEmailAddress2), responseBody.emailList)
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
                assertEquals(initUsername2Encoded, responseBody.username)
                assertEquals(mutableListOf(), responseBody.emailList)
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
                assertEquals(initUsernameEncoded, responseBody.username)
                assertEquals(mutableListOf(), responseBody.emailList)
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

}
