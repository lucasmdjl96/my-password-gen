package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*
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
            val oldSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0999")
            val client = createAndConfigureClientWithCookie(oldSessionId)
            val sessionNumber = testTransaction {
                Sessions.selectAll().count()
            }
            val response = client.put(SessionRoute())
            val sessionId = response.getSessionIdFromCookie()
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(sessionId)
            assertNotEquals(oldSessionId, sessionId)
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
            val client = createAndConfigureClientWithoutCookie()
            val sessionNumber = testTransaction {
                Sessions.selectAll().count()
            }
            val response = client.put(SessionRoute())
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
            val oldSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
            val client = createAndConfigureClientWithCookie(oldSessionId)
            var sessionNumber = 0L
            var oldUsers = emptyList<Int>()
            testTransaction {
                sessionNumber = Sessions.selectAll().count()
                oldUsers = User.find { Users.sessionId eq oldSessionId }.map { it.id.value }
            }
            val response = client.put(SessionRoute())
            val sessionId = response.getSessionIdFromCookie()
            assertEquals(HttpStatusCode.OK, response.status)
            assertNotNull(sessionId)
            assertNotEquals(oldSessionId, sessionId)
            testTransaction {
                assertEquals(sessionNumber, Sessions.selectAll().count())
                assertNull(Session.findById(oldSessionId))
                val session = Session.findById(sessionId)
                assertNotNull(session)
                assertNull(session.lastUser)
                User.forIds(oldUsers).forEach { user ->
                    assertEquals(sessionId, user.session.id.value)
                }
            }
        }

    }

}
