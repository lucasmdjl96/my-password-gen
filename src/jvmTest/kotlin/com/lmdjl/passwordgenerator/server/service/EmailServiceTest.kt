package com.lmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.service.UserService
import com.lucasmdjl.passwordgenerator.server.service.impl.EmailServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmailServiceTest : ServiceTestParent() {

    private lateinit var emailRepositoryMock: EmailRepository
    private lateinit var userServiceMock: UserService

    private lateinit var dummySessionId: UUID
    private lateinit var dummyEmailServerDto: EmailServerDto
    private lateinit var dummyUser: User
    private var dummyEmailId: Int = 0
    private lateinit var dummyEmail: Email

    @BeforeAll
    override fun initMocks() {
        emailRepositoryMock = mockk()
        userServiceMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyEmailServerDto = EmailServerDto("email@gmail.com", "user123")
        dummyUser = User(EntityID(1, Users))
        dummyEmailId = 3
        dummyEmail = Email(EntityID(2, Emails))
    }

    @Nested
    inner class Create {

        @Test
        fun `create email when user exists and email doesn't exist yet`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.createAndGetId(
                    dummyEmailServerDto.emailAddress,
                    dummyUser
                )
            } returns dummyEmailId
            every { emailRepositoryMock.getById(dummyEmailId) } returns dummyEmail
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val emailResult = emailService.create(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.createAndGetId(dummyEmailServerDto.emailAddress, dummyUser)
                emailRepositoryMock.getById(dummyEmailId)
            }

            assertEquals(dummyEmail, emailResult)
        }

        @Test
        fun `create email when user exists and email already exists`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every { emailRepositoryMock.createAndGetId(dummyEmailServerDto.emailAddress, dummyUser) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val emailResult = emailService.create(dummyEmailServerDto, dummySessionId)

            verify(exactly = 0) {
                emailRepositoryMock.getById(any())
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.createAndGetId(dummyEmailServerDto.emailAddress, dummyUser)
            }

            assertNull(emailResult)
        }

        @Test
        fun `create email when user doesn't exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            assertThrows<Exception> { emailService.create(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find email when user and email exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress,
                    dummyUser
                )
            } returns dummyEmail
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val emailResult = emailService.find(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            assertEquals(dummyEmail, emailResult)
        }

        @Test
        fun `find email when user exists but email doesn't exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every { emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val emailResult = emailService.find(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            assertNull(emailResult)
        }

        @Test
        fun `find email when user doesn't exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            assertThrows<Exception> { emailService.find(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete email when user and email exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress,
                    dummyUser
                )
            } returns dummyEmail
            every { emailRepositoryMock.delete(dummyEmail) } just Runs
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val result = emailService.delete(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
                emailRepositoryMock.delete(dummyEmail)
            }
            assertEquals(Unit, result)
        }

        @Test
        fun `delete email when user exists but email doesn't exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns dummyUser
            every { emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            val result = emailService.delete(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            verify(exactly = 0) {
                emailRepositoryMock.delete(any())
            }
            assertNull(result)
        }

        @Test
        fun `delete email when user doesn't exist`() {
            every { userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(emailRepositoryMock, userServiceMock)

            assertThrows<Exception> { emailService.delete(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userServiceMock.find(dummyEmailServerDto.userServerDto, dummySessionId)
            }
        }

    }

}
