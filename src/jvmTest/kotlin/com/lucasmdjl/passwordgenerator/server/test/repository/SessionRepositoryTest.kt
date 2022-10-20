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
            val initSessionId = UUID.fromString("05fbc804-aac7-4911-8125-1b63aaa03e84")
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
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
            val initSessionId = UUID.fromString("05fbc804-aac7-4911-8125-1b63aaa03e84")
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = sessionRepository.getById(initSessionId)
            assertNotNull(session)
            assertEquals(initSessionId, session.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("05fbc804-aac7-4911-8125-1b63aaa03e84")
            val initSessionId2 = UUID.fromString("10c268b7-b576-4944-8184-0036af4d5057")
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = sessionRepository.getById(initSessionId2)
            assertNull(session)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete session`() = testTransaction {
            val initSessionId = UUID.fromString("05fbc804-aac7-4911-8125-1b63aaa03e84")
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val before = Sessions.selectAll().count()
            val session = Session.findById(initSessionId)!!
            sessionRepository.delete(session)
            val after = Sessions.selectAll().count()
            assertEquals(before - 1, after)
            assertNull(Session.findById(initSessionId))
        }

    }

    @Nested
    inner class SetLastUser {

        @Test
        fun `set last user from null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            val user = User.findById(initUserId)!!
            sessionRepository.setLastUser(session, user)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNotNull(newSession.lastUser)
            assertEquals(user.id.value, newSession.lastUser!!.id.value)
        }

        @Test
        fun `set last user from not null to null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            sessionRepository.setLastUser(session, null)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

        @Test
        fun `set last user from not null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            val user = User.findById(initUserId)!!
            sessionRepository.setLastUser(session, user)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNotNull(newSession.lastUser)
            assertEquals(user.id.value, newSession.lastUser!!.id.value)
        }

        @Test
        fun `set last user from null to null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            sessionRepository.setLastUser(session, null)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

    }

    @Nested
    inner class GetLastUser {

        @Test
        fun `get last user when null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            val user = sessionRepository.getLastUser(session)
            assertNull(user)
        }

        @Test
        fun `get last user when not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val session = Session.findById(initSessionId)!!
            val user = sessionRepository.getLastUser(session)
            assertNotNull(user)
            assertEquals(initUserId, user.id.value)
        }

    }

}
