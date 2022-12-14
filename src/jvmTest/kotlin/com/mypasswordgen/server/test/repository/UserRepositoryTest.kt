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

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.repository.impl.UserRepositoryImpl
import com.mypasswordgen.server.tables.Users
import io.mockk.every
import io.mockk.mockkStatic
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
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initUsername2 = "not-user"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val beforeUsers =
                Users.select { Users.sessionId eq initSessionId }
            val beforeCount = beforeUsers.count()
            val beforeIds = beforeUsers.map { it[Users.id].value }
            val userId = userRepository
                .createAndGetId(initUsername2, initSessionId)
            val afterUsers =
                Users.select { Users.sessionId eq initSessionId }
            val afterCount = afterUsers.count()
            val afterIds = afterUsers.map { it[Users.id].value }
            assertNotNull(userId)
            assertTrue(userId !in beforeIds)
            assertTrue(userId in afterIds)
            assertEquals(beforeCount + 1, afterCount)
            val user = User.findById(userId)
            assertNotNull(user)
            assertEquals(initUsername2Encoded, user.username)
            assertEquals(initSessionId, user.session.id.value)
            assertNull(user.lastEmail)
        }

        @Test
        fun `create when it already exist`() {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            var beforeCount = 0L
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                COMMIT;
            """.trimIndent()
                )
                val userRepository = UserRepositoryImpl()
                val beforeUsers =
                    Users.select { Users.sessionId eq initSessionId }
                beforeCount = beforeUsers.count()
                assertThrows<Exception> {
                    userRepository
                        .createAndGetId(initUsername, initSessionId)
                }
            }
            testTransaction {
                val afterUsers =
                    Users.select { Users.sessionId eq initSessionId }
                val afterCount = afterUsers.count()
                assertEquals(beforeCount, afterCount)
            }
        }

        @Test
        fun `create when session doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("af4fcab5-52e0-47b8-af6f-3addcda81392")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
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
            val userRepository = UserRepositoryImpl()
            assertThrows<Exception> {
                userRepository
                    .createAndGetId(initUsername, initSessionId2)
            }
            Unit
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
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
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getById(initUserId)
            assertNotNull(user)
            assertEquals(initUserId, user.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("e6ffeb59-77bf-4132-98b0-4d5b5f8441cb")
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
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getById(initUserId2)
            assertNull(user)
        }

    }

    @Nested
    inner class GetByNameAndSession {

        @Test
        fun `get when exists`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
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
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(initUsername, initSessionId)
            assertNotNull(user)
            assertEquals(initUsernameEncoded, user.username)
            assertEquals(initSessionId, user.session.id.value)
        }

        @Test
        fun `get when exists in other session`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("676a41bf-a779-47ff-8321-88b2c383a7fd")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("81d91b6c-443c-4b3d-98b8-fdb2dee54e58")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId2');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId2');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(initUsername2, initSessionId)
            assertNull(user)
        }

        @Test
        fun `get when session doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("a60d7f85-3298-4010-b689-af67612533c0")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
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
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(initUsername, initSessionId2)
            assertNull(user)
        }

        @Test
        fun `get when username doesn't exist in session`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initUsername2 = "not-user"
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
            val userRepository = UserRepositoryImpl()
            val user = userRepository.getByNameAndSession(initUsername2, initSessionId)
            assertNull(user)
        }

    }

    @Nested
    inner class MoveAll {

        @Test
        fun `move all when both sessions exist`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("676a41bf-a779-47ff-8321-88b2c383a7fd")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("81d91b6c-443c-4b3d-98b8-fdb2dee54e58")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId2');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val userIds = Users.select { Users.sessionId eq initSessionId }.map { it[Users.id].value }
            userRepository.moveAll(initSessionId, initSessionId2)
            for (id in userIds) {
                val user = User.findById(id)
                assertNotNull(user)
                assertEquals(initSessionId2, user.session.id.value)
            }
        }

        @Test
        fun `move all when origin session doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("676a41bf-a779-47ff-8321-88b2c383a7fd")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("81d91b6c-443c-4b3d-98b8-fdb2dee54e58")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val countNewBefore = Users.select { Users.sessionId eq initSessionId }.count()
            userRepository.moveAll(initSessionId2, initSessionId)
            val countOldAfter = Users.select { Users.sessionId eq initSessionId2 }.count()
            val countNewAfter = Users.select { Users.sessionId eq initSessionId }.count()
            assertEquals(countNewBefore, countNewAfter)
            assertEquals(0, countOldAfter)
        }

        @Test
        fun `move all when target session doesn't exist`() {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("676a41bf-a779-47ff-8321-88b2c383a7fd")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("81d91b6c-443c-4b3d-98b8-fdb2dee54e58")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            var userIds = emptyList<UUID>()
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                COMMIT;
            """.trimIndent()
                )
                val userRepository = UserRepositoryImpl()
                userIds = Users.select { Users.sessionId eq initSessionId }.map { it[Users.id].value }
                assertThrows<Exception> { userRepository.moveAll(initSessionId, initSessionId2) }
            }
            testTransaction {
                for (id in userIds) {
                    val user = User.findById(id)
                    assertNotNull(user)
                    assertEquals(initSessionId, user.session.id.value)
                }
            }
        }


        @Test
        fun `move all when target session has conflict`() {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initSessionId2 = UUID.fromString("676a41bf-a779-47ff-8321-88b2c383a7fd")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUserId2 = UUID.fromString("81d91b6c-443c-4b3d-98b8-fdb2dee54e58")
            val initUsername = "User123"
            val initUsernameEncoded = "UserAbc"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            var userIds = emptyList<UUID>()
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId2');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsernameEncoded', '$initSessionId2');
                COMMIT;
            """.trimIndent()
                )
                val userRepository = UserRepositoryImpl()
                userIds = Users.select { Users.sessionId eq initSessionId }.map { it[Users.id].value }
                assertThrows<Exception> { userRepository.moveAll(initSessionId, initSessionId2) }
            }
            testTransaction {
                for (id in userIds) {
                    val user = User.findById(id)
                    assertNotNull(user)
                    assertEquals(initSessionId, user.session.id.value)
                }
            }
        }

    }

    @Nested
    inner class SetLastEmail {

        @Test
        fun `set last email from null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = Email.findById(initEmailId)!!
            userRepository.setLastEmail(user, email)
            val newUser = User.findById(initUserId)
            assertNotNull(newUser)
            assertNotNull(newUser.lastEmail)
            assertEquals(email.id.value, newUser.lastEmail!!.id.value)
        }

        @Test
        fun `set last email from not null to null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                UPDATE USERS
                    SET LAST_EMAIL_ID = '$initEmailId'
                    WHERE ID = '$initUserId'
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            userRepository.setLastEmail(user, null)
            val newUser = User.findById(initUserId)
            assertNotNull(newUser)
            assertNull(newUser.lastEmail)
        }

        @Test
        fun `set last email from not null to not null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                UPDATE USERS
                    SET LAST_EMAIL_ID = '$initEmailId'
                    WHERE ID = '$initUserId'
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = Email.findById(initEmailId)!!
            userRepository.setLastEmail(user, email)
            val newUser = User.findById(initUserId)
            assertNotNull(newUser)
            assertNotNull(newUser.lastEmail)
            assertEquals(email.id.value, newUser.lastEmail!!.id.value)
        }

        @Test
        fun `set last email from null to null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            userRepository.setLastEmail(user, null)
            val newUser = User.findById(initUserId)
            assertNotNull(newUser)
            assertNull(newUser.lastEmail)
        }

    }

    @Nested
    inner class GetLastEmail {

        @Test
        fun `get last email when null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = userRepository.getLastEmail(user)
            assertNull(email)
        }

        @Test
        fun `get last email when not null`() = testTransaction {
            val initSessionId = UUID.fromString("ef383190-6c04-405b-986f-ec320820b7fe")
            val initUserId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initEmailId = UUID.fromString("4a2e9aed-9639-4b1e-8dcc-1dc8eb9c2f06")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                UPDATE USERS
                    SET LAST_EMAIL_ID = '$initEmailId'
                    WHERE ID = '$initUserId'
            """.trimIndent()
            )
            val userRepository = UserRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = userRepository.getLastEmail(user)
            assertNotNull(email)
            assertEquals(initEmailId, email.id.value)
        }

    }

}
