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
            val response = client.put(SessionRoute())
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
            val response = client.put(SessionRoute())
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

}
