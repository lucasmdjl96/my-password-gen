package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.impl.UserRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.tables.Users
import org.jetbrains.exposed.sql.select
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val beforeUsers =
                Users.select { Users.sessionId eq UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c") }
            val beforeCount = beforeUsers.count()
            val beforeIds = beforeUsers.map { it[Users.id].value }
            val userId = userRepository
                .createAndGetId("not-user", UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            val afterUsers =
                Users.select { Users.sessionId eq UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c") }
            val afterCount = afterUsers.count()
            val afterIds = afterUsers.map { it[Users.id].value }
            assertNotNull(userId)
            assertTrue(userId !in beforeIds)
            assertTrue(userId in afterIds)
            assertEquals(beforeCount + 1, afterCount)
            val user = User.findById(userId)
            assertNotNull(user)
            assertEquals("not-user", user.username)
            assertEquals(UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"), user.session.id.value)
            assertNull(user.lastEmail)
        }

        @Test
        fun `create when it already exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val beforeUsers =
                Users.select { Users.sessionId eq UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c") }
            val beforeCount = beforeUsers.count()
            val userId = userRepository
                .createAndGetId("User123", UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c"))
            val afterUsers =
                Users.select { Users.sessionId eq UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c") }
            val afterCount = afterUsers.count()
            assertNull(userId)
            assertEquals(beforeCount, afterCount)
        }

        @Test
        fun `create when session doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            assertThrows<Exception> {
                userRepository
                    .createAndGetId("User123", UUID.fromString("757f2ad6-aa06-0000-aea3-d5e6cb9f0001"))
            }
            Unit
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getById(1)
            assertNotNull(user)
            assertEquals(1, user.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getById(2)
            assertNull(user)
        }

    }

    @Nested
    inner class GetByNameAndSession {

        @Test
        fun `get when exists`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(
                "User123",
                UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            )
            assertNotNull(user)
            assertEquals("User123", user.username)
            assertEquals("868f9d04-d1e8-44c9-84d3-2ef3da517d4c", user.session.id.value.toString())
        }

        @Test
        fun `get when exists in other session`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO SESSIONS (ID) VALUES ('2b3777f9-fa56-4304-b7e6-c80058f5b7f4');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                    INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User002', '2b3777f9-fa56-4304-b7e6-c80058f5b7f4');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(
                "User002",
                UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            )
            assertNull(user)
        }

        @Test
        fun `get when session doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(
                "User123",
                UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0019")
            )
            assertNull(user)
        }

        @Test
        fun `get when username doesn't exist in session`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(
                "not-user",
                UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            )
            assertNull(user)
        }

    }

    @Nested
    inner class MoveAll {

        @Test
        fun `move all when both sessions exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO SESSIONS (ID) VALUES ('bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User234', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val newSessionId = UUID.fromString("bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5")
            val userIds = Users.select { Users.sessionId eq oldSessionId }.map { it[Users.id].value }
            userRepository.moveAll(oldSessionId, newSessionId)
            for (id in userIds) {
                val user = User.findById(id)
                assertNotNull(user)
                assertEquals(newSessionId, user.session.id.value)
            }
        }

        @Test
        fun `move all when origin session doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO SESSIONS (ID) VALUES ('bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User234', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("757f2ad6-aa06-0000-aea3-d5e6cb9f0001")
            val newSessionId = UUID.fromString("bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5")
            val countNewBefore = Users.select { Users.sessionId eq newSessionId }.count()
            userRepository.moveAll(oldSessionId, newSessionId)
            val countOldAfter = Users.select { Users.sessionId eq oldSessionId }.count()
            val countNewAfter = Users.select { Users.sessionId eq newSessionId }.count()
            assertEquals(countNewBefore, countNewAfter)
            assertEquals(0, countOldAfter)
        }

        @Test
        fun `move all when target session doesn't exist`() {
            var userIds = emptyList<Int>()
            var oldSessionId = UUID.randomUUID()
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO SESSIONS (ID) VALUES ('bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User234', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                COMMIT;
            """.trimIndent()
                )
                val userRepository = UserRepositoryImpl()
                oldSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
                val newSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0020")
                userIds = Users.select { Users.sessionId eq oldSessionId }.map { it[Users.id].value }
                assertThrows<Exception> { userRepository.moveAll(oldSessionId, newSessionId) }
            }
            testTransaction {
                for (id in userIds) {
                    val user = User.findById(id)
                    assertNotNull(user)
                    assertEquals(oldSessionId, user.session.id.value)
                }
            }
        }


        @Test
        fun `move all when target session has conflict`() {
            var userIds = emptyList<Int>()
            var oldSessionId = UUID.randomUUID()
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO SESSIONS (ID) VALUES ('bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User234', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User234', 'bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5');
                COMMIT;
            """.trimIndent()
                )
                val userRepository = UserRepositoryImpl()
                oldSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
                val newSessionId = UUID.fromString("bd8cd8bb-1c1b-4602-8447-b99f7db0dbd5")
                userIds = Users.select { Users.sessionId eq oldSessionId }.map { it[Users.id].value }
                assertThrows<Exception> { userRepository.moveAll(oldSessionId, newSessionId) }
            }
            testTransaction {
                for (id in userIds) {
                    val user = User.findById(id)
                    assertNotNull(user)
                    assertEquals(oldSessionId, user.session.id.value)
                }
            }
        }

    }

    @Nested
    inner class SetLastEmail {

        @Test
        fun `set last email from null to not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            val email = Email.findById(1)!!
            userRepository.setLastEmail(user, email)
            val newUser = User.findById(1)
            assertNotNull(newUser)
            assertNotNull(newUser.lastEmail)
            assertEquals(email.id.value, newUser.lastEmail!!.id.value)
        }

        @Test
        fun `set last email from not null to null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                UPDATE USERS
                    SET LAST_EMAIL_ID=1
                    WHERE ID=1;
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            userRepository.setLastEmail(user, null)
            val newUser = User.findById(1)
            assertNotNull(newUser)
            assertNull(newUser.lastEmail)
        }

        @Test
        fun `set last email from not null to not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                UPDATE USERS
                    SET LAST_EMAIL_ID=1
                    WHERE ID=1;
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            val email = Email.findById(1)!!
            userRepository.setLastEmail(user, email)
            val newUser = User.findById(1)
            assertNotNull(newUser)
            assertNotNull(newUser.lastEmail)
            assertEquals(email.id.value, newUser.lastEmail!!.id.value)
        }

        @Test
        fun `set last email from null to null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            userRepository.setLastEmail(user, null)
            val newUser = User.findById(1)
            assertNotNull(newUser)
            assertNull(newUser.lastEmail)
        }

    }

    @Nested
    inner class GetLastEmail {

        @Test
        fun `get last email when null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            val email = userRepository.getLastEmail(user)
            assertNull(email)
        }

        @Test
        fun `get last email when not null`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                UPDATE USERS
                    SET LAST_EMAIL_ID=1
                    WHERE ID=1;
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(1)!!
            val email = userRepository.getLastEmail(user)
            assertNotNull(email)
            assertEquals(1, email.id.value)
        }

    }

}
