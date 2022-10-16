package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmailRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val beforeEmails = Emails.select { Emails.userId eq 2 }
                val beforeCount = beforeEmails.count()
                val beforeIds = beforeEmails.map { it[Emails.id].value }
                val user = User.findById(2)!!
                val emailId = emailRepository
                    .createAndGetId("not-email", user)
                val afterEmails = Emails.select { Emails.userId eq 2 }
                val afterCount = afterEmails.count()
                val afterIds = afterEmails.map { it[Emails.id].value }
                assertNotNull(emailId)
                assertTrue(emailId !in beforeIds)
                assertTrue(emailId in afterIds)
                assertEquals(beforeCount + 1, afterCount)
                val email = Email.findById(emailId)
                assertNotNull(email)
                assertEquals("not-email", email.emailAddress)
                assertEquals(2, email.user.id.value)
            }
        }

        @Test
        fun `create when it already exist`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val beforeEmails = Emails.select { Emails.userId eq 2 }
                val beforeCount = beforeEmails.count()
                val user = User.findById(2)!!
                val emailId = emailRepository
                    .createAndGetId("email002", user)
                val afterEmails = Emails.select { Emails.userId eq 2 }
                val afterCount = afterEmails.count()
                assertNull(emailId)
                assertEquals(beforeCount, afterCount)
            }
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val email = emailRepository.getById(2)
                assertNotNull(email)
                assertEquals(2, email.id.value)
            }
        }

        @Test
        fun `get by id when it doesn't exist`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val email = emailRepository.getById(50)
                assertNull(email)
            }
        }

    }

    @Nested
    inner class GetByAddressAndUser {

        @Test
        fun `get when exists`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val user = User.findById(2)!!
                val email = emailRepository.getByAddressAndUser("email002", user)
                assertNotNull(email)
                assertEquals("email002", email.emailAddress)
                assertEquals(2, email.user.id.value)
            }
        }

        @Test
        fun `get when exists in other user`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val user = User.findById(3)!!
                val email = emailRepository.getByAddressAndUser("email002", user)
                assertNull(email)
            }
        }

        @Test
        fun `get when email doesn't exist`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val user = User.findById(2)!!
                val email = emailRepository.getByAddressAndUser("not-email", user)
                assertNull(email)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete email`() {
            val emailRepository = EmailRepositoryImpl()
            testTransaction {
                val before = Emails.selectAll().count()
                val email = Email.findById(2)!!
                emailRepository.delete(email)
                val after = Emails.selectAll().count()
                assertEquals(before - 1, after)
                assertNull(Email.findById(2))
            }
        }

    }

}
