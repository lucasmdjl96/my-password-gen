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

class UserRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val beforeUsers = Users.select { Users.sessionId eq UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001") }
                val beforeCount = beforeUsers.count()
                val beforeIds = beforeUsers.map { it[Users.id].value }
                val userId = userRepository
                    .createAndGetId("not-user", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                val afterUsers = Users.select { Users.sessionId eq UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001") }
                val afterCount = afterUsers.count()
                val afterIds = afterUsers.map { it[Users.id].value }
                assertNotNull(userId)
                assert(userId !in beforeIds)
                assert(userId in afterIds)
                assertEquals(beforeCount + 1, afterCount)
                val user = User.findById(userId)
                assertNotNull(user)
                assertEquals("not-user", user.username)
                assertEquals(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"), user.session.id.value)
                assertNull(user.lastEmail)
            }
        }

        @Test
        fun `create when it already exist`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val beforeUsers = Users.select { Users.sessionId eq UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001") }
                val beforeCount = beforeUsers.count()
                val userId = userRepository
                    .createAndGetId("User002", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                val afterUsers = Users.select { Users.sessionId eq UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001") }
                val afterCount = afterUsers.count()
                assertNull(userId)
                assertEquals(beforeCount, afterCount)
            }
        }

        @Test
        fun `create when session doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val userId = userRepository
                    .createAndGetId("User002", UUID.fromString("757f2ad6-aa06-0000-aea3-d5e6cb9f0001"))
                assertNull(userId)
            }
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getById(2)
                assertNotNull(user)
                assertEquals(2, user.id.value)
            }
        }

        @Test
        fun `get by id when it doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getById(50)
                assertNull(user)
            }
        }

    }

    @Nested
    inner class GetByNameAndSession {

        @Test
        fun `get when exists`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getByNameAndSession("User002", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertNotNull(user)
                assertEquals("User002", user.username)
                assertEquals("757f2ad6-aa06-4403-aea3-d5e6cb9f0001", user.session.id.value.toString())
            }
        }

        @Test
        fun `get when exists in other session`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getByNameAndSession("User002", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                assertNull(user)
            }
        }

        @Test
        fun `get when session doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getByNameAndSession("User002", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0019"))
                assertNull(user)
            }
        }

        @Test
        fun `get when username doesn't exist in session`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = userRepository.getByNameAndSession("not-user", UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0019"))
                assertNull(user)
            }
        }

    }

    @Nested
    inner class MoveAll {

        @Test
        fun `move all when both sessions exist`() {
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
            val newSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0003")
            testTransaction {
                val userIds = Users.select { Users.sessionId eq oldSessionId }.map { it[Users.id].value }
                userRepository.moveAll(oldSessionId, newSessionId)
                for (id in userIds) {
                    val user = User.findById(id)
                    assertNotNull(user)
                    assertEquals(newSessionId, user.session.id.value)
                }
            }
        }

        @Test
        fun `move all when origin session doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("757f2ad6-aa06-0000-aea3-d5e6cb9f0001")
            val newSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0003")
            testTransaction {
                val countNewBefore = Users.select { Users.sessionId eq newSessionId }.count()
                userRepository.moveAll(oldSessionId, newSessionId)
                val countOldAfter = Users.select { Users.sessionId eq oldSessionId }.count()
                val countNewAfter = Users.select { Users.sessionId eq newSessionId }.count()
                assertEquals(countNewBefore, countNewAfter)
                assertEquals(0, countOldAfter)
            }
        }

        @Test
        fun `move all when target session doesn't exist`() {
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
            val newSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0020")
            var userIds = listOf<Int>()
            testTransaction {
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
            val userRepository = UserRepositoryImpl()
            val oldSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
            val newSessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002")
            var userIds = listOf<Int>()
            testTransaction {
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
        fun `set last email from null to not null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(2)!!
                val email = Email.findById(3)!!
                userRepository.setLastEmail(user, email)
                val newUser = User.findById(2)
                assertNotNull(newUser)
                assertNotNull(newUser.lastEmail)
                assertEquals(email.id.value, newUser.lastEmail!!.id.value)
            }
        }

        @Test
        fun `set last email from not null to null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(3)!!
                userRepository.setLastEmail(user, null)
                val newUser = User.findById(3)
                assertNotNull(newUser)
                assertNull(newUser.lastEmail)
            }
        }

        @Test
        fun `set last email from not null to not null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(3)!!
                val email = Email.findById(4)!!
                userRepository.setLastEmail(user, email)
                val newUser = User.findById(3)
                assertNotNull(newUser)
                assertNotNull(newUser.lastEmail)
                assertEquals(email.id.value, newUser.lastEmail!!.id.value)
            }
        }

        @Test
        fun `set last email from null to null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(2)!!
                userRepository.setLastEmail(user, null)
                val newUser = User.findById(2)
                assertNotNull(newUser)
                assertNull(newUser.lastEmail)
            }
        }

    }

    @Nested
    inner class GetLastEmail {

        @Test
        fun `get last email when null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(2)!!
                val email = userRepository.getLastEmail(user)
                assertNull(email)
            }
        }

        @Test
        fun `get last email when not null`() {
            val userRepository = UserRepositoryImpl()
            testTransaction {
                val user = User.findById(3)!!
                val email = userRepository.getLastEmail(user)
                assertNotNull(email)
                assertEquals(4, email.id.value)
            }
        }

    }

}
