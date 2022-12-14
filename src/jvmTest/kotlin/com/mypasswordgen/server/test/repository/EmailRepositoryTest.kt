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
import com.mypasswordgen.server.repository.impl.EmailRepositoryImpl
import com.mypasswordgen.server.tables.Emails
import io.mockk.every
import io.mockk.mockkStatic
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmailRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
            val initEmailAddress2 = "not-email"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            val initEmailAddress2Encoded = "EmailxyzCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            every { initEmailAddress2.encode() } returns initEmailAddress2Encoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
            """.trimIndent()
            )
            val emailRepository = EmailRepositoryImpl()
            val beforeEmails = Emails.select { Emails.userId eq initUserId }
            val beforeCount = beforeEmails.count()
            val beforeIds = beforeEmails.map { it[Emails.id].value }
            val user = User.findById(initUserId)!!
            val emailId = emailRepository
                .createAndGetId(initEmailAddress2, user)
            val afterEmails = Emails.select { Emails.userId eq initUserId }
            val afterCount = afterEmails.count()
            val afterIds = afterEmails.map { it[Emails.id].value }
            assertNotNull(emailId)
            assertTrue(emailId !in beforeIds)
            assertTrue(emailId in afterIds)
            assertEquals(beforeCount + 1, afterCount)
            val email = Email.findById(emailId)
            assertNotNull(email)
            assertEquals(initEmailAddress2Encoded, email.emailAddress)
            assertEquals(initUserId, email.user.id.value)
        }

        @Test
        fun `create when it already exist`() {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
            val initUsernameEncoded = "UserAbc"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            var beforeCount = 0L
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                COMMIT;
            """.trimIndent()
                )
                val emailRepository = EmailRepositoryImpl()
                val beforeEmails = Emails.select { Emails.userId eq initUserId }
                beforeCount = beforeEmails.count()
                val user = User.findById(initUserId)!!
                assertThrows<Exception> {
                    emailRepository
                        .createAndGetId(initEmailAddress, user)
                }
            }
            testTransaction {
                val afterEmails = Emails.select { Emails.userId eq initUserId }
                val afterCount = afterEmails.count()
                assertEquals(beforeCount, afterCount)
            }
        }


    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
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
            val emailRepository = EmailRepositoryImpl()
            val email = emailRepository.getById(initEmailId)
            assertNotNull(email)
            assertEquals(initEmailId, email.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initEmailId2 = UUID.fromString("a82de541-907e-49ff-b722-ab96e9d69716")
            val initUsername = "User123"
            val initEmailAddress = "email002"
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
            val emailRepository = EmailRepositoryImpl()
            val email = emailRepository.getById(initEmailId2)
            assertNull(email)
        }

    }

    @Nested
    inner class GetByAddressAndUser {

        @Test
        fun `get when exists`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
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
            val emailRepository = EmailRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = emailRepository.getByAddressAndUser(initEmailAddress, user)
            assertNotNull(email)
            assertEquals(initEmailAddress.encode(), email.emailAddress)
            assertEquals(initUserId, email.user.id.value)
        }

        @Test
        fun `get when exists in other user`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initUserId2 = UUID.fromString("903a8b62-c86a-4e2c-9879-7ec6e8598e7b")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initEmailAddress = "email002"
            val initUsernameEncoded = "UserAbc"
            val initUsername2Encoded = "UserXyz"
            val initEmailAddressEncoded = "EmailCba"
            mockkStatic("com.mypasswordgen.server.repository.crypto.Sha256Kt")
            every { initUsername.encode() } returns initUsernameEncoded
            every { initUsername2.encode() } returns initUsername2Encoded
            every { initEmailAddress.encode() } returns initEmailAddressEncoded
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                    VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
            """.trimIndent()
            )
            val emailRepository = EmailRepositoryImpl()
            val user = User.findById(initUserId2)!!
            val email = emailRepository.getByAddressAndUser(initEmailAddress, user)
            assertNull(email)
        }

        @Test
        fun `get when email doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
            val initEmailAddress2 = "not-email"
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
            val emailRepository = EmailRepositoryImpl()
            val user = User.findById(initUserId)!!
            val email = emailRepository.getByAddressAndUser(initEmailAddress2, user)
            assertNull(email)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete email`() = testTransaction {
            val initSessionId = UUID.fromString("868f9d04-d1e8-44c9-84d3-2ef3da517d4c")
            val initUserId = UUID.fromString("891b0b44-871f-4e4e-9efa-9c2dfe286250")
            val initEmailId = UUID.fromString("2435e389-f702-42fa-8f9b-f1a5adbbbbd0")
            val initUsername = "User123"
            val initEmailAddress = "email002"
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
            val emailRepository = EmailRepositoryImpl()
            val before = Emails.selectAll().count()
            val email = Email.findById(initEmailId)!!
            emailRepository.delete(email)
            val after = Emails.selectAll().count()
            assertEquals(before - 1, after)
            assertNull(Email.findById(initEmailId))
        }

    }

}
