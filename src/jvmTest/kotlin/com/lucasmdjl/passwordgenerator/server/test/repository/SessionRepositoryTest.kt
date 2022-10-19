package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class Create {

        @Test
        fun `create a new session`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val sessionsBefore = Sessions.selectAll()
            val beforeCount = sessionsBefore.count()
            val existingIds = sessionsBefore.map { it[Sessions.id].value.toString() }
            val session = sessionRepository.create()
            val afterCount = Sessions.selectAll().count()
            assertEquals(beforeCount + 1, afterCount)
            assertNotNull(session)
            assertNull(session.lastUser)
            assertTrue(session.id.value.toString() !in existingIds)
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = sessionRepository.getById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            assertNotNull(session)
            assertEquals("868f9d04-d1e8-44c9-84d3-2ef3da517d4c", session.id.value.toString())
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = sessionRepository.getById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f2001"))
            assertNull(session)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete session`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val before = Sessions.selectAll().count()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            sessionRepository.delete(session)
            val after = Sessions.selectAll().count()
            assertEquals(before - 1, after)
            assertNull(Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")))
        }

    }

    @Nested
    inner class SetLastUser {

        @Test
        fun `set last user from null to not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            val user = User.findById(1)!!
            sessionRepository.setLastUser(session, user)
            val newSession = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            assertNotNull(newSession)
            assertNotNull(newSession.lastUser)
            assertEquals(user.id.value, newSession.lastUser!!.id.value)
        }

        @Test
        fun `set last user from not null to null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                UPDATE SESSIONS
                    SET LAST_USER_ID=1
                    WHERE ID='868f9d04-d1e8-44c9-84d3-2ef3da517d4c';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            sessionRepository.setLastUser(session, null)
            val newSession = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

        @Test
        fun `set last user from not null to not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                UPDATE SESSIONS
                    SET LAST_USER_ID=1
                    WHERE ID='868f9d04-d1e8-44c9-84d3-2ef3da517d4c';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            val user = User.findById(1)!!
            sessionRepository.setLastUser(session, user)
            val newSession = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            assertNotNull(newSession)
            assertNotNull(newSession.lastUser)
            assertEquals(user.id.value, newSession.lastUser!!.id.value)
        }

        @Test
        fun `set last user from null to null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            sessionRepository.setLastUser(session, null)
            val newSession = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

    }

    @Nested
    inner class GetLastUser {

        @Test
        fun `get last user when null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            val user = sessionRepository.getLastUser(session)
            assertNull(user)
        }

        @Test
        fun `get last user when not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID)
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                UPDATE SESSIONS
                    SET LAST_USER_ID=1
                    WHERE ID='868f9d04-d1e8-44c9-84d3-2ef3da517d4c';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))!!
            val user = sessionRepository.getLastUser(session)
            assertNotNull(user)
            assertEquals(1, user.id.value)
        }

    }

}
