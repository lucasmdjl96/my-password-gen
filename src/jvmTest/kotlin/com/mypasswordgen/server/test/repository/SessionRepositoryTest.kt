/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.repository

import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.repository.impl.SessionRepositoryImpl
import com.mypasswordgen.server.tables.Sessions
import io.mockk.every
import io.mockk.mockkStatic
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
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

    @Nested
    inner class GetLastUserWithSessionID {

        @Test
        fun `get last user when no session`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initSessionId2 = UUID.fromString("576e3ad7-481a-4426-9074-a855fffef0d5")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = sessionRepository.getLastUser(initSessionId2)
            assertNull(user)
        }

        @Test
        fun `get last user when null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', 'initUsernameEncoded', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = sessionRepository.getLastUser(initSessionId)
            assertNull(user)
        }

        @Test
        fun `get last user when not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = sessionRepository.getLastUser(initSessionId)
            assertNotNull(user)
            assertEquals(initUserId, user.id.value)
        }
    }

    @Nested
    inner class SetLastUserWithSessionId {

        @Test
        fun `set last user when no session`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initSessionId2 = UUID.fromString("8e6a37e6-a6fa-4767-b53e-fffced72580e")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = User.findById(initUserId)!!
            sessionRepository.setLastUser(initSessionId2, user)
            val newSession = Session.findById(initSessionId2)
            assertNull(newSession)
        }

        @Test
        fun `set last user from null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = User.findById(initUserId)!!
            sessionRepository.setLastUser(initSessionId, user)
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            sessionRepository.setLastUser(initSessionId, null)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

        @Test
        fun `set last user from not null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("6a1cfcd2-040f-4756-aba6-cad8e0934ff9")
            val initUserId = UUID.fromString("7f2b4ee9-d150-4d67-9c5d-5d957476ed56")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                UPDATE SESSIONS
                    SET LAST_USER_ID = '$initUserId'
                    WHERE ID = '$initSessionId';
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            val user = User.findById(initUserId)!!
            sessionRepository.setLastUser(initSessionId, user)
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
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
            """.trimIndent()
            )
            val sessionRepository = SessionRepositoryImpl()
            sessionRepository.setLastUser(initSessionId, null)
            val newSession = Session.findById(initSessionId)
            assertNotNull(newSession)
            assertNull(newSession.lastUser)
        }

    }

}
