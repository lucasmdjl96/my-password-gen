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
                val client = createAndConfigureClientWithoutCookie()
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `login with bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
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
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val session = Session.findById(sessionId)
                    assertNotNull(session)
                    assertNull(session.lastUser)
                }
            }

            @Test
            fun `login with good cookie and username from other session`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("User009"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val session = Session.findById(sessionId)
                    assertNotNull(session)
                    assertNull(session.lastUser)
                }
            }

            @Test
            fun `login with good cookie and existing username`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                testTransaction {
                    val user = User.findById(2)!!
                    user.username = "User002".encode()
                }
                val response = client.post(UserRoute.Login()) {
                    setBody(UserServerDto("User002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals("User002".encode(), responseBody.username)
                assertEquals(mutableListOf("email002", "email003"), responseBody.emailList)
                testTransaction {
                    val session = Session.findById(sessionId)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals(2, session.lastUser!!.id.value)
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
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
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
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals("UserXYZ".encode(), responseBody.username)
                assertEquals(mutableListOf(), responseBody.emailList)
                testTransaction {
                    assertEquals(usersBefore + 1, Users.selectAll().count())
                    val user = User.find { Users.sessionId eq sessionId and (Users.username eq "UserXYZ".encode()) }
                    assertTrue(user.count() == 1L)
                    assertNotNull(user.first())
                    assertNull(user.first().lastEmail)
                    val session = Session.findById(sessionId)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals("UserXYZ".encode(), session.lastUser!!.username)
                    assertEquals(sessionId, session.lastUser!!.session.id.value)
                }
            }

            @Test
            fun `register with good cookie and username from other session`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto("User009"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<UserClientDto>()
                assertNotNull(responseBody)
                assertEquals("User009".encode(), responseBody.username)
                assertEquals(mutableListOf(), responseBody.emailList)
                testTransaction {
                    assertEquals(usersBefore + 1, Users.selectAll().count())
                    val user = User.find { Users.sessionId eq sessionId and (Users.username eq "User009".encode()) }
                    assertTrue(user.count() == 1L)
                    assertNotNull(user.first())
                    assertNull(user.first().lastEmail)
                    val session = Session.findById(sessionId)
                    assertNotNull(session)
                    assertNotNull(session.lastUser)
                    assertEquals("User009".encode(), session.lastUser!!.username)
                    assertEquals(sessionId, session.lastUser!!.session.id.value)
                }
            }

            @Test
            fun `register with good cookie and existing username`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
                val client = createAndConfigureClientWithCookie(sessionId)
                testTransaction {
                    val user = User.findById(2)!!
                    user.username = "User002".encode()
                }
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.post(UserRoute.Register()) {
                    setBody(UserServerDto("User002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Conflict, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    val session = Session.findById(sessionId)
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
                val client = createAndConfigureClientWithoutCookie()
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("UserXYZ"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `logout with bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
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
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002")
                val client = createAndConfigureClientWithCookie(sessionId)
                testTransaction {
                    val user = User.findById(6)!!
                    user.username = "User006".encode()
                }
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("User006".encode()))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNull(Session.findById(sessionId)!!.lastUser)
                    assertNull(User.findById(6)!!.lastEmail)
                }
            }

            @Test
            fun `logout with good cookie and username not last`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002")
                val client = createAndConfigureClientWithCookie(sessionId)
                testTransaction {
                    val user = User.findById(6)!!
                    user.username = "User006".encode()
                }
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("User007".encode()))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNotNull(Session.findById(sessionId)!!.lastUser)
                }
            }

            @Test
            fun `logout with good cookie and non-existing username`() = testApplication {
                val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002")
                val client = createAndConfigureClientWithCookie(sessionId)
                testTransaction {
                    val user = User.findById(6)!!
                    user.username = "User006".encode()
                }
                val usersBefore = testTransaction {
                    Users.selectAll().count()
                }
                val response = client.patch(UserRoute.Logout()) {
                    setBody(UserServerDto("UserXYZ".encode()))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(usersBefore, Users.selectAll().count())
                    assertNotNull(Session.findById(sessionId)!!.lastUser)
                }
            }
        }

    }

}
